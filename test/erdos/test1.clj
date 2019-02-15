(ns erdos.test1
  "Testing the require-all macro"
  (:require [clojure.test :refer [deftest is are testing]]
            [erdos.require-all :refer [require-all list-all-resources]]))

(require-all erdos.test1)

(deftest test-x-required
  (testing "Namespaces are already required and therefore exist"
    (the-ns 'erdos.test1.a.x)
    (the-ns 'erdos.test1.a.y)))

(deftest list-all-resources-test-1
  (testing "Testing for clj source resources"
    (is (not-empty (list-all-resources :prefix "clojure" :suffix "core.clj")))
    (is (not-empty (list-all-resources "erdos")))
    (is (empty? (list-all-resources "xyz")))))

(deftest list-all-resources-test-2
  (testing "Find files by prefix"
    (testing "Empty prefix"
      (is (not-empty (list-all-resources ""))))
    (testing "Found by prefix only"
      (is (= 5 (count (list-all-resources "a"))))
      (is (= 2 (count (list-all-resources "a/b"))))
      (is (= 5 (count (list-all-resources :prefix "a")))))
    (testing "Files not found by prefix"
      (is (= [] (list-all-resources "b")))))

  (testing "Find files by suffix"
    (is (= 1 (count (list-all-resources :suffix ".edn"))))
    (is (= 1 (count (list-all-resources :suffix-ci ".Edn"))))
    (testing "Can not find when prefix does not exist."
      (is (empty? (list-all-resources "noprefix" :suffix ".edn")))))

  (testing "Find by predicate"
    (is (= 4 (count (list-all-resources :predicate #(.endsWith (str %) ".txt")))))))
