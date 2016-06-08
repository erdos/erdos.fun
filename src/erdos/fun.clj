(ns erdos.fun
  "Functional programming utilities"
  (:require [erdos.fun.walk :refer [safe-macroexpand-all quote?]]))


(def genkwd
  "Like gensym but for keywords.
  Take in mind that generated keywords are cached permanently."
  (comp keyword name gensym))


(defn fixpt
  "Calls f(x), f(f(x)), ... until the same value is returned."
  ([f x]
   (let [fx (f x)]
     (if (= fx x)
       x, (recur f fx))))
  ([f x limit]
   (if (pos? limit)
     (let [fx (f x)]
       (if (= fx x)
         x, (recur f fx (dec limit))))
     (throw (new RuntimeException "Limit exceeded on fixpt")))))


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

(defmacro def-
  "Like clojure.core/def but defines a private var."
  [name & body]
  `(def ^:private ~name ~@body))


(defmacro defatom
  "Defines a var as an atom"
  ([name] `(def ~name (atom nil)))
  ([name val] `(def ~name (atom ~val)))
  ([name docs val] `(def ~name ~docs (atom ~val))))

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


(defn intern-all-str
  "Returns a copy of input with strings interned."
  [data]
  (clojure.walk/postwalk
   (fn [x] (if (string? x) (.intern ^String x) x))
   data))


(defn pmap*
  "Like pmap but eager and does not keep input orders.
Silently discards computations throwing exceptions. Returns same type as input"
  [f xs & {:as opts}]
  (assert (coll? xs))
  (assert (fn? f))
  (let [n       (or (:threads opts) (.availableProcessors (Runtime/getRuntime)))
        storage (or (:storage opts) (atom (empty xs)))]
    (assert (and (integer? n) (pos? n)))
    (assert (instance? clojure.lang.IAtom storage))
    (let [pool (java.util.concurrent.Executors/newFixedThreadPool n)
          bs   (get-thread-bindings)]
      (doseq [x xs]
        (.submit pool ^Runnable (fn []
                                  (push-thread-bindings bs)
                                  (try
                                    (swap! storage conj (f x))
                                    (finally
                                      (pop-thread-bindings))))))
      (.shutdown pool)
      (.awaitTermination pool Long/MAX_VALUE java.util.concurrent.TimeUnit/DAYS)
      (deref storage))))


(defmacro parallel
  "Returns a vector of arguments evaluated in future objects and derefered.
  Example: (parallel (Thread/sleep 1000) (Thread/sleep 1000))
  => [nil nil] in about 1 second."
  [& bodies]
  `(mapv deref [~@(for [x bodies] (list 'clojure.core/future x))]))


(defn filter-remove
  "Returns a vector of two seq with items filtered or removed by f."
  [f xs]
  [(filter f xs) (remove f xs)])

; (pmap* identity (range 100) :key 2)

(defn build-index-store
  "Returns tuple of memoized [value->key key->value] functions."
  []
  (let [a (atom {:key 0})]
    [(fn [v]
       (if-let [[_ k] (find (:val->key @a) v)]
         k
         (locking a
           (if-let [[_ k] (find (:val->key @a) v)]
             k
             (let [da @a
                   k (inc (:key da 0))]
               (reset! a
                       {:val->key (assoc (:val->key da) v k)
                        :key k
                        :key->val (assoc (:key->val da) k v)}))))))
     (fn [key]
       (if-let [[_ v] (find (:key->val @a) key)]
         v
         (throw (IllegalStateException.
                 (str "No value found for key: " key)))))]))

(defn indexed
  "Creates an indexed data structure. You can access current index
  with the .index method call."
  ([s] (indexed s 0))
  ([s ^long init]
   (reify
     clojure.lang.IndexedSeq
     (index [_] init)
     clojure.lang.Counted
     (count [_] (count s))
     clojure.lang.ISeq
     (first [_] (first s))
     (next [_] (if-let [n (next s)]
                 (indexed n (inc init))))
     (more [_] (indexed (.more (seq s)) (inc init)))
     (cons [_ x] (indexed (cons x s) init))
     (empty [_] (indexed (empty s) 0))
     (equiv [this that] (= that s))
     (seq [this] (if (seq s) this))
     clojure.lang.Sequential
     )))

(defn update-in'
  "A faster implementation of update-in."
  [original path fun]
  (let [n  ^int (count path)
        a ^:objects (object-array n)
        last (loop [i 0, obj original]
               (if (< i n)
                 (do (aset a i obj)
                     (recur (inc i) (get obj (nth path i))))
                 (fun obj)))]
    (loop [i (dec n), obj last]
      (if (neg? i)
        obj
        (recur (dec i) (assoc (aget a i) (nth path i) obj))))))


'OK
