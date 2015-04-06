(ns learning-blackjack.core
  (:require [learning-blackjack.cards :refer [deck]]
            [learning-blackjack.scoring :as s]
            [learning-blackjack.history :as h]))

(def explore-exploit-ratio (atom 0.5))
(def learning-rate (atom 0.1))

(defn exploit?
  []
  (> (rand) @explore-exploit-ratio))

(defn legal-moves
  [player-state]
  [:hit :stand])

(defn player-action
  [player-state history]
  (if (exploit?)
    (h/get-best-action player-state history)
    (rand-nth (legal-moves player-state))))

(defn take-action
  [action player deck]
  (if (= action :hit)
    (conj player (deck))
    nil))

(defn player-turn
  [player-state deck history]
  (loop [player player-state transitions []]
    (let [action (player-action player history)
          new-hand (take-action action (:player player) deck)]
      (cond
       (nil? new-hand) (conj transitions (h/->action-result player action new-hand))
       (s/is-loss? new-hand) (conj transitions (h/->action-result player action -1))
       :default (let [next-state (assoc player :player new-hand)]
                  (recur next-state
                         (conj transitions
                               (h/->action-result player action
                                                (h/get-state-score next-state history)))))))))

(defn dealer-turn
  [hand hit]
  (loop [hand hand]
    (let [{:keys [score soft?]} (s/score-hand hand)]
      (cond (< score 17) (recur (conj hand (hit)))
            (and soft? (= score 17)) (recur (conj hand (hit)))
            :default hand))))

(defn deal-hand
  [deck]
  [(deck) (deck)])

(def get-up-card first)

(def blackjack-return
  {:both 1
   :dealer -1
   :player 1.5})

(defn replace-end
  [vec new]
  (let [end (dec (count vec))]
    (assoc vec end new)))

(defn play-game
  [history]
  (let [deck (deck)
        hands {:dealer (deal-hand deck)
               :player (deal-hand deck)}]
    (if-let [result (s/check-blackjack hands)]
      (blackjack-return result)
      (let [pl-tran (player-turn (h/->player-state
                                   (:player hands)
                                   (get-up-card (:dealer hands)))
                                  deck
                                  history)
            last-tran (last pl-tran)]
        (if (nil? (:result last-tran))
          (let [dealer (dealer-turn (:dealer hands) deck)
                player-score (:score (s/score-hand (get-in last-tran [:start :player])))
                dealer-score (:score (s/score-hand dealer))
                final-result (compare player-score dealer-score)]
            {:transactions (replace-end pl-tran
                                        (assoc last-tran :result final-result))
             :dealer-cards dealer
             :points final-result})
          {:transactions pl-tran
           :points -1})))))

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


(defn learning-results
  [eval-n learn-n]
  (reset! learning-rate 0.00)
  (reset! explore-exploit-ratio 1.00)
  (let [[n score] (go eval-n)]
    (println "Random results, no learning" (* 100 (/ score n))))
  (reset! learning-rate 0.00)
  (reset! explore-exploit-ratio 0.00)
  (let [[n score] (go eval-n)]
    (println "Strategy before learning" (* 100 (/ score n))))
  (reset! learning-rate 0.01)
  (reset! explore-exploit-ratio 0.7)
  (let [[n score] (go learn-n)]
    (println "Results during learning" (* 100 (/ score n))))
  (reset! explore-exploit-ratio 0.00)
  (reset! learning-rate 0.001)
  (let [[n score] (go eval-n)]
    (println "Results after learning" (* 100 (/ score n)))))

(defn clear-history
  []
  (reset! h/history {}))


(defn show-progress
  [tries]
  (clear-history)
  (reset! learning-rate 0.00)
  (reset! explore-exploit-ratio 0.00)
  (let [[n score] (go 10000)]
    (loop [results [(/ score n)] i 0]
      (if (= tries i)
        results
        (do
          (println (str i " " (last results)))
          (reset! learning-rate 0.01)
          (reset! explore-exploit-ratio 0.70)
          (go 100000)
          (reset! learning-rate 0.00)
          (reset! explore-exploit-ratio 0.00)
          (let [[n score] (go 10000)]
            (recur (conj results (/ score n)) (inc i))))))))
