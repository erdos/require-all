(ns erdos.test1
  (:require [clojure.test :refer [deftest is are]]
   [erdos.require-all :refer [require-all]]))

(require-all erdos.test1)

(deftest test-x-required
  (the-ns 'erdos.test1.a.x)
  (the-ns 'erdos.test1.a.y))
