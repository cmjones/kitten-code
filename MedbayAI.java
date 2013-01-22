package team197;

import battlecode.common.RobotController;


/** Placeholder AI for the Medbay.
 */
public class MedbayAI extends AI {
    public MedbayAI(RobotController rc) {
    	super(rc);
    }
    public AI act(RobotController rc) throws Exception {
        // Set an indicator string
        rc.setIndicatorString(0, "I am a Medbay!");

        // Keep the same ai for next round
        return new FindPathAI(rc);
    }
}
