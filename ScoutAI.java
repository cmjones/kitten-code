package team197;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import team197.modules.NavModule;
import team197.modules.FightModule;


/** Scout that seeks and destroys enemy buildings.
 * Rushes towards the enemy base.  If enemies are encountered,
 *  it runs.  If buildings are encountered, it kills them.
 */
public class ScoutAI extends SoldierAI {
    public ScoutAI(RobotController rc) {
        super(rc);
        nav.setDestination(rc, rc.senseEnemyHQLocation());
    }

    public ScoutAI(RobotController rc, SoldierAI oldme) {
    	super(rc, oldme);
        nav.setDestination(rc, rc.senseEnemyHQLocation());
    }

    public AI act(RobotController rc) throws Exception {
        Direction d;
        MapLocation target;

        // If there are enemies to fight, run! Otherwise,
        // continue towards the enemy base
        if(rc.isActive()){
            d = fight.fightCoward(rc);
            if(d == null)
                d = fight.fightClosestRobot(rc);
            else if(d == Direction.OMNI)
                d = nav.moveFlock(rc, 12);

            if(d != Direction.NONE && d != Direction.OMNI) {
                // If there's a mine, defuse it
                target = rc.getLocation().add(d);
                if(rc.senseMine(target) != null && rc.senseMine(target) != rc.getTeam())
                    rc.defuseMine(target);
                else
                    moveSafe(rc, d);
            }
        }

        // Keep the same ai for next round
        return this;
    }
}
