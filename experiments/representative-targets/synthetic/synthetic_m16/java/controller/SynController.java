package syn16.controller;

import syn16.actuator.Actuator;
import syn16.constants.SynConstants;
import syn16.event.InputEvent;
import syn16.event.OutputEvent;
import syn16.mode.SynMode;
import syn16.sensor.Sensor;

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
        boolean below8 = sensor.reading8() <= SynConstants.threshold8;
        boolean below9 = sensor.reading9() <= SynConstants.threshold9;
        boolean below10 = sensor.reading10() <= SynConstants.threshold10;
        boolean below11 = sensor.reading11() <= SynConstants.threshold11;
        boolean below12 = sensor.reading12() <= SynConstants.threshold12;
        boolean below13 = sensor.reading13() <= SynConstants.threshold13;
        boolean below14 = sensor.reading14() <= SynConstants.threshold14;
        boolean below15 = sensor.reading15() <= SynConstants.threshold15;

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
                currentMode = SynMode.M8;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below7) {
                currentMode = SynMode.M6;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M7;
            }
        } else if (currentMode == SynMode.M8) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below8) {
                currentMode = SynMode.M9;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below8) {
                currentMode = SynMode.M7;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M8;
            }
        } else if (currentMode == SynMode.M9) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below9) {
                currentMode = SynMode.M10;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below9) {
                currentMode = SynMode.M8;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M9;
            }
        } else if (currentMode == SynMode.M10) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below10) {
                currentMode = SynMode.M11;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below10) {
                currentMode = SynMode.M9;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M10;
            }
        } else if (currentMode == SynMode.M11) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below11) {
                currentMode = SynMode.M12;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below11) {
                currentMode = SynMode.M10;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M11;
            }
        } else if (currentMode == SynMode.M12) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below12) {
                currentMode = SynMode.M13;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below12) {
                currentMode = SynMode.M11;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M12;
            }
        } else if (currentMode == SynMode.M13) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below13) {
                currentMode = SynMode.M14;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below13) {
                currentMode = SynMode.M12;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M13;
            }
        } else if (currentMode == SynMode.M14) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below14) {
                currentMode = SynMode.M15;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below14) {
                currentMode = SynMode.M13;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M14;
            }
        } else if (currentMode == SynMode.M15) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Advance && below15) {
                currentMode = SynMode.M1;
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }
            else if (event instanceof InputEvent.Retreat && !below15) {
                currentMode = SynMode.M14;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            else if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.M15;
            }
        } else if (currentMode == SynMode.Final) {
            if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.Final;
            }
        }
    }
}
