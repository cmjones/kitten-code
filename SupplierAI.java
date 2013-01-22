package team197;

import battlecode.common.RobotController;


/** Placeholder AI for the Supplier.
 */
public class SupplierAI extends AI {
    public SupplierAI(RobotController rc) {
    	super(rc);
    }
    public AI act(RobotController rc) throws Exception {
        // Set an indicator string
        rc.setIndicatorString(0, "I am a Supplier!");

        // Keep the same ai for next round
        return this;
    }
}
