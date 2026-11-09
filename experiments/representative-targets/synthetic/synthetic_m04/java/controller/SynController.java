package syn04.controller;

import syn04.actuator.Actuator;
import syn04.constants.SynConstants;
import syn04.event.InputEvent;
import syn04.event.OutputEvent;
import syn04.mode.SynMode;
import syn04.sensor.Sensor;

public final class SynController {

    private SynMode currentMode = SynMode.M1;

    private final Sensor sensor;
    private final Actuator actuator;

    public SynController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
    }

    public SynMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean below1 = sensor.reading1() <= SynConstants.threshold1;
        boolean below2 = sensor.reading2() <= SynConstants.threshold2;
        boolean below3 = sensor.reading3() <= SynConstants.threshold3;

        if (currentMode == SynMode.M1) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below1) {
                currentMode = SynMode.M2;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below1) {
                currentMode = SynMode.M1;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M1;
            }
        } else if (currentMode == SynMode.M2) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below2) {
                currentMode = SynMode.M3;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below2) {
                currentMode = SynMode.M1;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M2;
            }
        } else if (currentMode == SynMode.M3) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below3) {
                currentMode = SynMode.M1;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below3) {
                currentMode = SynMode.M2;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M3;
            }
        } else if (currentMode == SynMode.Final) {
            if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.Final;
            }
        }
    }
}
