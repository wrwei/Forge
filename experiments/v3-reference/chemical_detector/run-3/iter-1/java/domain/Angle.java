package chemical_detector.domain;

/** A turn direction relative to the robot body (CD-DM2). */
public enum Angle {
    /** Turn to the left of the body. */
    Left,
    /** Turn to the right of the body. */
    Right,
    /** Turn around. */
    Back,
    /** Keep facing forward. */
    Front
}
