(ns erdos.fun.ring)

(defonce routes {})

(defmacro defreq [method url [request success error] body]
  (assert ('#{GET POST PUT DELETE} method))
  (assert (string? url))
  (let [method (keyword (.toLowerCase (name method)))
        r (for [itm (.split (str url) "/"), :when (seq itm)]
            (if (.startsWith (str itm) ":")
              (keyword (.substring (str itm) 1))
              (str itm)))
        assoc-path (concat [method] (map (fn [a] (if (keyword? a) :* a)) r) [:end])
        ks (filter keyword? r)]
    `(alter-var-root #'routes assoc-in ~(vec assoc-path)
                     {:fn (fn [~request ~success ~error] ~body)
                      :ks ~(vec ks)})))

(defn req-handler [req]
  (let [url (remove empty? (.split (str (:uri req)) "/"))]
    (loop [url url
           routes (get routes (:request-method req))
           params []]
      (if-let [[u & url] (seq url)]
        (cond
          (contains? routes u) (recur url (get routes u) params)
          (contains? routes :*) (recur url (get routes :*) (conj params u)))
        (if-let [end (:end routes)]
          {:handler (:fn end)
           :route-params (zipmap (:ks end) params)})))))

(def not-found {:status 404 :body "Route Not Found"})

;; call it like:
;; (req-handler {:uri "/api/projects" :request-method :get})
;; (req-handler {:uri "/api/query/ABCDEF/do" :request-method :get})
(defn handle-routes
  ([req success error]
   (if-let [h (req-handler req)]
     ((:handler h) (assoc req :route-params (:route-params h)) success error)
     (success not-found)))
  ([request]
   (if-let [h (req-handler req)]
     ((:handler h) (assoc req :route-params (:route-params h)))
     not-found)))

(defn wrap-add-async
  "Returns a new ring handler with an asyn 3-arity added."
  [handler]
  (fn
    ([request] (handler request))
    ([request response raise]
     (try
       (response (handler request))
       (catch Throwable t
         (raise t))))))

(defn wrap-add-sync
  "Returns a new ring handler with synchronous 1-arity added."
  [handler]
  (fn ([request]
       (let [p (promise)]
         (try (handler request
                       (partial deliver p)
                       (partial deliver p))
              (catch Throwable t (deliver p t)))
         @p))
    ([request response raise] (handler request response raise))))
