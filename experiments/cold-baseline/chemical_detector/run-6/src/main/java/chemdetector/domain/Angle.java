package chemdetector.domain;

/**
 * Turn direction relative to the robot body (CD-DM2). Used as the operand of
 * the move operation and as the payload of the turn event.
 */
public enum Angle {
    Left,
    Right,
    Back,
    Front
}
