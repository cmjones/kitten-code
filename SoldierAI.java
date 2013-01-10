package team197;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import team197.modules.NavModule;


/** Example Soldier ai, obtained from the example code.
 * moves randomly and lays mines down
 */
public class SoldierAI extends AI {
    private NavModule nav;

    public SoldierAI(RobotController rc) {
        nav = new NavModule(rc);
        nav.setDestination(rc, rc.senseEnemyHQLocation());
    }

    public AI act(RobotController rc) throws Exception {
        Direction d;
        MapLocation target;

        // If we can't do anything, don't do anything
        if(!rc.isActive()) return this;

        // Grab the next direction to travel
        d = nav.moveSimple(rc);

        // If there's a mine in the way, defuse it. Otherwise,
        //  move there.
        if(d != Direction.NONE && d != Direction.OMNI) {
            // If there's a mine, defuse it
            target = rc.getLocation().add(d);
            if(rc.senseMine(target) != null)
                rc.defuseMine(target);
            else
                rc.move(d);
        }

        // Keep the same ai for next round
        return this;
    }
}
