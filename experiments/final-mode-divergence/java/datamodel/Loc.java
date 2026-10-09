package chemdetector.datamodel;

/**
 * Side of the robot on which an obstacle has been detected (CD-DM3).
 * Carried by the obstacle event and passed to changeDirection.
 */
public enum Loc {
    left,
    right,
    front
}
