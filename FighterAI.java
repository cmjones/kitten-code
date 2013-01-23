package team197;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import team197.modules.NavModule;
import team197.modules.FightModule;


/** Simple rusher using the fight code.
 * Rushes towards the enemy base.  If enemies
 * are encountered, switches to fighting mode.
 */
public class FighterAI extends SoldierAI {
	int channel_listen;
    public FighterAI(RobotController rc, int channel) {
        super(rc);
        //nav.setDestination(rc, rc.senseEnemyHQLocation());
        channel_listen = channel;
    }

    public FighterAI(RobotController rc, SoldierAI oldme, int channel){
    	super(rc, oldme);
       // nav.setDestination(rc, rc.senseEnemyHQLocation());
        channel_listen = channel;
    }

    public AI act(RobotController rc) throws Exception {
        Direction d = Direction.NONE;
        MapLocation target;
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
	        	moveSafe(rc, d);
            	rc.yield();
        	} while (num_heard != waypoint_heard.length);
        	
        	// Set destination one last time
        	//nav.setDestination(rc, waypoint_heard[waypoint_heard.length], waypoint_heard);
        	d = nav.moveSimple(rc);
        	moveSafe(rc, d);
        	rc.yield();
        }
        
        // If we can't do anything, don't do anything

        // If there are enemies to fight, fight! Otherwise,
        // continue towards the enemy base

        if(d != Direction.NONE && d != Direction.OMNI) {
            // If there's a mine, defuse it
            target = rc.getLocation().add(d);
            if(rc.senseMine(target) != null && rc.senseMine(target) != rc.getTeam())
                rc.defuseMine(target);
            else
                moveSafe(rc, d);
        }

        // Keep the same ai for next round
        return this;
    }
}
