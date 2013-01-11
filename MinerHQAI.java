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
	int[][] internalmap;
	int mapwidth;
	int mapheight;
	
    public MinerHQAI() {
        super();
    }

    public MinerHQAI(HQAI oldme) {
        super(oldme);
    }

    public AI act(RobotController rc) throws Exception {
    	if(internalmap == null){
	        mapwidth = rc.getMapWidth();
	        mapheight = rc.getMapHeight();
	        internalmap = new int [mapwidth][mapheight];
	        MapLocation[] badmineslocs = rc.senseNonAlliedMineLocations(new MapLocation(0,0),mapheight * mapheight + mapwidth * mapwidth);
	        for(int i = 0; i < badmineslocs.length; i++){
	        	internalmap[badmineslocs[i].x][badmineslocs[i].y] = 1;
	        }
	        
	        rc.yield();
	        
	        MapLocation[] encamps = rc.senseAllEncampmentSquares();
	        for(int i = 0; i < encamps.length; i ++){
	        	internalmap[encamps[i].x][badmineslocs[i].y] = 2;
	        }
    	}
    	
        if (rc.isActive()) {
            Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
            // First, work on the Pickaxe.
            if(rc.hasUpgrade(Upgrade.PICKAXE) != true){
                rc.researchUpgrade(Upgrade.PICKAXE);
            } else {
                if(Clock.getRoundNum()%15 == 1){
                    if(robotCount == 1){
                        radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_MINESWEEPER_L);
                        if (rc.canMove(dir))
                                rc.spawn(dir);
                    } else if(robotCount == 2){
                        radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_MINESWEEPER_M);
                        if (rc.canMove(dir))
                                rc.spawn(dir);
                    } else if(robotCount == 3){
                        radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_MINESWEEPER_R);
                        if (rc.canMove(dir))
                                rc.spawn(dir);
                    } else if(robotCount < 1){
                        radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_STANDARD);
                        if (rc.canMove(dir))
                                rc.spawn(dir);
                    } else if(robotCount == 4){
                    	int msgbuf;
                    	int x = rc.senseAllEncampmentSquares()[0].x;
                    	int y = rc.senseAllEncampmentSquares()[0].y;
                    	
                    	msgbuf = x << 17;
                    	msgbuf += y << 10;
                    	msgbuf += AI.TOBUILD_GENERATOR << 4;
                    	msgbuf += AI.JOB_BUILDER;
                        radio.write(rc, RadioModule.CHANNEL_GETJOB, msgbuf);
                        if(rc.canMove(dir))
                                rc.spawn(dir);
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
