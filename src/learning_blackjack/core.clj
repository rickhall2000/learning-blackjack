(ns learning-blackjack.core
  (:require [learning-blackjack.learning :refer [go learning-rate explore-exploit-ratio clear-history]]
            [incanter.charts :as chart]
            [incanter.core :refer [view]]))

(defn learn!
  []
  (reset! learning-rate 0.02)
  (reset! explore-exploit-ratio 0.7))

(defn exploit!
  []
  (reset! learning-rate 0.00)
  (reset! explore-exploit-ratio 0.00))

(defn random-strategy!
  []
  (reset! learning-rate 0.00)
  (reset! explore-exploit-ratio 1.00))

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

(defn ema
  [data rate]
  (into [] (reductions (fn [a b] (+ (* (- 1 rate) a) (* rate b)))  data)))

(defn graph-progress
  [n]
  (let [results (show-progress n)
        x-axis (range (count results))
        c (chart/scatter-plot x-axis results)
        blah (ema results 0.1)]
    (println blah)
    (view (chart/add-lines c x-axis (ema results 0.1)))
    results))
