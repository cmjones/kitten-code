package team197;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import team197.modules.RadioModule;
import battlecode.common.Upgrade;
import battlecode.common.Clock;


/** Example HQ ai, obtained from the example code.
 *  Just spawns new soldiers.
 */
public class MinerHQAI extends AI {
    public AI act(RobotController rc) throws Exception {
        if (rc.isActive()) {
            Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
            // First, work on the Pickaxe.
            	if(rc.hasUpgrade(Upgrade.PICKAXE) != true){
            		rc.researchUpgrade(Upgrade.PICKAXE);
            	}
            	else {
            		if(Clock.getRoundNum()%15 == 1){
	            		if(radio.read(rc, RadioModule.CHANNEL_CHECKIN) == 1){
	            			radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_MINESWEEPER_L);
	                    	if (rc.canMove(dir))
	                    		rc.spawn(dir);
	            		}
	            		else if(radio.read(rc, RadioModule.CHANNEL_CHECKIN) == 2){
	            			radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_MINESWEEPER_M);
	                    	if (rc.canMove(dir))
	                    		rc.spawn(dir);
	            		}
	            		else if(radio.read(rc, RadioModule.CHANNEL_CHECKIN) == 3){
	            			radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_MINESWEEPER_R);
	                    	if (rc.canMove(dir))
	                    		rc.spawn(dir);
	            		}
	            		else if(radio.read(rc, RadioModule.CHANNEL_CHECKIN) < 10){
	            			radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_STANDARD);
	                    	if (rc.canMove(dir))
	                    		rc.spawn(dir);
	            		}
	            		else{
	            			rc.researchUpgrade(Upgrade.NUKE);
	            		}

            		}
            	}
        }

        // Keep the same ai for next round
        return this;
    }
}
