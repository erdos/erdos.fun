(ns erdos.fun.docs
  "Generating markdown documentation from source.")

;; TODO: create boot/lein task to auto gen doc.

(def project-dir
  (.getCanonicalPath
   (clojure.java.io/file ".")))

(def output-dir (str project-dir "/" "doc"))

(defn lines [& args]
  (clojure.string/join "\n\n" (filter some? (flatten args))))

(defn words [& args]
  (clojure.string/join ", " (filter some? (flatten args))))

(defn link
  ([name id href] (format "[%s][%s](%s)" name id href))
  ([name href] (format "[%s](%s)" name href)))

(defn list-namespaces
  "Get a list of project namespaces."
  ([dir]
   (let [dir  (clojure.java.io/file dir)
         dw   (-> dir .getCanonicalPath count inc)
         clj? #(.endsWith (.getName %) ".clj")
         fs   (filter clj? (file-seq dir))
         rep! #(-> % (.getCanonicalPath)
                   (.substring dw)
                   (.replaceAll "/" ".")
                   (.replaceAll "_" "-")
                   (.replaceAll ".clj$" "")
                   (symbol))
         lo!  #(try (require %)
                    (the-ns %)
                    (catch Exception _))]
     (doall (keep lo! (map rep! fs)))))
  ([]
   (let [f #(-> % clojure.java.io/file .getCanonicalPath)]
     (mapcat (comp list-namespaces f)
             ["src" "test"]))))

;; (list-namespaces) -> ns from proj list.

;; (require ')
;; (find-ns 'erdos.fun-walk-test)

(defn var->type
  "Returns a human readable string containing information on the type of argument."
  [v]
  (let [vd (deref v)]
    (cond (-> v meta :macro) "macro"
          (fn? vd)   "function"
          (-> v meta :dynamic) "dynamic"
          (-> v meta :test)    "test"
          (string? vd) "string"
          (vector? vd) "vector"
          (map? vd) "map"
          (list? vd) "list"
          (number? vd) "number"
          ({true 1 false 1} vd) "bool"
          (nil? vd) "nil"

         :otherwise "var"
         )))

(defn process-var-lines
;  "Creates a"
  [v]
  [(format "## _%s_ *%s* " (var->type v) (-> v meta :name))
   (when-let [as (-> v meta :arglists)]
     (str
      "_arguments:_ "
      (words (for [a as] (str "`" a "`")))))
   (-> v meta :doc)])

(defn process-ns-lines
  [ns]
  (let [ns (the-ns ns)
        pubs (vals (ns-publics ns))
        mets (mapv meta pubs)]
    [(link "Return to index" "index.md") ;; home
     (str "# namespace " (.getName ns))
     ; (str "files: " (words (seq (set (map (comp :file meta) pubs)))))
     (str (-> ns meta :doc))
     (str "__public vars:__ ")
     (map process-var-lines (vals (ns-publics ns)))
     \newline]))

(defn process-ns [ns]
  (let [ns (the-ns ns)]
    (process-ns-lines ns)
    {:ns (.getName ns)
     :lines (flatten (process-ns-lines ns))
     :file  (-> ns .getName (str ".md"))}
    ))

(defn process-index-lines [bib]
  ["# Index"
   "__namespaces:__"
   (for [ns bib]
     [(link (:ns ns) (str (:ns ns) ".md"))
      (str " " (words
                 (for [[k v]
                       (ns-publics (the-ns (:ns ns)))] (str k))))])
   ]
  )

(defn process-index
  "Returns a map containin info to build an index.md file.
  Argument `bib`: seq of values returned from `process-ns`
  Returning map keys:
  - file: index file name
  - lines: seq of lines to insert to file"
  [bib]
  {:file "index.md"
   :lines (flatten (process-index-lines bib))})
;; (mapv meta (vals (ns-publics (the-ns 'erdos.fun))))
; (process-ns (first (list-namespaces)))

(defn process
  ([] (process project-dir))
  ([root]
   (let [nss (mapv process-ns (list-namespaces))
         ind (process-index nss)]
     (doseq [n nss]
       (spit (str output-dir "/" (:file n))
             (lines (:lines n))))
     (spit (str output-dir "/" (:file ind))
           (lines (:lines ind)))
     :ok)))

;; (meta #'erdos.fun.docs/process)
;; (println (process))


'OK
