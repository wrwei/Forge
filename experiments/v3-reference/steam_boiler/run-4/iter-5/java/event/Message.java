package steam_boiler.event;

/**
 * A message the program transmits at the end of a cycle (SB-DM8, SB-V3):
 * either a command issued by the controller ({@link OutputEvent}) or a
 * failure/repair protocol message ({@link ProtocolMessage}).
 */
public interface Message {
}
