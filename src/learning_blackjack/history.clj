(ns learning-blackjack.history)

(def +new-state+ {:hit 0 :stand 0 :double 0 :split 0})

(def history (atom {}))

(defn lookup-history
  [player-state history]
  (get history player-state +new-state+))

(defn best-q
  [q-vals]
  (into (sorted-map-by
         (fn [key1 key2]
           (compare [(get q-vals key2) key2]
                    [(get q-vals key1) key1])))
        q-vals))

(defn get-best-action
  [player-state history]
  (-> (lookup-history player-state history)
      (best-q)
      (ffirst)))

(defn get-state-score
  [player-state history]
  (-> (lookup-history player-state history)
      (best-q)
      (first)
      (second)))

(defn get-history
  []
  @history)

(defn update-item
  [{:keys [start action result]} learning-rate]
  (let [prior (lookup-history start @history)
        result (if (vector? result)
                 (get-state-score (assoc start :player result) @history)
                   result)
        expected (get prior action)
        new-score (+ (* (- 1 learning-rate) expected)
                     (* learning-rate result))
        new-qs (assoc prior action new-score)]
    (swap! history assoc start new-qs)))

(defn update-history
  [transactions learning-rate]
  (doseq [t transactions]
    (update-item t learning-rate))
  @history)
