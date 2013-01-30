package team197;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import team197.modules.NavModule;
import team197.modules.FightModule;

public class DefenderAI extends SoldierAI {
	int radius;
	Direction[] directions;
	Direction tempdir;
	Robot[] temp;
    public DefenderAI(RobotController rc, SoldierAI oldme, int navdata){
    	super(rc, oldme);
    	radius = navdata;
    	directions = Direction.values();
    	nav.setDestination(rc, rc.senseHQLocation());
    }
    
	public AI act(RobotController rc) throws Exception {
		if(rc.isActive()){
			temp = rc.senseNearbyGameObjects(Robot.class, radius, rc.getTeam().opponent());
			if(temp.length == 0){
			tempdir = fight.fightCoward(rc);
			if(tempdir == Direction.OMNI){
				if(rc.senseMine(rc.getLocation()) != rc.getTeam()){
					//System.out.println(rc.senseMine(rc.getLocation()) + " " + rc.getTeam());
					rc.layMine();
				} else {
					tempdir = nav.moveFlock(rc, 2);
					//System.out.println("T_T");
						if(rc.senseHQLocation().distanceSquaredTo(rc.getLocation().add(tempdir)) < radius){
							moveSafe(rc, tempdir);
						}
				}
			} else if(tempdir != null){
				if(rc.senseHQLocation().distanceSquaredTo(rc.getLocation().add(tempdir)) <  radius){
					moveSafe(rc, tempdir);
				}
			} else if(tempdir == null){
				tempdir = fight.fightClosestRobot(rc);
				if(rc.senseHQLocation().distanceSquaredTo(rc.getLocation().add(tempdir)) <  radius){
					moveSafe(rc, tempdir);
				}
			}
			//System.out.println(rc.senseHQLocation().distanceSquaredTo(rc.getLocation().add(tempdir)) + " " + radius);
			} else {
				tempdir = fight.fightClosestRobot(rc);
						if(rc.senseHQLocation().distanceSquaredTo(rc.getLocation().add(tempdir)) <  radius){
							moveSafe(rc, tempdir);
						}
			}
			}
		return this;
	}
}
