package lre.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;

import lre.event.OutputEvent;

/** Appends every command the controller issues to a JSON-lines log. */
public final class CommandPublisher {

    private final Path log;
    private final Gson gson = new Gson();

    public CommandPublisher(Path log) {
        this.log = log;
    }

    public void publish(OutputEvent event) throws IOException {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("timestamp", System.currentTimeMillis());
        if (event instanceof OutputEvent.AdvVel av) {
            record.put("command", "advVel");
            record.put("value", av.value());
        } else if (event instanceof OutputEvent.AdvHdng ah) {
            record.put("command", "advHdng");
            record.put("value", ah.value());
        }
        Files.writeString(log, gson.toJson(record) + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
