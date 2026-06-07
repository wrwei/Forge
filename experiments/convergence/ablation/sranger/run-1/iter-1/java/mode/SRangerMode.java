package sranger.mode;

/**
 * Operating modes of the SRanger controller. {@code Moving} is the
 * initial mode; {@code Halted} is the absorbing shutdown mode entered
 * on the operator endTask event (the specification's terminal "Final"
 * mode, renamed because the formal-model pipeline reserves the state
 * name {@code Final}).
 */
public enum SRangerMode {
    Moving,
    Turning,
    Halted
}
