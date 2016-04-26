(ns erdos.fun-test
  "Unit tests for erdos.fun namespace."
  (:require [clojure.test :refer :all]
            [erdos.fun :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

(deftest genkwd-test
  (testing "Any parameter allowed"
    (is (keyword? (genkwd)))
    (are [x] (keyword? (genkwd x))
         nil 2 -2 true false :asd 'def "lala" str [] () #{})))

(deftest fixpt-test
  (testing "constant series"
    (is (= 1 (fixpt int 1)))
    (is (= "asdf" (fixpt str 'asdf)))))

(deftest pmap*-test
  (testing "empty coll"
    (are [x] (= x (pmap* str x))
         [] {} #{} ()))
  (testing "Produces result like (map) but unordered"
    (are [f xs] (= (sort (map f xs)) (sort (pmap* f xs)))
         inc [1 2 3 4]
         -   (range 1000))))
