package lre.actuator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives advised velocity and heading from the LRE controller via
 * {@link OutputEvent} messages and stores the most recent values for the
 * autopilot controller.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvVel;

    @RoboChartType("real")
    private double lastAdvHdng;

    private final Gson gson = new Gson();
    private final Path log;

    public Actuator() {
        this(Path.of("commands.jsonl"));
    }

    public Actuator(Path log) {
        this.log = log;
        this.lastAdvVel = 0.0;
        this.lastAdvHdng = 0.0;
    }

    public void receive(OutputEvent event) {
        publish(event);
        if (event instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel av = (OutputEvent.AdvVel) event;
            this.lastAdvVel = av.value();
        } else if (event instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng ah = (OutputEvent.AdvHdng) event;
            this.lastAdvHdng = ah.value();
        }
    }

    @RoboChartType("real")
    public double lastAdvVel() {
        return lastAdvVel;
    }

    @RoboChartType("real")
    public double lastAdvHdng() {
        return lastAdvHdng;
    }

    private void publish(OutputEvent event) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("timestamp", System.currentTimeMillis());
        if (event instanceof OutputEvent.AdvVel av) {
            record.put("command", "advVel");
            record.put("value", av.value());
        } else if (event instanceof OutputEvent.AdvHdng ah) {
            record.put("command", "advHdng");
            record.put("value", ah.value());
        }
        try {
            Files.writeString(log, gson.toJson(record) + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
