package team197;

import battlecode.common.Direction;
import battlecode.common.RobotController;


/** Example HQ ai, obtained from the example code.
 *  Just spawns new soldiers.
 */
public class HQAI extends AI {
    public AI act(RobotController rc) throws Exception {
        if (rc.isActive()) {
            // Spawn a soldier
            Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
            if (rc.canMove(dir))
                rc.spawn(dir);
        }

        // Keep the same ai for next round
        return this;
    }
}
