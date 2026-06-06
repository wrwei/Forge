The Chemical Detector is an autonomous mobile robot whose mission is to locate the source of a target chemical (e.g. a gas leak) in an unknown environment that may contain obstacles. It operates without human input from start to finish.

The robot is equipped with three kinds of sensors. A gas-sensor array provides multi-directional concentration readings: each reading is a sequence of (chemical, intensity) pairs, where the position in the sequence corresponds to a sensing direction. A forward-facing obstacle detector reports the side (left, right, or front) at which an obstacle has been encountered. An odometer reports the cumulative distance the robot has travelled.

The robot can perform two kinds of motion. It can move with a configurable linear velocity in one of four directions relative to its body (Left, Right, Back, Front). It can also perform a random walk — bounded (short) or unbounded — when no specific direction has been chosen.

A target chemical is considered located when the peak intensity of a current reading is at or above a configured intensity threshold. When this happens the robot must halt and emit a single signal indicating success.

Behaviour at runtime alternates between four high-level activities: searching (random walk while waiting for the next reading), classifying the most recent reading, navigating toward the strongest detected signal when the chemical is present but below the threshold, and avoiding obstacles encountered during travel. Obstacle avoidance includes a recovery sub-behaviour for the case where the robot becomes stuck (i.e. cannot make sufficient progress between successive obstacles within a configured time window).

Each gas-reading classification yields one of two outcomes: no target chemical detected (continue searching) or target chemical detected (decide whether to home in or to halt, based on whether the peak intensity is above the threshold).
