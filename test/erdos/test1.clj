(ns erdos.test1
  (:require [clojure.test :refer [deftest is are]]
   [erdos.require-all :refer [require-all list-all-resources]]))

(require-all erdos.test1)

(deftest test-x-required
  (the-ns 'erdos.test1.a.x)
  (the-ns 'erdos.test1.a.y))

(deftest list-all-resources-test
  (is (seq (doall (list-all-resources :prefix "clojure" :suffix "core.clj"))))
  (is (seq (doall (list-all-resources "erdos"))))
  (is (empty? (list-all-resources "xyz"))))
