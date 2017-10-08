(ns erdos.fun.tables
  "Helpers for joining and displaying data tables")

(defmacro ^:private unique-merge [a b] `(set (into ~a ~b)))

(defn full-join-merge [key-fn1 map-seq1, key-fn2 map-seq2]
  (let [key1->map1 (group-by key-fn1 map-seq1)
        key2->map2 (group-by key-fn2 map-seq2)]
    (for [k  (unique-merge (keys key1->map1) (keys key2->map2))
          m1 (key1->map1 k [{}]),
          m2 (key2->map2 k [{}])]
      (merge m1 m2))))

(defn inner-join-merge [key-fn1 map-seq1, key-fn2 map-seq2]
  (let [key1->map1 (group-by key-fn1 map-seq1)
        key2->map2 (group-by key-fn2 map-seq2)]
    (for [k  (unique-merge (keys key1->map1) (keys key2->map2))
          m1 (key1->map1 k),
          m2 (key2->map2 k)]
      (merge m1 m2))))


(defn full-join-nested [key-fn1 map-seq1, key-fn2 map-seq2]
  (let [key1->map1 (group-by key-fn1 map-seq1)
        key2->map2 (group-by key-fn2 map-seq2)]
    (for [k  (unique-merge (keys key1->map1) (keys key2->map2))
          m1 (key1->map1 k [nil]),
          m2 (key2->map2 k [nil])]
      {:left m1, :right m2})))


(defn inner-join-nested [key-fn1 map-seq1, key-fn2 map-seq2]
  (let [key1->map1 (group-by key-fn1 map-seq1)
        key2->map2 (group-by key-fn2 map-seq2)]
    (for [k  (unique-merge (keys key1->map1) (keys key2->map2))
          m1 (key1->map1 k),
          m2 (key2->map2 k)]
      {:left m1, :right m2})))


(defn full-join-vec [tab & joins]
  (reduce (fn [result [k1 k2 rows]]
            (for [row (full-join-nested (comp k1 peek) result k2 rows)]
              (conj (:left row) (:right row))))
          (if (seq tab) (map vector tab) []) (partition 3 joins)))


(defn inner-join-vec [tab & joins]
  (reduce (fn [result [k1 k2 rows]]
            (for [row (inner-join-nested (comp k1 peek) result k2 rows)]
              (conj (:left row) (:right row))))
          (if (seq tab) (map vector tab) []) (partition 3 joins)))


                                        ; HTML

(defn- ->elem [tag content]
  (cond (vector? content)
        (if (= tag (first content))
          (if (map? (second content))
            content
            (vec (list* tag {} (nnext content))))
          [tag {} content])

        (seq? content)
        (vec (list* tag {} content))

        (or (nil? content) (number? content)
            (true? content) (false? content) (string? content))
        [tag {} content]

        :otherwise
        (assert false (str "Unexpected content " (pr-str content)))))


(defn- ->tr [x] (->elem :tr x))
(defn- ->td [x] (->elem :td x))

;; todo: handle row span
(defn render-table [{:keys [cols rows merge-cols]}]
  ;; cols: {} col map list
  ;; rows: row list
  (let []
    [:table
     [:thead
      [:tr (for [col cols]
             [:th {:colspan (:span col 1),
                   :style   (:style col "")}
              (:title col)])]]
     [:tbody
      (for [row rows]
        (->tr (for [c row] (->td c))))]]))

(defn html-merge-col
  "Returns a hiccup-style table structure with the nth columns joined"
  [merge-col-idx rows]
  (assert (integer? merge-col-idx))
  (let [rows         (for [[tr at & bs] (map ->tr rows)]
                       (apply vector tr at (map ->td bs)))
        row-nth      #(nth % (+ merge-col-idx 2))
        row-del-nth  #(assoc % (+ merge-col-idx 2) nil)
        row-set-span #(assoc-in %1 [(+ 2 merge-col-idx) 1 :row-span] %2)]
    (mapcat (fn [row-part]
              (if (next row-part)
                (list* (row-set-span (first row-part) (count row-part))
                       (map row-del-nth (next row-part)))
                row-part))
            (partition-by row-nth rows))))

(comment

  (html-merge-col 0 '[(1 1 1) (2 1 2) (3 2 3) (4 2 4) (5 3 5)])
  (html-merge-col 1 '[(1 1 1) (2 1 2) (3 2 3) (4 2 4) (5 3 5)])
  (html-merge-col 2 '[(1 1 1) (2 1 2) (3 2 3) (4 2 4) (5 3 5)])

  )


(defn html-merge-cols [merge-col-idxs rows]
  ;; addot indexu oszlopokat osszevon, ha azonos a tartalmuk (mindennel egyutt.)
  (reduce html-merge-col rows merge-col-idxs))

;(instance? String nil)

;;(defn draw-table [tab])
;;
(comment

  (html-merge-cols
   2 ;; merge on first two rows
   row)


  (render-table
   {:cols [{:title "" :style "color:red" :span 5}]
    :rows []}
   )


  (render-table
   {:col-groups [[1 {:style "background:red"}]
                 [2 {:style "backgound:yellow"}]
                 [3 {:style "background: silver"}]]
    :titles [[1 "Elso tabla"] [2 "Masodik tabla"] [3 "Harmadik tabla"]]
    :subtitles ["Oszlop1" "Oszlop2" "Oszlop3" "Oszlop4"]

    ;; vagy: rows-html
    :rows (full-join-vec
           table1
           :id
           :tab-id
           table2
           :col-id
           :id
           table3)})

  )


(comment
  (full-join-nested :a '({:a 1} {:a 2} {:a 3}), :b nil)
  (full-join-nested :a nil, :b '({:a 1} {:a 2} {:a 3}))

  (full-join-vec nil)

  (full-join-vec
   nil ;'({:a 1 :b 1} {:a 1 :b 2} {:a 2 :b 1.1} {:a 3 :b :b})
   :a :aa '({:aa 1 :ab 1} {:aa 2 :ab 2}))

  (full-join-vec
   nil ;'({:a 1 :b 1} {:a 1 :b 2} {:a 2 :b 1.1} {:a 3 :b :b})
   :a :aa '({:aa 1 :ab 1} {:aa 2 :ab 2})
   :ab :k '({:k 1 :kk 1} {:k 1 :kk 1.1})
   )


  (full-join-vec
   '({:a 1 :b 1} {:a 1 :b 2} {:a 2 :b 1.1} {:a 3 :b :b})
   :a :aa '({:aa 1 :ab 1} {:aa 2 :ab 2})
   :ab :k '({:k 1 :kk 1} {:k 1 :kk 1.1} {:k nil}))

  (full-join-vec
   nil ;'({:a 1 :b 1} {:a 1 :b 2} {:a 2 :b 1.1} {:a 3 :b :b})
   :a :aa '({:aa 1 :ab 1} {:aa 2 :ab 2})
                                        ;:ab :k '({:k 1 :kk 1} {:k 1 :kk 1.1} {:k nil})
   )

  )
