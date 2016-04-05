(ns erdos.fun-walk-test
  (:require [erdos.fun.walk :refer :all]
            [clojure.test :refer :all]))


(deftest data-quote?-test
  (testing "data-quote"
    (are [?x] (quote? ?x)
         ''[1 2 3 4] ''nil
         ''1 '':asd ''a
         '(quote (f x)))
    (are [?x] (not (quote? ?x))
         '[1 2 3 4] [1 2 3 4]
         nil 'nil
         1 '1 :a ':a 'a
         '(f x) '())))


(deftest data-coll?-test
  (testing "data-coll?"
    ;; vectors
    (are [?x] (data-coll? ?x)
         [1 2 3] [[1 2 3]]
         '[1 2 3]
         )
    ;; quoted forms
    (are [?x] (not (data-coll? ?x))
         ''1 :a ':a "asd"
         '(1 2 3)
         ''(1 2 3)
         1)))


(deftest scalar?-test
  (testing "data-const?"
    (are [?x] (scalar? ?x)
         :a "asd" 123 ::asd #"asd")
    (are [?x] (not (scalar? ?x))
         [1 2 3] '(1 2) ''1 #{} () {:a 1})))


(deftest data?-test
  (are [?x] (data? ?x)
       [1 2 3] [[]] [nil :a]
       {[] []} #{[] [[]] [[[]]]}
       ''1 '(quote (f x)) ''(f x) ''''1
       '['(f x)] '#{'(f x) ''(g x)})
  (are [?x] (not (data? ?x))
       '(f x) '(f '1) '[(f x) (g x)]
       '(((quote 1)))))


;; (expr-*-test)

(deftest code-seq-test
  (is (= (set (code-seq '(f (inc x))))
         '#{(f (inc x)) (inc x) f inc x}))
  (is (= (set (code-seq '(f (g '(h 1)))))
         '#{(f (g '(h 1))) (g '(h 1)) '(h 1) f g})))



;;(println :OK)

:OK
