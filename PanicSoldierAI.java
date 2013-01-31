package team197;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import team197.modules.FightModule;
import team197.modules.NavModule;
import team197.modules.RadioModule;


/** Simple rusher using the fight code.
 * Rushes towards the enemy base.  If enemies
 * are encountered, switches to fighting mode.
 */
public class PanicSoldierAI extends SoldierAI {
    private static final int MINE_DIST = 81;
    MapLocation target;
    int panic;

    public PanicSoldierAI(RobotController rc) {
        super(rc);
        target = rc.senseEnemyHQLocation();
        nav.setDestination(rc, rc.senseEnemyHQLocation());
    }

    public PanicSoldierAI(RobotController rc, SoldierAI oldme) {
    	super(rc, oldme);
        target = rc.senseEnemyHQLocation();
        nav.setDestination(rc, rc.senseEnemyHQLocation());
    }

    public AI act(RobotController rc) throws Exception {
        Direction d;

        // Check the panic channel.  If we're told to hold, then set
        //  destination to what the channel says and do so.
        if(Clock.getRoundNum()%15 == 1) {
            if((panic = radio.read(rc, RadioModule.CHANNEL_PANIC)) != 0) {
                if(panic == 0xFADCAD) {
                    nav.setDestination(rc, rc.senseEnemyHQLocation());
                } else {
                    target = new MapLocation(panic >>> 7, panic&0x7F);
                    nav.setDestination(rc, target);
                }
            }
        }

        // If there are enemies to fight, fight! Otherwise,
        // continue towards the enemy base
        if(rc.isActive()){
            if((d = fight.fightClosestRobot(rc)) == Direction.OMNI) {
                // If we're still holding, let's lay down some mines
                if(panic != 0xFADCAD &&
                   rc.getLocation().distanceSquaredTo(target) < MINE_DIST &&
                   rc.senseMine(rc.getLocation()) == null) {
                    rc.layMine();
                    return this;
                } else {
                    d = nav.moveFlock(rc, 2);
                }
            }

            moveSafe(rc, d);
        }

        // Keep the same ai for next round
        return this;
    }
}
