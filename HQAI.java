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
    private int lastCount;

    public AI act(RobotController rc) throws Exception {
        if(Clock.getRoundNum()%15 == 1)
            lastCount = radio.read(rc, RadioModule.CHANNEL_CHECKIN);

        if (rc.isActive()) {
            // Spawn a soldier
            Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
            if(lastCount < 4) {
            	if (rc.canMove(dir)) {
            		rc.spawn(dir);
                        lastCount++;
                }
            } else if(rc.hasUpgrade(Upgrade.DEFUSION) != true) {
                rc.researchUpgrade(Upgrade.DEFUSION);
            } else if(radio.read(rc, RadioModule.CHANNEL_CHECKIN) < 30){
            	if (rc.canMove(dir)) {
            		rc.spawn(dir);
                        lastCount++;
                }
            } else {
                rc.researchUpgrade(Upgrade.NUKE);
            }
        }

        // Keep the same ai for next round
        return this;
    }
}
