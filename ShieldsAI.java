package team197;

import battlecode.common.RobotController;


/** Placeholder AI for the Shields.
 */
public class ShieldsAI extends AI {
	
    public ShieldsAI(RobotController rc) {
    	super(rc);
    }
    public AI act(RobotController rc) throws Exception {
        // Set an indicator string
        rc.setIndicatorString(0, "I am Shields!");

        // Keep the same ai for next round
        return this;
    }
}
