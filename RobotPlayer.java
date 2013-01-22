package team197;

import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Clock;

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
            return new ArtilleryAI(rc);
        case GENERATOR:
            return new GeneratorAI(rc);
        case HQ:
            return new HQAI(rc);
        case MEDBAY:
            return new MedbayAI(rc);
        case SHIELDS:
            return new ShieldsAI(rc);
        case SUPPLIER:
            return new SupplierAI(rc);
        case SOLDIER:
            return new SoldierAI(rc);
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
                ai.do_upkeep(rc);
                ai = ai.act(rc);
            } catch(Exception e) {
                // Wear the error hat!
                rc.wearHat();
                e.printStackTrace();
            }

            rc.yield();
        }

        // This robot doesn't have another ai to use, might
        // as well end it all.
        rc.suicide();
    }
}
