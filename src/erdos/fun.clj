(ns erdos.fun
  "Functional programming utilities"
  (:require [erdos.fun.walk :refer [safe-macroexpand-all quote?]]))


(def genkwd
  "Like gensym but for keywords.
  Take in mind that generated keywords are cached permanently."
  (comp keyword name gensym))


(defn fixpt
  "Calls f(x), f(f(x)), ... until the same value is returned."
  [f x]
  (let [fx (f x)]
    (if (= fx x)
      x, (recur f fx))))


(defmacro fn-memo
  "Creates a memoized function that may be recursive.
   You can use the given name or (recur) to create a
   recursive memoized function.

   Usage: `(fn-memo name [args..] body..)`
       or `(fn-memo [args..] body..)`"
  [head & body]
  (let [name (if (symbol? head) head)
        args (if name (first body) head)
        body (if name (rest body) body)]
    `(let [a# (atom {})]
      (fn ~(or name (gensym)) [& ps#]
        (if-let [kv# (find @a# ps#)]
          (val kv#)
          (let [[~@args] ps#
                res# (do ~@body)]
            (swap! a# assoc ps# res#)
            res#))))))


(defmacro fn-> [& calls]
  "Same as #(-> % ...)"
  `(fn* [x#] (-> x# ~@calls)))


(defmacro fn->> [& calls]
  "Same as #(->> % ...)"
  `(fn* [x#] (->> x# ~@calls)))


(defmacro fnx
  "Same as (fn [x] ...)"
  [& body]
  `(fn* [~'x] ~@body))


(defmacro fnxy
  "Same as (fn [x y] ...)"
  [& body]
  `(fn [~'x ~'y] ~@body))


(defmacro fncase [& clauses]
  "Same as #(case % ...)"
  `(fn* [x#] (case x# ~@clauses)))


(defmacro cond!
  "Like clojure.core/cond but throws exception when no clause matched."
  [& clauses]
  `(cond ~@clauses
         :default
         (throw (new IllegalStateException "No clause matched."))))


(defn comp<
  "Like `clojure.core/comp` with reverse argument list"
  [& fs] (comp (reverse fs)))

(defn partial<
  "Like `clojure.core/partial` with partial application from the right.
  EXAMPLES:
  - `((partial - 2) 5) ; => -3`
  - `((partial< - 2) 5) ; => 3`"
  ([f & args] (fn [& args2] (apply f (concat args2 args)))))

(comment

  ;; PARALLEL
  (def ^:dynamic *chunk-size* 256)
  (def ^:dynamic *threads-count* (+ 2 34))

  (defn pmap [f & xs] ;; TODO: implement this
    (apply pmap f xs))


  (defmacro pfor [bindings body]
    `(pmap deref (for ~bindings (delay ~body))))

  (defmacro pdoseq [bindings body]
    `(doall (pfor ~bindings ~body)))

  )


(defmacro def- [name & body]
  `(def ^:private ~name ~@body))


(defmacro defatom
  ([name] `(def ~name (atom nil)))
  ([name val] `(def ~name (atom ~val)))
  ([name docs val] `(def ~name ~docs (atom ~val))))

;; (defmacro atom= [expr])

;; todo: finish this. add event handlers
(defmacro defatom=
  "Define a var as an atom. Reloads atom value when one of the dereffed atoms change. Beware not to make circular references."
  ([name expr]
   `(defatom= ~name "" ~expr))
  ([name doc body]
   (let [expr (safe-macroexpand-all body)
         expr (tree-seq (fnx (and (coll? x) (not (quote? x)))) seq expr)
         expr (keep (fnx (when (and (seq? x)
                  (= 'clojure.core/deref (first x))
                  (symbol? (second x)))
                           (second x))) expr)
         f (gensym)]
     `(let [~f (fn [] ~body)]
        (def ~name ~doc (atom (~f)))
        ~@(for [e expr]
            `(cond (instance? clojure.lang.IBlockingDeref ~e)
                   (future (reset! ~name (~f)))
                   (instance? clojure.lang.IRef ~e)
                   (add-watch ~e :defatom
                              (fn [_# _# _# _#] (reset! ~name (~f))))
                   :else
                   (throw (IllegalArgumentException.
                           (str "Unexpected deref " '~e " of type " (type ~e))))))
        (var ~name)))))

(comment


  (defatom= a 1)
  (defatom= b 43)
  (defatom= c (+ @a @b))
  @c

  )

'OK
