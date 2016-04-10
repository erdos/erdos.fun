(ns erdos.fun.format
 "experimental string formatting macro")

(defn- indices-of
  ([s p i]
   (assert (string? s))
   (assert (string? p))
   (assert (integer? i))
   (let [j (.indexOf s p i)]
     (if (not (neg? j))
       (cons j (lazy-seq (indices-of s p (inc j)))))))
  ([s p] (lazy-seq (indices-of s p 0))))

(defn split-at-str [s i]
  (cond
   (integer? i)
   (if (<= 0 i (dec (count s)))
     [(.substring s 0 i) (.substring s i)]
     [s nil])
   (string? i)
   (let [j (.indexOf s i)]
     (if (neg? j)
       [s nil]
       [(.substring s 0 j)
        (.substring s (+ j (count i)))]))))

;; (split-at-str "abcdefg" 2)
;; (split-at-str "abcdefg" "c")
;; (split-at-str "abcdef" "cd")

; (indices-of "2% 1 2 3 % %%" "%")


(defmacro template [p & itms]
  "Given a string pattern `p`, replaces all occurences of character '%' with an item from `itms`."
  (assert (string? p) (str "no str: " p))
  (let [ls (.split p "%")
        c (count (indices-of p "%"))]
    (assert (= c (count itms))
            (str "Argument count does not match"))
    `(str ~@(interleave ls itms)
          ~(if (not= (count ls) (count itms))
             (last ls)))))

(defmacro template-named [p m]
  (assert (string? p))
  (let [ms (gensym "m")
        str->get (fn [b] `(~(keyword b) ~ms))
        pts (fn f [s]
              (when (seq s)
                (let [[a b] (split-at-str s "{")]
                  (if (seq b)
                    (let [[b c] (split-at-str b "}")]
                      (conj (lazy-seq (f c))
                            (str->get b) a))
                    [a]))))]
    `(let [~ms ~m]
       (str ~@(pts p)))))

;; (template-named "asad{a}bbbb{b}cccc{c}" {})
;; (template-named "asad{a}bbbb{b}cccc{c}ddd" {})
;; (template-named "asad{a}bbbb{b}cccc{c}" {:a 1 :b 2 :c 23})

;; (template "a % sdf % % x" 1 2 3)
;; (template "% sdf % %" 1 2 3)
