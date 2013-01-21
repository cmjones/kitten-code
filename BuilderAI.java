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
	int channel_listen;
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
			channel_listen = buildermessage >>> 6;
			buildingtype = buildermessage&0x3F;
		}
	}
	
	public AI act(RobotController rc) throws Exception {
		Direction d;
		d = nav.moveSimple(rc);
		
		// If we can't do anything, don't do anything
        if(!rc.isActive()) return this;
        
        if(hear_waypoints(rc, channel_listen) && waypoint_heard != null){
        	nav.setDestination(rc, waypoint_heard[waypoint_heard.length-1], waypoint_heard);
        }
        
        if(sendconfo == 1 && Clock.getRoundNum() % 15 == 0){
        	radio.write(rc, channel_listen, 1);
        }
        
        if(d != Direction.NONE && d != Direction.OMNI) {
            // If there's a mine, defuse it
            target = rc.getLocation().add(d);
            if(rc.senseMine(target) != null && rc.senseMine(target) != rc.getTeam())
                rc.defuseMine(target);
            else
                rc.move(d);
        }
        
        if(d == Direction.OMNI){
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
		
		return this;
		
	}
}
