package team197;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import team197.modules.RadioModule;
import battlecode.common.Upgrade;
import battlecode.common.Clock;


/** Example HQ ai, obtained from the example code.
 *  Just spawns new soldiers.
 */
public class HQAI extends AI {
    public AI act(RobotController rc) throws Exception {
        if (rc.isActive()) {
            // Spawn a soldier
            Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
            if(Clock.getRoundNum()%15 == 1){
            if(radio.read(rc, RadioModule.CHANNEL_CHECKIN) < 10){
            	if (rc.canMove(dir))
            		rc.spawn(dir);
            }
            }
            else {
            	if(rc.hasUpgrade(Upgrade.PICKAXE) != true){
            		rc.researchUpgrade(Upgrade.PICKAXE);
            	}
            	else {
            		rc.researchUpgrade(Upgrade.NUKE);
            	}
            }
        }

        // Keep the same ai for next round
        return this;
    }
}
