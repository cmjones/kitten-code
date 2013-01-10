package team197;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import team197.modules.NavModule;


/** Example Soldier ai, obtained from the example code.
 * moves randomly and lays mines down
 */
public class MinesweeperAI extends AI {
    private NavModule nav;
    /*
    Direction diradvance;
    Direction dirleft;
    Direction dirright;
    Direction dirmove;*/
    Direction d;
    int type;
    public MinesweeperAI(RobotController rc, int type_get) {
        nav = new NavModule(rc);
        nav.setDestination(rc, rc.senseEnemyHQLocation());
        d = nav.moveSimple(rc);
        nav.setDestination(rc, rc.getLocation().add(d,2));
    }
    
    public MinesweeperAI(RobotController rc, MapLocation gotoloc, int type_get){
    	nav = new NavModule(rc);
    	nav.setDestination(rc, gotoloc);
    	d = nav.moveSimple(rc);
    }

    public AI act(RobotController rc) throws Exception {
        

        MapLocation target;
        MapLocation checkup;
        MapLocation checkright;
        MapLocation checkdown;
        MapLocation checkleft;


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
