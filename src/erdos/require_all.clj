(ns erdos.require-all
  (:import java.io.File)
  (:import java.net.URL)
  (:require [clojure.java.io :refer [file reader input-stream]]))


(defn- root-resource [lib] (.. (name lib) (replace \- \_) (replace \. \/)))


(defn- list-all-resources [root]
  (-> (Thread/currentThread)
      (.getContextClassLoader)
      (.getResources (str root))
      (enumeration-seq)))


(defn- walk-url [url] (file-seq (file url)))


(defn- file-hidden? [^File f]
  (.startsWith (.getName f) "."))


(defn- file-clj? [^File f]
  (or (.endsWith (.toLowerCase (.getName f)) ".clj")
      (.endsWith (.toLowerCase (.getName f)) ".cljc")))


(defn- file-class? [^File f]
  (-> f (.getName) (.toLowerCase) (.endsWith "__init.class")))


(defn- jar-url->items [^URL url]
  (assert (instance? URL url))
  (when (= "jar" (.getProtocol url))
    (let [innerfile (new URL (.getFile url))]
      (when (= "file" (.getProtocol innerfile))
        (let [[jar inside] (.split (.getFile innerfile) "!")
              inside (.substring inside 1)] ;; / levagasa az elejerol
          (for [elem (enumeration-seq (.entries (java.util.zip.ZipFile. (str jar))))
                :let [item (.getName elem)]
                :when (.startsWith item inside)
                :when (file-clj? (file item))]
            {:jar jar :item item :type :jar}))))))


(defn- file-url->items [url]
  (assert (instance? java.net.URL url))
  (when (= "file" (.getProtocol url))
    (for [f (walk-url (.getFile url))
          :when (file-clj? f)
          :when (not (file-hidden? f))]
      {:type :file :file f})))


(defn- form->ns-sym [form]
  (when (seq? form)
    (when (= 'ns (first form))
      (when (symbol? (second form))
        (second form)))))


(defn- enum-items [namespace-prefix]
  (mapcat (some-fn file-url->items jar-url->items)
          (list-all-resources (root-resource namespace-prefix))))


(defn- input-stream->ns [istream]
  (->> (repeatedly #(read {:eof ::eof} is))
       (take-while (complement #{::eof}))
       (some form->ns-sym )
       (with-open [is (new java.io.PushbackReader (reader istream))])))


(defmulti ^:private item-ns :type)


;; content is found inside a jar file
(defmethod item-ns :jar [item]
  (assert (:jar item))
  (assert (:item item))
  (let [zip (java.util.zip.ZipFile. (str (:jar item)))
        entry (.getEntry zip (str (:item item)))]
    (input-stream->ns (.getInputStream zip entry))))


(defmethod item-ns :file [x]
  (assert (:file x))
  (input-stream->ns (input-stream (:file x))))


(defn list-all-namespaces
  "Returns a seq of all namespace symbols with a given prefix"
  [namespace-prefix]
  (keep item-ns (enum-items namespace-prefix)))

;; TODO: blacklisting: do not import test classes
;; TODO: also require precompiled namespaces
;; TODO: work both in compile and runtime!

;; (list-all-namespaces 'erdos)
;; (list-all-namespaces 'clojure)

(defmacro require-all
  "Requires every namespace with a give prefix. "
  [prefix & {:as opts}]
  (assert (symbol? prefix))
  (assert (every? symbol? (:except opts)))
  (let [all-namespaces (into (sorted-set) (list-all-namespaces prefix))
        namespaces     (apply disj all-namespaces (:except opts))]
    (list* 'do

           ;; Runtime requires
           `(doseq [ns# (list-all-namespaces '~prefix)] (require ns#))

           ;; Compile time import
           (for [nss namespaces] `(require '~nss)))))

:OK
