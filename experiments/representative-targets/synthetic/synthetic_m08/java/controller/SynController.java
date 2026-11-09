package syn08.controller;

import syn08.actuator.Actuator;
import syn08.constants.SynConstants;
import syn08.event.InputEvent;
import syn08.event.OutputEvent;
import syn08.mode.SynMode;
import syn08.sensor.Sensor;

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
        boolean below4 = sensor.reading4() <= SynConstants.threshold4;
        boolean below5 = sensor.reading5() <= SynConstants.threshold5;
        boolean below6 = sensor.reading6() <= SynConstants.threshold6;
        boolean below7 = sensor.reading7() <= SynConstants.threshold7;

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
                currentMode = SynMode.M4;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below3) {
                currentMode = SynMode.M2;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M3;
            }
        } else if (currentMode == SynMode.M4) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below4) {
                currentMode = SynMode.M5;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below4) {
                currentMode = SynMode.M3;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M4;
            }
        } else if (currentMode == SynMode.M5) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below5) {
                currentMode = SynMode.M6;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below5) {
                currentMode = SynMode.M4;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M5;
            }
        } else if (currentMode == SynMode.M6) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below6) {
                currentMode = SynMode.M7;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below6) {
                currentMode = SynMode.M5;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M6;
            }
        } else if (currentMode == SynMode.M7) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below7) {
                currentMode = SynMode.M1;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below7) {
                currentMode = SynMode.M6;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M7;
            }
        } else if (currentMode == SynMode.Final) {
            if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.Final;
            }
        }
    }
}
