package team197;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Clock;

import team197.modules.NavModule;
import team197.modules.FightModule;


/** Example Soldier ai, obtained from the example code.
 * moves randomly and lays mines down
 */
public class MinesweeperAI extends SoldierAI {
    Direction d;
	MapLocation gotoloc;
    int type;

    boolean contcheck = true;
    public MinesweeperAI(RobotController rc, SoldierAI oldme, int type_get){
    	super(rc, oldme);
    	type = type_get;
    	gotoloc = rc.getLocation();
    	switch(type){
    	case AI.JOB_MINESWEEPER_L:
    		gotoloc = rc.getLocation().add(Direction.WEST,2).add(Direction.NORTH);
    		break;
    	case AI.JOB_MINESWEEPER_M:
    		gotoloc = rc.getLocation();
    		break;
    	case AI.JOB_MINESWEEPER_R:
    		gotoloc = rc.getLocation().add(Direction.EAST,2).add(Direction.NORTH);
    		break;
    	}
    	nav.setDestination(rc, gotoloc);
    	d = nav.moveSimple(rc);
    }

    public AI act(RobotController rc) throws Exception {
        

        MapLocation target;
        MapLocation checkup;
        MapLocation checkright;
        MapLocation checkdown;
        MapLocation checkleft;

        if(Clock.getRoundNum()%15 == 2 && contcheck == true){
           contcheck = hear_waypoints(rc);
        }
        
        // If we can't do anything, don't do anything
        if(!rc.isActive()) return this;

        // Grab the next direction to travel
        d = nav.moveSimple(rc);

        
        if(d != Direction.NONE && d != Direction.OMNI){

    		
            // If there's a mine, defuse it
            target = rc.getLocation().add(d);
            if(rc.senseMine(target) != null && rc.senseMine(target) != rc.getTeam()){
                rc.defuseMine(target);
            }
            else{
            	rc.move(d);
            }            			//System.out.println(radio.readTransient(rc, RadioModule.CHANNEL_CHECKIN));
        }
        else if(d == Direction.OMNI){
        	checkup = rc.getLocation().add(Direction.NORTH);
        	checkright = rc.getLocation().add(Direction.EAST);
        	checkdown = rc.getLocation().add(Direction.SOUTH);
        	checkleft = rc.getLocation().add(Direction.WEST);
        	if(rc.senseMine(checkup) != null && rc.senseMine(checkup) != rc.getTeam()){
        		rc.defuseMine(checkup);
        	}
        	else if(rc.senseMine(checkright) != null && rc.senseMine(checkright) != rc.getTeam()){
        		rc.defuseMine(checkright);
        	}
        	else if(rc.senseMine(checkdown) != null && rc.senseMine(checkdown) != rc.getTeam()){
        		rc.defuseMine(checkdown);
        	}
        	else if(rc.senseMine(checkleft) != null && rc.senseMine(checkleft) != rc.getTeam()){
        		rc.defuseMine(checkleft);
        	}
        	else {
            	rc.layMine();
            			nav.setDestination(rc, rc.getLocation().add(rc.getLocation().directionTo(rc.senseEnemyHQLocation()),2));
            		
        	}

        }
        // Keep the same ai for next round
        return this;
    }
}
