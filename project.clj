(defproject io.github.erdos/require-all "0.1.2"
  :description "Library to load resources by prefix."
  :url "http://github.com/erdos/require-all"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :profiles {:dev {:resource-paths ["test-resources"]}})
