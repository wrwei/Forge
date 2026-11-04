package steam_boiler.sensor;

import steam_boiler.annotation.RoboChartType;

/**
 * The analysed information of the current cycle, as the controller reads it. The plant monitor
 * fills it in during the analysis stage of each cycle (SB-V3); the controller only queries it.
 */
public final class BoilerSensor {

    @RoboChartType("nat") private int successiveStops;
    private boolean transmissionFailure;
    private boolean waitingSignalled;
    private boolean unitsReadyReceived;
    private boolean valveOpen;
    private boolean levelDeviceFailed;
    private boolean steamDeviceFailed;
    private boolean pumpControlFailed;
    private boolean allUnitsSound = true;
    @RoboChartType("real") private double waterLevel;
    @RoboChartType("real") private double steamRate;
    @RoboChartType("real") private double estimatedLevel;
    @RoboChartType("real") private double predictedMinimumLevel;
    @RoboChartType("real") private double predictedMaximumLevel;

    /**
     * Records the message-level facts of the cycle.
     *
     * @param stops successive transmissions that have carried STOP
     * @param transmissionLost whether a transmission failure was detected
     * @param plantWaiting whether STEAM_BOILER_WAITING has arrived
     * @param unitsReady whether PHYSICAL_UNITS_READY arrived in this cycle
     * @param valveIsOpen whether the evacuation valve has been ordered open
     */
    public void updateMessages(@RoboChartType("nat") int stops, boolean transmissionLost,
            boolean plantWaiting, boolean unitsReady, boolean valveIsOpen) {
        this.successiveStops = stops;
        this.transmissionFailure = transmissionLost;
        this.waitingSignalled = plantWaiting;
        this.unitsReadyReceived = unitsReady;
        this.valveOpen = valveIsOpen;
    }

    /**
     * Records which units are out of order.
     *
     * @param levelDown whether the level measuring device is out of order
     * @param steamDown whether the steam measuring device is out of order
     * @param controlDown whether any pump controller is out of order
     * @param soundUnits whether every physical unit is sound
     */
    public void updateUnits(boolean levelDown, boolean steamDown, boolean controlDown,
            boolean soundUnits) {
        this.levelDeviceFailed = levelDown;
        this.steamDeviceFailed = steamDown;
        this.pumpControlFailed = controlDown;
        this.allUnitsSound = soundUnits;
    }

    /**
     * Records the readings, the level estimate and the worst-case level prediction.
     *
     * @param levelReading the reported water level q, litres
     * @param steamReading the reported steam rate v, litres per second
     * @param levelEstimate the middle of the estimated level interval, litres
     * @param lowestNextLevel the lowest level reachable by the next cycle, litres
     * @param highestNextLevel the highest level reachable by the next cycle, litres
     */
    public void updateLevels(@RoboChartType("real") double levelReading,
            @RoboChartType("real") double steamReading,
            @RoboChartType("real") double levelEstimate,
            @RoboChartType("real") double lowestNextLevel,
            @RoboChartType("real") double highestNextLevel) {
        this.waterLevel = levelReading;
        this.steamRate = steamReading;
        this.estimatedLevel = levelEstimate;
        this.predictedMinimumLevel = lowestNextLevel;
        this.predictedMaximumLevel = highestNextLevel;
    }

    /** @return how many successive transmissions have carried STOP (SB-V10). */
    @RoboChartType("nat")
    public int successiveStops() {
        return successiveStops;
    }

    /** @return whether a transmission failure was detected in this cycle (SB-FR5). */
    public boolean transmissionFailure() {
        return transmissionFailure;
    }

    /** @return whether STEAM_BOILER_WAITING has arrived (SB-Beh2). */
    public boolean waitingSignalled() {
        return waitingSignalled;
    }

    /** @return whether PHYSICAL_UNITS_READY arrived in this cycle (SB-Beh8). */
    public boolean unitsReadyReceived() {
        return unitsReadyReceived;
    }

    /** @return whether the evacuation valve is open (SB-DM7). */
    public boolean valveOpen() {
        return valveOpen;
    }

    /** @return whether the water-level measuring device is out of order (SB-V9, SB-FR3). */
    public boolean levelDeviceFailed() {
        return levelDeviceFailed;
    }

    /** @return whether the steam measuring device is out of order (SB-V9, SB-FR4). */
    public boolean steamDeviceFailed() {
        return steamDeviceFailed;
    }

    /** @return whether any pump controller is out of order (SB-V9, SB-FR2). */
    public boolean pumpControlFailed() {
        return pumpControlFailed;
    }

    /** @return whether every physical unit is sound (SB-V9). */
    public boolean allUnitsSound() {
        return allUnitsSound;
    }

    /** @return the water level q most recently reported, litres (SB-V5). */
    @RoboChartType("real")
    public double waterLevel() {
        return waterLevel;
    }

    /** @return the steam rate v most recently reported, litres per second (SB-V6). */
    @RoboChartType("real")
    public double steamRate() {
        return steamRate;
    }

    /** @return the middle of the estimated level interval, litres (SB-V8, SB-Beh19). */
    @RoboChartType("real")
    public double estimatedLevel() {
        return estimatedLevel;
    }

    /** @return the lowest level the boiler can reach by the next cycle, litres (SB-Beh13). */
    @RoboChartType("real")
    public double predictedMinimumLevel() {
        return predictedMinimumLevel;
    }

    /** @return the highest level the boiler can reach by the next cycle, litres (SB-Beh13). */
    @RoboChartType("real")
    public double predictedMaximumLevel() {
        return predictedMaximumLevel;
    }
}
