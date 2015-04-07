(ns learning-blackjack.game
  (:require [learning-blackjack.cards :refer [deck deal-hand get-up-card]]
            [learning-blackjack.scoring :as s]))

(defrecord action-result
  [start action result])

(defrecord player-state
  [player dealer])

(defn replace-end
  [vec new]
  (let [end (dec (count vec))]
    (assoc vec end new)))

(defn legal-moves
  [player-state]
  [:hit :stand])

(defn random-action
  [player-state]
  (rand-nth (legal-moves player-state)))

(defn take-action
  [action player deck]
  (if (= action :hit)
    (conj player (deck))
    nil))

(defn player-turn
  [player-state deck player-action]
  (loop [player player-state transitions []]
    (let [action (player-action player)
          new-hand (take-action action (:player player) deck)]
      (cond
       (nil? new-hand) (conj transitions (->action-result player action new-hand))
       (s/is-loss? new-hand) (conj transitions (->action-result player action -1))
       :default (let [next-state (assoc player :player new-hand)]
                  (recur next-state
                         (conj transitions
                               (->action-result player action new-hand))))))))

(defn dealer-turn
  [hand hit]
  (loop [hand hand]
    (let [{:keys [score soft?]} (s/score-hand hand)]
      (cond (< score 17) (recur (conj hand (hit)))
            (and soft? (= score 17)) (recur (conj hand (hit)))
            :default hand))))

(defn play-game
  ([] (play-game random-action))
  ([player-action]
      (let [deck (deck)
            hands {:dealer (deal-hand deck)
                   :player (deal-hand deck)}]
        (if-let [result (s/check-blackjack hands)]
          (s/blackjack-return result)
          (let [pl-tran (player-turn (->player-state
                                      (:player hands)
                                      (get-up-card (:dealer hands)))
                                     deck
                                     player-action)
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
               :points -1}))))))
