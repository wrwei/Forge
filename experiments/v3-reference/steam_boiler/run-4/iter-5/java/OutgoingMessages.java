package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.event.Message;

/**
 * The messages the program issues during one cycle, emitted together at the
 * cycle's transmit stage (SB-V3, SB-DM8).
 */
public final class OutgoingMessages {

    private final List<Message> messages = new ArrayList<>();

    /** Starts a new cycle: the previous cycle's messages have been transmitted. */
    public void clear() {
        messages.clear();
    }

    /** Adds one message to the current cycle's transmission. */
    public void add(Message message) {
        messages.add(message);
    }

    /** The messages issued in the current cycle, in the order they were issued. */
    public List<Message> messages() {
        return List.copyOf(messages);
    }
}
