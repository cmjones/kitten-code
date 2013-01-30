package team197;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Upgrade;


public class MineFieldHQAI extends HQAI {
	MapLocation[] allencamps;
	MapLocation myhqloc;
	MapLocation enemyhqloc;
	int closestencamp;
	int radius;
	int num_defenders;
	MapLocation[] encampsnear;
	int curencamp;
	MapLocation[] farther_encamps;
	int maxencamp;
	
	public MineFieldHQAI(RobotController rc) {
		super(rc);
	}
	
	public MineFieldHQAI(RobotController rc, HQAI oldme) {
		super(rc, oldme);
        enemyhqloc = rc.senseEnemyHQLocation();
        myhqloc = rc.getLocation();
		allencamps = rc.senseAllEncampmentSquares();
		curencamp = 0;
		closestencamp = -1;
		num_defenders = 10;
		docount = true;
		maxencamp = 10;
	}
	
	public int findClosestEncamp(){
		int closest = Integer.MAX_VALUE;
		int temp = 0;
		for(int i = 0; i < allencamps.length; i ++){
			if(myhqloc.distanceSquaredTo(allencamps[i]) < closest){
				closest = myhqloc.distanceSquaredTo(allencamps[i]);
				temp = i;
			}
		}
		return temp;
	}
	
	public AI act(RobotController rc) throws Exception {
		   if(closestencamp == -1){
			   closestencamp = findClosestEncamp();
			   if(myhqloc.distanceSquaredTo(allencamps[closestencamp]) < myhqloc.distanceSquaredTo(enemyhqloc)){
				   radius = myhqloc.distanceSquaredTo(allencamps[closestencamp]) + 4;
				   if(radius < 16){
					   radius = 16;
				   }
			   } else {
				   radius = myhqloc.distanceSquaredTo(enemyhqloc)/3;
			   }
			   encampsnear = rc.senseEncampmentSquares(myhqloc, 4 * radius, null);
		   }
		   
		   if(encampsnear.length != 0 && curencamp != encampsnear.length && rc.isActive() && curencamp < maxencamp){
			  
			   desti = encampsnear[curencamp];
			   if(!desti.equals(myhqloc.add(Direction.NORTH_EAST)) && !desti.equals(myhqloc.add(Direction.NORTH_WEST)) && !desti.equals(myhqloc.add(Direction.SOUTH_EAST)) && !desti.equals(myhqloc.add(Direction.SOUTH_WEST))){
	           	msgbuf = desti.x << 13;
	           	msgbuf += desti.y << 6;
	           	msgbuf += AI.TOBUILD_ARTILLERY;
			   makeRobot(rc, msgbuf, AI.JOB_BUILDER);
			   }
			   curencamp ++;
		   } else if(robotCount - artCount < num_defenders && rc.isActive()){
			   //System.out.println(robotCount);
			   makeRobot(rc, radius, AI.JOB_DEFENDER);
		   } else if (rc.isActive()) {
			   rc.researchUpgrade(Upgrade.NUKE);
		   }
		   
		   
		   return this;
	}
}
