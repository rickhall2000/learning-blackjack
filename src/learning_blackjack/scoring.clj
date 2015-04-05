(ns learning-blackjack.scoring)

(def face-card #{:k :q :j})

(defn score-card
  [card]
  (cond (face-card card) 10
        (= :a card) 1
        :default card))

(defn score-hand
  [hand]
  (let [raw-score (reduce + (map score-card hand))]
    (if (and (>= 11 raw-score)
             (some (partial = :a) hand))
      {:score (+ 10 raw-score)
       :soft? true}
      {:score raw-score})))

(defn blackjack?
  [cards]
  (and (= 2 (count cards))
       (= 21 (:score (score-hand cards)))))

(defn check-blackjack
  [{:keys [dealer player]}]
  (cond (and (blackjack? dealer) (blackjack? player)) :both
        (blackjack? dealer) :dealer
        (blackjack? player) :player
        :else nil))

(defn is-loss?
  [hand]
  (do
    (> (:score (score-hand hand)) 21)))
