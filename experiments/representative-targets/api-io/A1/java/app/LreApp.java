package lre.app;

import java.io.IOException;
import java.nio.file.Path;

import lre.actuator.Actuator;
import lre.controller.LreController;
import lre.event.InputEvent;
import lre.io.CommandPublisher;
import lre.io.TelemetryLink;
import lre.operation.CalcCDyn;
import lre.operation.CalcCPA;
import lre.operation.CalcCStc;
import lre.operation.CalcVel;
import lre.operation.CheckOPEZ;
import lre.sensor.ObstacleRegister;
import lre.sensor.Sensor;

/** Wires the controller to a telemetry socket and a command log; one step per telemetry frame. */
public final class LreApp {

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9100;
        Path log = Path.of(args.length > 2 ? args[2] : "commands.jsonl");

        Sensor sensor = new Sensor(new ObstacleRegister());
        Actuator actuator = new Actuator();
        CalcVel calcVel = new CalcVel(sensor);
        CalcCStc calcCStc = new CalcCStc(sensor);
        CalcCDyn calcCDyn = new CalcCDyn(sensor);
        CalcCPA calcCPA = new CalcCPA(sensor, calcCDyn);
        CheckOPEZ checkOPEZ = new CheckOPEZ(sensor, calcCStc);
        LreController controller = new LreController(sensor, actuator, calcVel, calcCStc, calcCDyn, calcCPA, checkOPEZ);
        CommandPublisher publisher = new CommandPublisher(log);

        try (TelemetryLink link = new TelemetryLink(host, port)) {
            while (link.poll(sensor)) {
                controller.step(new InputEvent.ReqMOM());
                publisher.publish(new lre.event.OutputEvent.AdvVel(actuator.lastAdvVel()));
            }
        }
    }
}
