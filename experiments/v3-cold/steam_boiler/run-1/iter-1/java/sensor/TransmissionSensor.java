package steam_boiler.sensor;

import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.SteamBoilerConstants;
import steam_boiler.event.IncomingMessage;

/**
 * The receiving side of the message transmission system: the content of the
 * incoming transmission of the current cycle.
 */
public final class TransmissionSensor {
    @RoboChartType("nat") private static final int PUMPS = SteamBoilerConstants.PUMP_COUNT;

    // Successive transmissions carrying STOP; capped at the limit, which already forces emergency stop.
    @RoboChartType("nat") private int stopCount = 0;
    private boolean boilerWaitingEver = false;
    private boolean boilerWaitingNow = false;
    private boolean unitsReadyNow = false;
    private boolean intact = false;
    // The most recently reported level and steam rate.
    @RoboChartType("real") private double q = 0.0;
    @RoboChartType("real") private double v = 0.0;
    private final boolean[] pumpOn = new boolean[PUMPS];
    private final boolean[] flowing = new boolean[PUMPS];
    private final boolean[] pumpRepair = new boolean[PUMPS];
    private final boolean[] pumpControlRepair = new boolean[PUMPS];
    private final boolean[] pumpFailureAck = new boolean[PUMPS];
    private final boolean[] pumpControlFailureAck = new boolean[PUMPS];
    private boolean levelRepair = false;
    private boolean steamRepair = false;
    private boolean levelFailureAck = false;
    private boolean steamFailureAck = false;

    /** Takes in the messages of one cycle's incoming transmission. */
    public void receive(List<IncomingMessage> messages) {
        clearCycleFlags();
        boolean stop = false;
        boolean malformed = false;
        int levelCount = 0;
        int steamCount = 0;
        int[] pumpStateCount = new int[PUMPS];
        int[] pumpControlStateCount = new int[PUMPS];
        for (IncomingMessage message : messages) {
            if (message instanceof IncomingMessage.StopRequest) {
                stop = true;
            } else if (message instanceof IncomingMessage.SteamBoilerWaiting) {
                boilerWaitingNow = true;
                boilerWaitingEver = true;
            } else if (message instanceof IncomingMessage.PhysicalUnitsReady) {
                unitsReadyNow = true;
            } else if (message instanceof IncomingMessage.PumpState) {
                IncomingMessage.PumpState report = (IncomingMessage.PumpState) message;
                int n = report.reading().pump();
                if (validPump(n)) {
                    pumpOn[n] = report.reading().open();
                    pumpStateCount[n] = pumpStateCount[n] + 1;
                } else {
                    malformed = true;
                }
            } else if (message instanceof IncomingMessage.PumpControlState) {
                IncomingMessage.PumpControlState report = (IncomingMessage.PumpControlState) message;
                int n = report.reading().pump();
                if (validPump(n)) {
                    flowing[n] = report.reading().flowing();
                    pumpControlStateCount[n] = pumpControlStateCount[n] + 1;
                } else {
                    malformed = true;
                }
            } else if (message instanceof IncomingMessage.Level) {
                IncomingMessage.Level level = (IncomingMessage.Level) message;
                q = level.value();
                levelCount = levelCount + 1;
            } else if (message instanceof IncomingMessage.Steam) {
                IncomingMessage.Steam steam = (IncomingMessage.Steam) message;
                v = steam.value();
                steamCount = steamCount + 1;
            } else if (message instanceof IncomingMessage.PumpRepaired) {
                IncomingMessage.PumpRepaired repaired = (IncomingMessage.PumpRepaired) message;
                malformed = malformed || !mark(pumpRepair, repaired.pump());
            } else if (message instanceof IncomingMessage.PumpControlRepaired) {
                IncomingMessage.PumpControlRepaired repaired = (IncomingMessage.PumpControlRepaired) message;
                malformed = malformed || !mark(pumpControlRepair, repaired.pump());
            } else if (message instanceof IncomingMessage.LevelRepaired) {
                levelRepair = true;
            } else if (message instanceof IncomingMessage.SteamRepaired) {
                steamRepair = true;
            } else if (message instanceof IncomingMessage.PumpFailureAcknowledgement) {
                IncomingMessage.PumpFailureAcknowledgement ack = (IncomingMessage.PumpFailureAcknowledgement) message;
                malformed = malformed || !mark(pumpFailureAck, ack.pump());
            } else if (message instanceof IncomingMessage.PumpControlFailureAcknowledgement) {
                IncomingMessage.PumpControlFailureAcknowledgement ack =
                        (IncomingMessage.PumpControlFailureAcknowledgement) message;
                malformed = malformed || !mark(pumpControlFailureAck, ack.pump());
            } else if (message instanceof IncomingMessage.LevelFailureAcknowledgement) {
                levelFailureAck = true;
            } else if (message instanceof IncomingMessage.SteamOutcomeFailureAcknowledgement) {
                steamFailureAck = true;
            }
        }
        if (stop) {
            if (stopCount < SteamBoilerConstants.STOP_LIMIT) {
                stopCount = stopCount + 1;
            }
        } else {
            stopCount = 0;
        }
        intact = !malformed && levelCount == 1 && steamCount == 1
                && eachExactlyOnce(pumpStateCount) && eachExactlyOnce(pumpControlStateCount);
    }

