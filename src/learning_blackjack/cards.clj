(ns learning-blackjack.cards)

(def deck-count 1)
(def cards [:a 2 3 4 5 6 7 8 9 10 :j :q :k])

(defn deck
  []
  (let [card-count (* deck-count 52)
        used (atom #{})]
    (fn []
      (let [new-card (rand-int card-count)]
        (cond (not (contains? @used new-card))
              (do (swap! used conj new-card)
                  (nth cards (mod new-card 13)))
              (= card-count (count @used))
              (do (reset! used #{})
                  (recur))
              :else (recur))))))
