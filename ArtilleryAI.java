package team197;

import battlecode.common.RobotController;


/** Placeholder AI for the Artillery.
 */
public class ArtilleryAI extends AI {
    public AI act(RobotController rc) throws Exception {
        // Set an indicator string
        rc.setIndicatorString(0, "I am an Artillery!");

        // Keep the same ai for next round
        return this;
    }
}
