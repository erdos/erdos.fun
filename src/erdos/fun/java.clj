(ns erdos.fun.java)

(defn min-arity
  "Returns minimum arity of function"
  [f]
  (assert (fn? f))
  (->>
   (for [^java.lang.reflect.Method m (-> f class .getDeclaredMethods)
         :when (= "invoke" (.getName m))]
     (.getParameterCount m))
   (apply min)))

;; (min-arity min)
;; (min-arity reduce)


(defn ppstr [x] (with-out-str (pprint x)))

(defn exception? [x] (instance? Exception x))
