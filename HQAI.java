package team197;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.GameActionException;

import team197.modules.RadioModule;


/** Example HQ ai, obtained from the example code.
 *  Just spawns new soldiers.
 */
public class HQAI extends AI {
    protected int robotCount;
    private Direction enemyHQ;
	int[][] internalmap;
	int mapwidth;
	int mapheight;
    
    public HQAI(RobotController rc) {
        robotCount = 0;
        enemyHQ = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
    }

    public HQAI(RobotController rc, HQAI oldme) {
        super(oldme);
        robotCount = oldme.robotCount;
        enemyHQ = oldme.enemyHQ;
        internalmap = oldme.internalmap;
        mapwidth = oldme.mapwidth;
        mapheight = oldme.mapheight;
    }
    
    public void makeInternalMap(RobotController rc){
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

    public void do_upkeep(RobotController rc) {
         if(Clock.getRoundNum()%15 == 1)
            robotCount = radio.read(rc, RadioModule.CHANNEL_CHECKIN);
    }

    public AI act(RobotController rc) throws Exception {
    	makeInternalMap(rc);
        return new MinerHQAI(rc, this);
    }
    
    public void makeRobot(RobotController rc, int data, int job) throws GameActionException{
    	int msgbuf = (data << 4) + job;
        radio.write(rc, RadioModule.CHANNEL_GETJOB, msgbuf);
        Direction dir = enemyHQ;
        do{
        	if(rc.canMove(dir)){
        		rc.spawn(dir);
        		break;
        	}
        } while((dir = dir.rotateLeft()) != enemyHQ);
    }
}
