package steam_boiler.sensor;

/**
 * Soundness bookkeeping of one physical unit (SB-V9): whether it is believed sound or
 * out of order, whether its failure detection has been acknowledged (SB-FR6), and
 * whether a repair it announced must be acknowledged this cycle (SB-FR7).
 */
final class UnitRecord {

    private UnitStatus status = UnitStatus.SOUND;
    private boolean repairNotice;
    private boolean acknowledgementNotice;
    private boolean repairToAcknowledge;

    /** Clears the notices of the previous transmission. */
    void beginTransmission() {
        repairNotice = false;
        acknowledgementNotice = false;
        repairToAcknowledge = false;
    }

    /** The unit announced its repair in this transmission. */
    void noteRepaired() {
        repairNotice = true;
    }

    /** The unit acknowledged the program's failure detection in this transmission. */
    void noteAcknowledged() {
        acknowledgementNotice = true;
    }

    /**
     * Applies this transmission's acknowledgement and repair notices. An
     * acknowledgement is expected only while a failure detection is being announced,
     * and a repair only while the unit is out of order; any other notice is aberrant.
     *
     * @return false if a notice was aberrant
     */
    boolean settleNotices() {
        boolean consistent = true;
        if (acknowledgementNotice) {
            if (status == UnitStatus.FAILED) {
                status = UnitStatus.FAILED_ACKNOWLEDGED;
            } else {
                consistent = false;
            }
        }
        if (repairNotice) {
            if (status != UnitStatus.SOUND) {
                status = UnitStatus.SOUND;
                repairToAcknowledge = true;
            } else {
                consistent = false;
            }
        }
        return consistent;
    }

    /** The program has detected a failure of this unit. */
    void detectFailure() {
        status = UnitStatus.FAILED;
    }

    boolean sound() {
        return status == UnitStatus.SOUND;
    }

    /** Whether the failure detection must be (re)announced this cycle. */
    boolean announcementDue() {
        return status == UnitStatus.FAILED;
    }

    /** Whether a repair acknowledgement must be sent this cycle. */
    boolean repairAcknowledgementDue() {
        return repairToAcknowledge;
    }
}
