package lre.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.google.gson.Gson;

import lre.sensor.Sensor;

/** Reads one JSON telemetry line per cycle from a TCP link and pushes it into the sensor. */
public final class TelemetryLink implements AutoCloseable {

    /** One line of telemetry as the vehicle's bridge publishes it. */
    public static final class Frame {
        public double depth;
        public double ns_vel;
        public double ew_vel;
        public double rate_of_climb;
    }

    private final Socket socket;
    private final BufferedReader reader;
    private final Gson gson = new Gson();

    public TelemetryLink(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /** Reads the next frame and updates the sensor; returns false at end of stream. */
    public boolean poll(Sensor sensor) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return false;
        }
        Frame f = gson.fromJson(line, Frame.class);
        sensor.update(f.depth, f.ns_vel, f.ew_vel, f.rate_of_climb);
        return true;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
