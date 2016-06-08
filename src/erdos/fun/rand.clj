(ns erdos.fun.rand
  "Custom random function that lets one set seed."
  (:refer-clojure :exclude [rand]))

(def ^:private rand-obj
  (new java.util.Random))

(defn set-seed! [seed]
  (assert (integer? seed))
  (.setSeed ^java.util.Random rand-obj (long seed)))

(defn rand []
  (.nextDouble ^java.util.Random rand-obj))
