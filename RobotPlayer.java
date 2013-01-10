package team197;

import battlecode.common.RobotController;
import battlecode.common.RobotType;

/** Top-level entry into the robot AI.
 * Robots generally act with a specific behavior when created, and this behavior
 * can be determined by the HQ.  Thus, the current strategy isdecided by the AI
 * of the HQ (which can change).
 */
public class RobotPlayer {
    /** Chooses an initial ai for the robot.
     * First, check the message board to see if the HQ has a particular ai in mind.
     * If not, decide on an ai based on simple statistics about the game.
     */
    public static AI chooseAI(RobotController rc) {
        switch(rc.getType()) {
        case ARTILLERY:
            return new ArtilleryAI();
        case GENERATOR:
            return new GeneratorAI();
        case HQ:
            return new HQAI();
        case MEDBAY:
            return new MedbayAI();
        case SHIELDS:
            return new ShieldsAI();
        case SUPPLIER:
            return new SupplierAI();
        case SOLDIER:
            return new FighterAI(rc);
        }

        return null;
    }

    public static void run(RobotController rc) {
        AI ai;

        // Choose the ai for this robot
        ai = chooseAI(rc);

        // Start acting
        while (ai != null) {
            try {
            	ai.do_checkin(rc);
                ai = ai.act(rc);
                rc.yield();
            } catch(Exception e) {
                // Wear the error hat!
                rc.wearHat();
                e.printStackTrace();
            }
        }

        // This robot doesn't have another ai to use, might
        // as well end it all.
        rc.suicide();
    }
}
