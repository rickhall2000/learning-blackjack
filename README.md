# learning-blackjack

learning-blackjack uses reinforcement learning to develop a strategy for playing blackjack.

## Usage

The core namespace contains functions that can be used to run the learning process.  

There are two atoms in core.clj. 
* explore-exploit-ratio sets the percentage of time that the player will behave randomly, vs the percentage when it will use the strategy that it has dveloped.
* learning-rate sets the amount of weight to be given to the most recent example vs the amount of weight to be given to past experience. A learning rate of 0 means that no learning will occur. 

The go function accpets a number of iterations to run, and returns a vector containing the net result of playing the games (+1 for each win, -1 for each loss, +1.5 for player blackjack)

learning-results accepts 2 parameters, the number of iterations to use for an evaluation and the number of iterations to use for the learning phase.  The function evaluates a random strategy, then evaluates a strategy based on any learning prior to this run of the function. Then the learning rate is set to 0.01 and the explore-exploit-ratio is set to 0.7.  The learning iterations are run, and a score is reported for those.  Finally, the explore-exploit-ratio is set back to 0 and the learning-rate is reduced to 0.001, and another evaluation is run.

The ongoing learning is stored in an atom called history in history.clj. The clear-history function in core.clj will clear out the history.

The show-progress function alternates between running evaluation and learning stages. Each evaluation plays 10,000 games with no learning. Each learning stage plays 100,000 games. History is cleared, and then an evaluation is run. Another evaluation is run after each learning session. Each evluation is printed out, and a vector of the results is returned.


## License

Copyright © 2015 Rick Hall

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
