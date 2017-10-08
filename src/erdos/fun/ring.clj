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

(defonce default-routes {})


(defn- split-url [url]
  (cond (string? url)     (.split (str url) "/")
        (sequential? url) (doall (map name url))
        :else             (assert false "unknown type")))


(defn make-route-path [method url handler]
  (assert ('#{GET POST PUT DELETE OPTIONS ANY} method))
  (assert (string? url))
  (let [method (keyword (.toLowerCase (name method)))
        r (for [itm (split-url url)
                :when (seq itm)]
            (if (.startsWith (str itm) ":")
              (keyword (.substring (str itm) 1))
              (str itm)))
        assoc-path (concat [method]
                           (for [a r] (if (keyword? a) :* a))
                           [:end])
        parameter-names (filter keyword? r)]
    {:path       (vec assoc-path)
     :handler    handler
     :parameters (vec parameter-names)}))


(defn assoc-route [routes-map method url handler]
  (let [x (make-route-path method url handler)]
    (assoc-in routes-map (:path x) x)))


(defn assoc-route! [routes-map-atom method url handler]
  (swap! routes-map-atom assoc-route method url handler))


(defmacro defroute
  ([method url handler]
   `(defroute default-routes ~method ~url ~handler))
  ([routes method url handler]
   (let [x (make-route-path method url handler)]
     `(alter-var-root ~routes assoc-route ~method ~url ~handler))))


(defmacro defroutes [var-name & bodies]
  (assert (symbol? var-name))
  (let [bs (partition-all 3 bodies)]
    (list*
     'do
     `(def ~var-name {})
     (for [[method url handler] (partition-all 3 bodies)]
       `(defroute ~var-name ~method ~url ~handler)))))


(defn route-handler-raw [routes method uri]
  (loop [url (remove empty? (.split (str uri) "/"))
         routes (get routes method)
         params []]
    (if-let [[u & url] (seq url)]
      (if-let [ru (get routes u)]
        (recur url ru params)
        (if-let [r* (get routes :*)]
          (recur url r* (conj params u))
          (:end routes))))))


(defn route->handler
  ([req] (route->handler default-routes req))
  ([routes req]
   (or (route-handler-raw default-routes (:request-method req) (:uri req))
       (route-handler-raw default-routes :any (:uri req)))))


(defn ring-handler [routes]
  ([] (ring-handler default-routes))
  ([routes]
   (fn
     ([req echo raise]
      (let [handler (route->handler req)]
        (handler req echo raise)))
     ([req]
      (let [handler (route->handler req routes)]
        (handler req))))))


(defn all-handlers
  ([] (all-handlers default-routes))
  ([routes] ()))


(defn all-methods
  ([] (all-methods default-routes))
  ([routes] ()))


(defn all-routes
  ([] (all-routes default-routes))
  ([routes] ()))


(defn handler->routes
  ([value mapping] (handler->routes default-routes value mapping))
  ([routes value mapping]
   ;; visszaszedi ami kell
   ))


(defn visualize-handlers
  ;; renders a hiccup table visualization of the rules
  ;; can be used to visualize auth rules or etc
  [route handler->html]
  [:div
   [:div.route-item
    [:div.route-tag "route-item"]
    [:div.route-body "route-rest"]]])
