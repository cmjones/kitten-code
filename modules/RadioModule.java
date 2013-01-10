package team197.modules;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;


/** Module for reading and writing messages in a somewhat secure manner.
 * Potential messages are written to channels depending on the turn count,
 *  and signed with the turn count and a secret key.  Attempting to read
 *  a message that has been corrupted will simply return a blank message (0).
 *
 * Each channel is dedicated to a particular use, and these channel locations
 *  change every turn in a predictable manner.  Thus, messages only last for
 *  one full turn even if not corrupted, and can only be read for the turn
 *  after the current one.
 */
public class RadioModule {
    public static final int CHANNEL_FREE01 = 0,
                            CHANNEL_FREE02 = 1,
                            CHANNEL_FREE03 = 2,
                            CHANNEL_FREE04 = 3,
                            CHANNEL_FREE05 = 4,
                            CHANNEL_FREE06 = 5,
                            CHANNEL_FREE07 = 6,
                            CHANNEL_FREE08 = 7,
                            CHANNEL_FREE09 = 8,
                            CHANNEL_FREE10 = 9,
                            CHANNEL_BUILDER_DESTI = 10, //NOTE: This is temporary.
                            CHANNEL_GETJOB = 11,
                            CHANNEL_CHECKIN = 12;
    private static final int KEY = 0xCA000000,
                             CHANNEL_OFFSET = 197,
                             TURN_SHIFT = 24,
                             INC = 13,
                             SIG_MASK = 0xFF000000,
                             SIG_MASK_INV = 0x00FFFFFF;
    private int curTurn,
                curReadBase,
                curWriteBase,
                prevSignature,
                curSignature;


    // Ensures the current turn and read/write bases are up-to-date.
    // Safe to call multiple times in one round, any calls past the
    // first should do nothing.
    private void checkTurn() {
        int round = Clock.getRoundNum();
        if(curTurn == round) return;
        // Recalculate things
        curTurn = round;
        curReadBase = (round-1)*INC+CHANNEL_OFFSET;
        curWriteBase = curReadBase+INC;
        prevSignature = ((round-1) << TURN_SHIFT)^KEY;
        curSignature = (round << TURN_SHIFT)^KEY;
        ////System.out.println(Integer.toHexString(prevSignature) + " " + Integer.toHexString(curSignature));
    }

    /** Writes a message securely to the given channel.
     * This message can only be read next round.  The channel number
     *  should be one of the reserved channels (CHANNEL_<name>), and
     *  writing to a channel will not erase its readable contents.
     *
     * Depending on the size of the signature, not every bit of the
     *  message will be written.  Currently, only the lower 24 bits
     *  are free.
     *
     * Fails silently on error.
     */
    public void write(RobotController rc, int channelNum, int message) {
        // First ensure the turn count is up to date
        checkTurn();

        // Now write a signed message to the channel
        message = (message&SIG_MASK_INV)|curSignature;

        try {
            rc.broadcast((curWriteBase+channelNum)%GameConstants.BROADCAST_MAX_CHANNELS, message);
        } catch(GameActionException e) {
            // Fail Silently
        }
    }

    /** Reads a message securely from the given channel.
     * This message must have been written in the previous round.
     *  If the message is invalid, or corrupted, the signature will
     *  not match what is expected and the returned message will
     *  be 0.
     *
     * Also returns 0 on an error.
     */
    public int read(RobotController rc, int channelNum) {
        int msg;

        // First ensure the turn count is up to date
        checkTurn();

        // Now read a message from the channel
        try {
            msg = rc.readBroadcast((curReadBase+channelNum)%GameConstants.BROADCAST_MAX_CHANNELS);
        } catch(GameActionException e) {
            // Failure means no message was read.
            msg = 0;
        }

        // Check the signature on the message
        if((msg&SIG_MASK) == prevSignature)
            return msg&SIG_MASK_INV;

        // Signature was invalid
        return 0;
    }

    /** Reads the current state of the indicated channel.
     * Returns the current value of the message on the given channel.
     *  Only returns the message portion of the value; use rc.readBroadcast
     *  to get the raw value.  For the value VAL, if the signature portion
     *  of the channel is valid for this round, returns VAL.  Otherwise,
     *  returns -VAL.
     */
    public int readTransient(RobotController rc, int channelNum) {
        int msg,
            retval;

        // First ensure the turn count is up to date
        checkTurn();

        // Now read a message from the channel
        try {
            msg = rc.readBroadcast((curWriteBase+channelNum)%GameConstants.BROADCAST_MAX_CHANNELS);
        } catch(GameActionException e) {
            // Failure means no message was read.
            msg = 0;
        }

        // Grab the message portion of the channel
        retval = msg&SIG_MASK_INV;

        // Now check the signature
        if((msg&SIG_MASK) == curSignature)
            return retval;
        else
            return -retval;
    }
}
