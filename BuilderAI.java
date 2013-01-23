package team197;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import team197.modules.RadioModule;
import battlecode.common.Clock;

import team197.modules.NavModule;
import team197.modules.FightModule;

public class BuilderAI extends SoldierAI {
	int buildermessage;
	int xdesti;
	int ydesti;
	int buildingtype;
	MapLocation target;
	int channel_listen = 0;
	boolean hear_return;
	public BuilderAI(RobotController rc, SoldierAI oldme, int navdata){
		super(rc, oldme);
		buildermessage = navdata;
		if(buildermessage >>> 13 != 127){
			xdesti = buildermessage >>> 13;
			ydesti = (buildermessage >>> 6)&0x7F;
			//System.out.println(xdesti + " " + ydesti);
			buildingtype = buildermessage&0x3F;
			target = new MapLocation(xdesti, ydesti);
			nav.setDestination(rc,target);
		} else {
			System.out.println(channel_listen);
			channel_listen = (buildermessage >>> 6)&0x3F;
			buildingtype = buildermessage&0x3F;
		}
	}
	
	public AI act(RobotController rc) throws Exception {
		Direction d;
		d = nav.moveSimple(rc);
		
		// If we can't do anything, don't do anything
        if(channel_listen != 0 && waypoint_heard == null){
        	// Loop until waypoints start appearing
        	while(waypoint_heard == null) {
        		hear_waypoints(rc, channel_listen);
        		rc.yield();
        	}
        	// Loop to fill the waypoints array
        	do {
        		hear_waypoints(rc, channel_listen);
    			System.out.println("new desti set");
	        	nav.setDestination(rc, waypoint_heard[num_heard - 1], waypoint_heard);
	        	d = nav.moveSimple(rc);
	        	if(d != Direction.NONE){
	        		moveSafe(rc,d);
	        	}
            	rc.yield();
        	} while (num_heard != waypoint_heard.length);
        	
        	// Set destination one last time
        	//nav.setDestination(rc, waypoint_heard[waypoint_heard.length], waypoint_heard);
        	d = nav.moveSimple(rc);
        	if(d != Direction.NONE){
        		moveSafe(rc,d);
        	}
        	rc.yield();
        }
//        if((waypoint_heard == null || num_heard != waypoint_heard.length)){
//        	//hear_waypoints(rc, channel_listen);
//        	hear_return = hear_waypoints(rc, channel_listen);
//        	//System.out.println("meh");
//        }
//        if(hear_return && waypoint_heard != null){
//        	System.out.println("new destination set");
//        	nav.setDestination(rc, waypoint_heard[waypoint_heard.length-1], waypoint_heard);
//        }
        
        if(sendconfo == 1 && Clock.getRoundNum() % 15 == 0){
        	radio.write(rc, channel_listen, 1);
        }
        if(rc.isActive()) {
	        if(d != Direction.NONE && d != Direction.OMNI) {
	            // If there's a mine, defuse it
	            target = rc.getLocation().add(d);
	            if(rc.senseMine(target) != null && rc.senseMine(target) != rc.getTeam())
	                rc.defuseMine(target);
	            else
	                rc.move(d);
	        }
	        
	        if(d == Direction.OMNI && rc.senseEncampmentSquare(rc.getLocation()) == true){
	        	switch(buildingtype){
	        	case TOBUILD_GENERATOR:
	        		rc.captureEncampment(RobotType.GENERATOR);
	        		break;
	        	case TOBUILD_ARTILLERY:
	        		rc.captureEncampment(RobotType.ARTILLERY);
	        		break;
	        	case TOBUILD_MEDBAY:
	        		rc.captureEncampment(RobotType.MEDBAY);
	        		break;
	        	case TOBUILD_SHIELDS:
	        		rc.captureEncampment(RobotType.SHIELDS);
	        		break;
	        	case TOBUILD_SUPPLIER:
	        		rc.captureEncampment(RobotType.SUPPLIER);
	        		break;
	        	}
	        }
        }
		return this;
		
	}
}
