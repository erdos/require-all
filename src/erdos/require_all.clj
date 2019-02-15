(ns erdos.require-all
  (:import java.io.File)
  (:import java.net.URL)
  (:require [clojure.java.io :refer [file reader input-stream resource as-url]]))


(set! *warn-on-reflection* true)


(defn- root-resource [lib]
  (assert (some? lib))
  (.. (name lib) (replace \- \_) (replace \. \/)))


(defn- all-resources-seq [root]
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


(defn- jar-url->items [match-fn ^URL url]
  (assert (fn? match-fn))
  (assert (instance? URL url))
  (when (= "jar" (.getProtocol url))
    (let [innerfile (new URL (.getFile url))]
      (when (= "file" (.getProtocol innerfile))
        (let [[jar inside] (.split (.getFile innerfile) "!")
              inside (.substring (str inside) 1)] ;; / levagasa az elejerol
          (for [elem (enumeration-seq (.entries (java.util.zip.ZipFile. (str jar))))
                :let [item (.getName ^java.util.zip.ZipEntry elem)]
                :when (.startsWith item inside)
                :when (match-fn (file item))]
            {:jar jar :item item :type :jar}))))))


(defn- file-url->items [match-fn ^URL url]
  (assert (fn? match-fn))
  (assert (instance? URL url))
  (when (= "file" (.getProtocol url))
    (for [f (walk-url (.getFile url))
          :when (not (.isDirectory ^File f))
          :when (match-fn f)
          :when (not (file-hidden? f))]
      {:type :file :file f})))


(defn- form->ns-sym [form]
  (when (seq? form)
    (when (= 'ns (first form))
      (when (symbol? (second form))
        (second form)))))


(defn- enum-items [match-fn namespace-prefix]
  (assert (fn? match-fn))
  (assert (some? namespace-prefix))
  (mapcat (some-fn (partial file-url->items match-fn) (partial jar-url->items match-fn))
          (all-resources-seq (root-resource namespace-prefix))))


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
  (assert (symbol? namespace-prefix))
  (keep item-ns (enum-items file-clj? namespace-prefix)))

(defn list-all-resources
  "Returns a seq of all resources as URL objects. Optional keys:
   - :prefix the directories of the resource
   - :predicate a predicate to decide if a result should be included.
   - :suffix suffix for the file name
   - :suffix-ci case insensitive suffix for file name"
  [& kvs]
  (let [[pre kvs] (if (even? (count kvs)) [nil kvs] [(not-empty (first kvs)) (next kvs)])
        {:keys [prefix suffix suffix-ci predicate]} (apply hash-map kvs)
        prefix   (or pre prefix "")
        match-fn (or predicate (constantly true))
        match-fn (cond
                   suffix    (every-pred #(.endsWith (.getName ^File %) ^String suffix) match-fn)
                   suffix-ci (every-pred #(.endsWith (.toLowerCase (.getName ^File %))
                                                     (.toLowerCase ^String suffix-ci)) match-fn)
                   :default  match-fn)]
    (assert (fn? match-fn))
    (->> (enum-items match-fn prefix)
         (keep #(or (some-> % :item resource)
                    (some-> % :file as-url))))))

(defmacro require-all
  "Requires every namespace with a given prefix.
   Opts is a map with the following keys:
   - :except - a colletion namespace name symbols that should not be required.
  "
  [prefix & {:as opts}]
  (assert (symbol? prefix))
  (assert (every? symbol? (:except opts)))
  (let [all-namespaces (into (sorted-set) (list-all-namespaces prefix))
        namespaces     (apply disj all-namespaces (:except opts))]
    (list* 'do

           ;; Runtime requires
           `(doseq [ns# (list-all-namespaces '~prefix)
                    :when (not (contains? '~(set (:except opts)) ns#))]
              (require ns#))

           ;; Compile time import
           (for [nss namespaces] `(require '~nss)))))

:OK