    /** Number of successive transmissions, up to this one, that carried STOP. */
    @RoboChartType("nat")
    public int successiveStops() {
        return stopCount;
    }

    /** Whether STEAM_BOILER_WAITING has arrived in this or an earlier transmission. */
    public boolean boilerWaitingSeen() {
        return boilerWaitingEver;
    }

    /** Whether this transmission carries STEAM_BOILER_WAITING. */
    public boolean boilerWaitingReceived() {
        return boilerWaitingNow;
    }

    /** Whether this transmission carries PHYSICAL_UNITS_READY. */
    public boolean unitsReadyReceived() {
        return unitsReadyNow;
    }

    /** Whether every indispensable data message is present exactly once and no message is malformed. */
    public boolean transmissionIntact() {
        return intact;
    }

    /** The steam rate reported by STEAM(v) in this transmission (litre/sec). */
    @RoboChartType("real")
    public double steamReading() {
        return v;
    }

    @RoboChartType("real")
    double levelReading() {
        return q;
    }

    boolean pumpReportedOn(@RoboChartType("nat") int pump) {
        return pumpOn[pump];
    }

    boolean flowReported(@RoboChartType("nat") int pump) {
        return flowing[pump];
    }

    boolean pumpRepairReported(@RoboChartType("nat") int pump) {
        return pumpRepair[pump];
    }

    boolean pumpControlRepairReported(@RoboChartType("nat") int pump) {
        return pumpControlRepair[pump];
    }

    boolean pumpFailureAckReceived(@RoboChartType("nat") int pump) {
        return pumpFailureAck[pump];
    }

    boolean pumpControlFailureAckReceived(@RoboChartType("nat") int pump) {
        return pumpControlFailureAck[pump];
    }

    boolean levelRepairReported() {
        return levelRepair;
    }

    boolean steamRepairReported() {
        return steamRepair;
    }

    boolean levelFailureAckReceived() {
        return levelFailureAck;
    }

    boolean steamFailureAckReceived() {
        return steamFailureAck;
    }

    private void clearCycleFlags() {
        boilerWaitingNow = false;
        unitsReadyNow = false;
        levelRepair = false;
        steamRepair = false;
        levelFailureAck = false;
        steamFailureAck = false;
        for (int n = 0; n < PUMPS; n++) {
            pumpRepair[n] = false;
            pumpControlRepair[n] = false;
            pumpFailureAck[n] = false;
            pumpControlFailureAck[n] = false;
        }
    }

    private static boolean validPump(int pump) {
        return pump >= 0 && pump < PUMPS;
    }

    // Sets flags[pump]; false if the pump number does not name a pump.
    private static boolean mark(boolean[] flags, int pump) {
        if (validPump(pump)) {
            flags[pump] = true;
            return true;
        }
        return false;
    }

    private static boolean eachExactlyOnce(int[] counts) {
        for (int count : counts) {
            if (count != 1) {
                return false;
            }
        }
        return true;
    }
}
