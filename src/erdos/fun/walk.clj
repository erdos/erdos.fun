(ns erdos.fun.walk)


(defn quote?
  "Returns true iff argument is a quoted form."
  [x] (and (seq? x) (= (first x) 'quote)))


(defn scalar?
  "Returns if argument is not a composite value."
  [xs]
  (cond
   (string? xs)  true
   (number? xs)  true
   (nil? xs)     true
   (= true xs)   true
   (= false xs)  true
   (symbol? xs)  false
   (keyword? xs) true
   (instance? java.util.regex.Pattern xs) true
   :else false))

(defn data-coll?
  "Returns true iff argument is collection but not seq"
  [xs]
  (and (coll? xs) (not (seq? xs))))


(defn- data?-walker
  [xs]
  (loop [[x & xs] xs
         buf      nil]
    (cond
     (list? x)   (if (= 'quote (first x)) true false)
     (coll? x)   (recur xs (cons (seq x) buf))
     (scalar? x)   (if (empty? xs)
                         (if (empty? buf)
                           true
                           (recur (first buf)
                                  (next buf)))
                         (recur xs buf))
     :else        false)))


(defn data?
  "Returns true if argument evaluates to itself"
  [xs]
  (cond
   (seq? xs)  (if (= 'quote (first xs)) true false)
   (coll? xs) (data?-walker (seq xs))
   :else      (scalar? xs)))


(defn code-seq
  "Returns a lazy seq of code itms in expression"
  [root]
  (tree-seq
   (fn [x] (and (coll? x)
               (not (quote? x))))
   seq root))


(defn code-seq-children
  [root] (remove data-coll? (code-seq root)))


(defn code-seq-calls
  "Returns all function/macro calls from given expression"
  [root] (filter seq? (code-seq root)))


(def ^:private macro-whitelist0
  "set of macro names that are safe to expand"
  '#{-> ->> ..
     as-> comment
     cond cond-> cond->> condp
     delay doseq dotimes doto
     sync dosync
     for future
     if-let if-not if-some
     let fn letfn
     some-> some->>
     when when-first when-let when-not
     when-some while
     memfn
     with-redefs})

(def macro-whitelist-toplevel
  "Set of top level macro expression names"
  '#{defn defn- defonce defmacro defprotocol})

(def ^:dynamic *macro-whitelist*
  (set (concat macro-whitelist0
               (for [s macro-whitelist0]
                 (symbol "clojure.core" (name s))))))


(defn safe-macroexpand-1
  "See: safe-macroexpand"
  [expr]
  (if-not (seq? expr)
    expr
    (let [f (first expr)]
      (if (and (symbol? f)
               (contains? *macro-whitelist* f))
        (macroexpand-1 expr) expr))))


(defn safe-macroexpand
  "Like clojure.core/macroexpand but does not expand
  quoted forms."
  [form]
  (let [ex (safe-macroexpand-1 form)]
    (if (identical? ex form)
      form (recur ex))))


(defn safe-macroexpand-all
  "See: safe-macroexpand"
  [form]
  (cond
   (vector? form) (mapv safe-macroexpand-all form)
   (seq? form)    (if (= 'quote (first form)) form
                      (safe-macroexpand (map safe-macroexpand-all form)))
   (map? form)    (into {} (for [[k v] form]
                             [(safe-macroexpand-all k)
                              (safe-macroexpand-all v)]))
   (set? form)    (set (map safe-macroexpand-all form))
   :else form))


(comment
  ;; TODO: code walker functionality
  (def ^:dynamic *walk-vars* #{})
  (def ^:dynamic *walk-bindings* {})
  (defn walk-code-fn*)
  (defn walk-code-if)
  (defn walk-code-case)
  (defn walk-code-let)
  (defn walk-code-letfn)
  (defn walk-code-try-catch)
  (defn walk-code-loop)

  (defn walk-code
    [letfn closure-fn expr-fn root]
    (let [root (safe-macroexpand-all root)
          walk-fn* (fn [form]
                     (binding [*walk-vars*
                               (if (symbol? (second form))
                                 (conj (second form) *walk-vars*)
                               *walk-vars*)]
                     (doseq [[args & body] (filter seq? form)]
                       (binding [*walk-vars* (apply conj *walk-vars* args)]
                         (doseq [b body]
                           (expr-fn b))))))
        walker (fn [w])]
    (walker root)))

)

'OK
