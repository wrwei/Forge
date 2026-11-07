package chemical_detector.domain;

/**
 * The side of the robot on which an obstacle has been detected (CD-DM3);
 * carried by the obstacle event and passed to changeDirection.
 */
public enum Loc {
    left,
    right,
    front
}
