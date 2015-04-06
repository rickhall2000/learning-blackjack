(ns learning-blackjac.learning
  (require [learning-blackjack.game]
           [learning-blackjack.history :as h]))

(comment
(def explore-exploit-ratio (atom 0.5))
(def learning-rate (atom 0.1))

(defn exploit?
  []
  (> (rand) @explore-exploit-ratio))



(defn player-action
  [player-state history]
  (if (exploit?)
    (h/get-best-action player-state history)
    (rand-nth (legal-moves player-state))))

(defn clear-history
  []
  (reset! h/history {}))

(defn learn!
  []
  (reset! learning-rate 0.01)
  (reset! explore-exploit-ratio 0.7))

(defn exploit!
  []
  (reset! learning-rate 0.00)
  (reset! explore-exploit-ratio 0.00))

(defn random-strategy!
  []
  (reset! learning-rate 0.00)
  (reset! explore-exploit-ratio 1.00))


(defn go
  ([] (go [1]))
  ([game-count]
     (loop [game 0 current-score 0]
       (let [history (h/get-history)
             results (play-game history)
             points (if (map? results)
                      (:points results)
                      results)]
         (do
           (when (map? results)
             (h/update-history (:transactions results) @learning-rate))
           (if (< game game-count)
             (recur (inc game) (+ current-score points))
             [game current-score]))))))

(defn evaluate-strategy
  [iterations message]
  (let [[n score] (go iterations)]
    (println message (* 100 (/ score n)))
    [n score]))

(defn learning-results
  [eval-n learn-n]
  (random-strategy!)
  (evaluate-strategy eval-n "Random results, no learning")
  (exploit!)
  (evaluate-strategy eval-n "Strategy before learning")
  (learn!)
  (evaluate-strategy learn-n "Results during learning")
  (exploit!)
  (evaluate-strategy eval-n "Strategy after learning"))

(defn show-progress
  [tries]
  (clear-history)
  (exploit!)
  (let [[n score] (evaluate-strategy 10000 "Initial ")]
    (loop [results [(/ score n)] i 0]
      (if (= tries i)
        results
        (do
          (learn!)
          (go 100000)
          (exploit!)
          (let [[n score] (evaluate-strategy 10000 (str i " "))]
            (recur (conj results (/ score n)) (inc i))))))))
)
