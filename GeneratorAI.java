package team197;

import battlecode.common.RobotController;


/** Placeholder AI for the Generator.
 */
public class GeneratorAI extends AI {
	
    public GeneratorAI(RobotController rc) {
    	super(rc);
    }
    public AI act(RobotController rc) throws Exception {
        // Set an indicator string
        rc.setIndicatorString(0, "I am a Generator!");

        // Keep the same ai for next round
        return this;
    }
}
