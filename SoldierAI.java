package team197;

import battlecode.common.Direction;
import battlecode.common.RobotController;


/** Example Soldier ai, obtained from the example code.
 * moves randomly and lays mines down
 */
public class SoldierAI extends AI {
    public AI act(RobotController rc) throws Exception {
        if (rc.isActive()) {
            if (Math.random()<0.005) {
                // Lay a mine
                if(rc.senseMine(rc.getLocation())==null)
                    rc.layMine();
            } else {
                // Choose a random direction, and move that way if possible
                Direction dir = Direction.values()[(int)(Math.random()*8)];
                if(rc.canMove(dir)) {
                    rc.move(dir);
                    rc.setIndicatorString(0, "Last direction moved: "+dir.toString());
                }
            }
        }

        // Keep the same ai for next round
        return this;
    }
}
