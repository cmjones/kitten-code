package team197;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import team197.modules.RadioModule;

import team197.modules.NavModule;
import team197.modules.FightModule;

public class BuilderAI extends SoldierAI {
	int buildermessage;
	int xdesti;
	int ydesti;
	int buildingtype;
	MapLocation target;
	public BuilderAI(RobotController rc, SoldierAI oldme, int navdata){
		super(rc, oldme);
		buildermessage = navdata;
		xdesti = buildermessage >>> 13;
		ydesti = (buildermessage >>> 6)&0x7F;
		//System.out.println(xdesti + " " + ydesti);
		buildingtype = buildermessage&0x3F;
		target = new MapLocation(xdesti, ydesti);
		nav.setDestination(rc,target);
	}
	
	public AI act(RobotController rc) throws Exception {
		Direction d;
		d = nav.moveSimple(rc);
		
		// If we can't do anything, don't do anything
        if(!rc.isActive()) return this;
        
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
