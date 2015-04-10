(ns learning-blackjack.learning
  (require [learning-blackjack.game :as g]
           [learning-blackjack.history :as h]))

(def explore-exploit-ratio (atom 0.5))
(def learning-rate (atom 0.1))

(defn exploit?
  []
  (> (rand) @explore-exploit-ratio))

(defn player-action
  [player-state]
  (if (exploit?)
    (h/get-best-action player-state (h/get-history))
    (rand-nth (g/legal-moves player-state))))

(defn clear-history
  []
  (reset! h/history {}))

(defn go
  ([] (go 1))
  ([game-count]
     (loop [game 1 current-score 0]
       (let [history (h/get-history)
             results (g/play-game player-action)
             points (if (map? results)
                      (:points results)
                      results)]
         (do
           (when (map? results)
             (h/update-history (:transactions results) @learning-rate))
           (if (< game game-count)
             (recur (inc game) (+ current-score points))
             [game current-score]))))))
