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
    (let [{:keys [score soft?]} (score-hand hand)]
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
    (if-let [result (check-blackjack hands)]
      (blackjack-return result)
      (let [pl-tran (player-turn (h/->player-state
                                   (:player hands)
                                   (get-up-card (:dealer hands)))
                                  deck
                                  history)
            last-tran (last pl-tran)]
        (if (nil? (:result last-tran))
          (let [dealer (dealer-turn (:dealer hands) deck)
                player-score (:score (score-hand (get-in last-tran [:start :player])))
                dealer-score (:score (score-hand dealer))
                final-result (compare player-score dealer-score)]
            {:transactions (replace-end pl-tran
                                        (assoc last-tran :result final-result))
             :dealer-cards dealer})
          {:transactions pl-tran})))))

(defn go
  []
  (-> (h/get-history)
      (play-game)
      (:transactions)
      (h/update-history @learning-rate)))
