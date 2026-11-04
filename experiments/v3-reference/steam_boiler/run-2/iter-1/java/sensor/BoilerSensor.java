package steam_boiler.sensor;

import steam_boiler.annotation.RoboChartType;

/**
 * Sensor service: what the program knows about the plant in the current cycle,
 * once the incoming transmission has been received and analysed. The unit
 * monitor fills it before the controller is stepped; the controller only reads
 * it.
 */
public final class BoilerSensor {

    private boolean operatorStopConfirmed;
    private boolean transmissionFailure;
    private boolean steamBoilerWaitingReceived;
    private boolean physicalUnitsReadyReceived;
    private boolean levelDeviceOutOfOrder;
    private boolean steamDeviceOutOfOrder;
    private boolean anyPumpOutOfOrder;
    private boolean anyPumpControllerOutOfOrder;
    @RoboChartType("real") private double steamReading;
    @RoboChartType("real") private double levelReading;
    @RoboChartType("real") private double predictedMinLevel;
    @RoboChartType("real") private double predictedMaxLevel;
    @RoboChartType("real") private double predictedMinLevelPumpsClosed;
    @RoboChartType("real") private double predictedMaxLevelPumpsClosed;

    /** Records the message-level facts of the current cycle. */
    public void updateMessages(boolean operatorStopConfirmed, boolean transmissionFailure,
            boolean steamBoilerWaitingReceived, boolean physicalUnitsReadyReceived) {
        this.operatorStopConfirmed = operatorStopConfirmed;
        this.transmissionFailure = transmissionFailure;
        this.steamBoilerWaitingReceived = steamBoilerWaitingReceived;
        this.physicalUnitsReadyReceived = physicalUnitsReadyReceived;
    }

    /** Records which units are currently believed out of order. */
    public void updateUnits(boolean levelDeviceOutOfOrder, boolean steamDeviceOutOfOrder,
            boolean anyPumpOutOfOrder, boolean anyPumpControllerOutOfOrder) {
        this.levelDeviceOutOfOrder = levelDeviceOutOfOrder;
        this.steamDeviceOutOfOrder = steamDeviceOutOfOrder;
        this.anyPumpOutOfOrder = anyPumpOutOfOrder;
        this.anyPumpControllerOutOfOrder = anyPumpControllerOutOfOrder;
    }

    /** Records the readings and the level predictions of the current cycle. */
    public void updateLevels(@RoboChartType("real") double steamReading,
            @RoboChartType("real") double levelReading,
            @RoboChartType("real") double predictedMinLevel,
            @RoboChartType("real") double predictedMaxLevel,
            @RoboChartType("real") double predictedMinLevelPumpsClosed,
            @RoboChartType("real") double predictedMaxLevelPumpsClosed) {
        this.steamReading = steamReading;
        this.levelReading = levelReading;
        this.predictedMinLevel = predictedMinLevel;
        this.predictedMaxLevel = predictedMaxLevel;
        this.predictedMinLevelPumpsClosed = predictedMinLevelPumpsClosed;
        this.predictedMaxLevelPumpsClosed = predictedMaxLevelPumpsClosed;
    }

    /** STOP has arrived on three successive transmissions (SB-V10, SB-Beh23). */
    public boolean operatorStopConfirmed() { return operatorStopConfirmed; }

    /** The transmission system has failed in this cycle (SB-FR5). */
    public boolean transmissionFailure() { return transmissionFailure; }

    /** STEAM_BOILER_WAITING is part of this cycle's transmission. */
    public boolean steamBoilerWaitingReceived() { return steamBoilerWaitingReceived; }

    /** PHYSICAL_UNITS_READY is part of this cycle's transmission. */
    public boolean physicalUnitsReadyReceived() { return physicalUnitsReadyReceived; }

    /** The water-level measuring device is believed out of order (SB-V9, SB-FR3). */
    public boolean levelDeviceOutOfOrder() { return levelDeviceOutOfOrder; }

    /** The steam measuring device is believed out of order (SB-V9, SB-FR4). */
    public boolean steamDeviceOutOfOrder() { return steamDeviceOutOfOrder; }

    /** At least one pump is believed out of order (SB-V9, SB-FR1). */
    public boolean anyPumpOutOfOrder() { return anyPumpOutOfOrder; }

    /** At least one pump controller is believed out of order (SB-V9, SB-FR2). */
    public boolean anyPumpControllerOutOfOrder() { return anyPumpControllerOutOfOrder; }

    /** v: the steam rate reported in this cycle (litre/sec, SB-V6). */
    @RoboChartType("real") public double steamReading() { return steamReading; }

    /** q: the water level reported in this cycle (litre, SB-V5). */
    @RoboChartType("real") public double levelReading() { return levelReading; }

    /**
     * Lowest level the boiler can have until the next transmission if the open
     * pumps stay open, under worst-case steam; from q, or from the estimate
     * while the level device is out of order (litre).
     */
    @RoboChartType("real") public double predictedMinLevel() { return predictedMinLevel; }

    /** Highest level the boiler can have until the next transmission if the open pumps stay open (litre). */
    @RoboChartType("real") public double predictedMaxLevel() { return predictedMaxLevel; }

    /** Lowest level the boiler can have until the next transmission if the program closes the pumps now (litre). */
    @RoboChartType("real") public double predictedMinLevelPumpsClosed() { return predictedMinLevelPumpsClosed; }

    /** Highest level the boiler can have until the next transmission if the program closes the pumps now (litre). */
    @RoboChartType("real") public double predictedMaxLevelPumpsClosed() { return predictedMaxLevelPumpsClosed; }
}
