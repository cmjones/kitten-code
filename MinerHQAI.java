package team197;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.Upgrade;
import battlecode.common.MapLocation;

import team197.modules.RadioModule;


/** Example HQ ai, obtained from the example code.
 *  Just spawns new soldiers.
 */
public class MinerHQAI extends HQAI {

	
    public MinerHQAI(RobotController rc) {
        super(rc);
    }

    public MinerHQAI(RobotController rc, HQAI oldme) {
        super(rc, oldme);
    }

    public AI act(RobotController rc) throws Exception {
    	
        if (rc.isActive()) {
            Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
            // First, work on the Pickaxe.
            if(rc.hasUpgrade(Upgrade.PICKAXE) != true){
                rc.researchUpgrade(Upgrade.PICKAXE);
            } else {
                if(Clock.getRoundNum()%15 == 1){
                    if(robotCount == 1){
                        makeRobot(rc, 0, AI.JOB_MINESWEEPER_L);
                    } else if(robotCount == 2){
                    	makeRobot(rc, 0, AI.JOB_MINESWEEPER_M);
                    } else if(robotCount == 3){
                    	makeRobot(rc, 0, AI.JOB_MINESWEEPER_R);
                    } else if(robotCount < 1){
                    	makeRobot(rc, 0, AI.JOB_STANDARD);
                    } else if(robotCount == 4){
                    	int msgbuf;
                    	int x = rc.senseAllEncampmentSquares()[0].x;
                    	int y = rc.senseAllEncampmentSquares()[0].y;
                    	
                    	msgbuf = x << 13;
                    	msgbuf += y << 6;
                    	msgbuf += AI.TOBUILD_GENERATOR;
                    	
                    	makeRobot(rc, msgbuf, AI.JOB_BUILDER);
                    } else{
                        rc.researchUpgrade(Upgrade.NUKE);
                    }
                }
            }
        }
        
        //ROBOT BUILD MESSAGE SYSTEM
        /*
         * Builder Robot:
        0000000 0000000 000000 0000
        	x	   y	 type   Job 
       |         data         | job |
        */
        // Keep the same ai for next round
        return this;
    }
}
