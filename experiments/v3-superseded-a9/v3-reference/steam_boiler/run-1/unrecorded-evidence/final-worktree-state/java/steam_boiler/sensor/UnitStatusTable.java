package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;

/**
 * The status of every physical unit: the four pumps, the four pump controllers, the level
 * measuring device and the steam measuring device. Pumps and controllers are numbered 1 to 4.
 */
public final class UnitStatusTable {

    private final List<UnitRecord> pumps = new ArrayList<>();
    private final List<UnitRecord> pumpControls = new ArrayList<>();
    private final UnitRecord levelDevice = new UnitRecord();
    private final UnitRecord steamDevice = new UnitRecord();

    /** Creates a table in which every unit is sound. */
    public UnitStatusTable() {
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            pumps.add(new UnitRecord());
            pumpControls.add(new UnitRecord());
        }
    }

    /** Whether {@code pump} names one of the pumps (1 to 4). */
    public boolean isPumpNumber(@RoboChartType("nat") int pump) {
        return pump >= 1 && pump <= BoilerConstants.PUMP_COUNT;
    }

    /** The record of pump {@code pump} (1 to 4). */
    public UnitRecord pump(@RoboChartType("nat") int pump) {
        return pumps.get(pump - 1);
    }

    /** The record of the controller of pump {@code pump} (1 to 4). */
    public UnitRecord pumpControl(@RoboChartType("nat") int pump) {
        return pumpControls.get(pump - 1);
    }

    /** The record of the level measuring device. */
    public UnitRecord levelDevice() {
        return levelDevice;
    }

    /** The record of the steam measuring device. */
    public UnitRecord steamDevice() {
        return steamDevice;
    }

    /** Whether at least one pump is out of order. */
    public boolean anyPumpOutOfOrder() {
        for (int i = 0; i < pumps.size(); i++) {
            if (pumps.get(i).isOutOfOrder()) {
                return true;
            }
        }
        return false;
    }

    /** Whether at least one pump controller is out of order. */
    public boolean anyPumpControlOutOfOrder() {
        for (int i = 0; i < pumpControls.size(); i++) {
            if (pumpControls.get(i).isOutOfOrder()) {
                return true;
            }
        }
        return false;
    }

    /** Clears every unit's per-cycle repair report. */
    public void startCycle() {
        for (int i = 0; i < pumps.size(); i++) {
            pumps.get(i).startCycle();
            pumpControls.get(i).startCycle();
        }
        levelDevice.startCycle();
        steamDevice.startCycle();
    }
}
