(ns erdos.fun.benchmark)

(def ^:dynamic *bench-data*)

(defmacro mark [k expr]
  `(let [t0# (System/currentTimeMillis)]
     (try ~expr
          (finally (let [t1# (System/currentTimeMillis)
                         dt# (- t1# t0#)]
                     (swap! *bench-data* update-in [:times ~k]
                            (fnil conj ()) dt#))))))

(defn- grad* [xs ratio]
  (let [c* (* ratio (dec (count xs)))
        c+ (Math/ceil c*) c- (Math/floor c*)]
    (if (= c+ c-)
      (nth xs (int c*))
      (let [xc- (nth xs (int c-))
            xc+ (nth xs (int c+))
            c** (rem c* 1.0)]
        (+ (* c** xc+) (* (- 1.0 c**) xc-))))))

;; (grad* (range 1000) 0.999999)
(defn- quantiles [n xs]
  (mapv (partial grad* (vec (sort xs)))
        (next (range 0 1 (/ 1.0 n)))))

(defn benchmark-stat- [times]
  (assert (sequential? times))
  {:mean (/ (reduce + 0.0 times) (count times))
   :quartiles (quantiles 4 times)
   :count (count times)})

(defmacro benchmark [expr & {:as opts}]
  (let []
    `(binding [*bench-data* (atom {:times {}})]
       (let [t0# (System/currentTimeMillis)
             e# ~expr
             t1# (System/currentTimeMillis)
             d# @*bench-data*]
         {:result e#
          :total (- t1# t0#)
          :stats (zipmap (keys (:times d#))
                         (map benchmark-stat- (vals (:times d#))))}))))

(comment

  (benchmark (dotimes [i 100]
               (mark :iter (reduce + (range 10000 (+ 10000 (* i 10000))))
                )))


  )
