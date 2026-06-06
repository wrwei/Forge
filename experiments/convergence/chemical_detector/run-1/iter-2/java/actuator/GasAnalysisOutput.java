package chemdetector.actuator;

import chemdetector.event.InputEvent;

/**
 * Output channel of the gas-analysis subsystem: carries the turn, stop,
 * and resume events to the movement subsystem (CD-ARCH2).
 */
public final class GasAnalysisOutput {

    private InputEvent lastCommand;

    public void apply(InputEvent command) {
        this.lastCommand = command;
    }

    public InputEvent lastCommand() {
        return lastCommand;
    }
}
