package team197;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

import team197.modules.NavModule;
import team197.modules.FightModule;


/** Simple rusher using the fight code.
 * First, moves towrds a wait point.  Once enough
 *  allies are nearby, proceeds towards the kill
 *  point.  This should make fighters attack in
 *  waves.
 */
public class FighterAI extends SoldierAI {
    private static final int WAIT_DIST = 25,
                             WAIT_NUM = 10;

    MapLocation wait_point,
                kill_point;
    int channel_listen;
    boolean wait;

    public FighterAI(RobotController rc, int channel) {
        super(rc);
        setTargetPoints(rc);
        nav.setDestination(rc, wait_point);
        channel_listen = channel;
        wait = true;
    }

    public FighterAI(RobotController rc, FighterAI oldme) {
    	super(rc, oldme);
        wait_point = oldme.wait_point;
        kill_point = oldme.kill_point;
        wait = oldme.wait;
        nav.setDestination(rc, wait_point);
        channel_listen = oldme.channel_listen;
    }

    public FighterAI(RobotController rc, SoldierAI oldme, int channel) {
    	super(rc, oldme);
        setTargetPoints(rc);
        nav.setDestination(rc, wait_point);
        channel_listen = channel;
        wait = true;
    }

    private void setTargetPoints(RobotController rc) {
        MapLocation tmp;

        tmp = rc.senseHQLocation();

        // Set the kill point to be the enemy.
        kill_point = rc.senseEnemyHQLocation();

        // Set the wait point to be half-way
        //  between the hqs.
        wait_point = new MapLocation((tmp.x+kill_point.x)/2, (tmp.y+kill_point.y)/2);
    }

    public AI act(RobotController rc) throws Exception {
        Direction d;
        MapLocation target,
                    cur;

/*        if(channel_listen != 0 && waypoint_heard == null){
        	// Loop until waypoints start appearing
        	while(waypoint_heard == null) {
        		hear_waypoints(rc, channel_listen);
        		rc.yield();
        	}
        	// Loop to fill the waypoints array
        	do {
        		hear_waypoints(rc, channel_listen);
    			System.out.println("new desti set");
	        	nav.setDestination(rc, rc.senseEnemyHQLocation(), waypoint_heard);
	        	d = nav.moveSimple(rc);
	        	moveSafe(rc, d);
            	rc.yield();
        	} while (num_heard != waypoint_heard.length);

        	// Set destination one last time
        	//nav.setDestination(rc, waypoint_heard[waypoint_heard.length], waypoint_heard);
        	d = nav.moveSimple(rc);
        	moveSafe(rc, d);
        	rc.yield();
        }*/

        // If there are enemies to fight, fight! Otherwise,
        // continue towards the current destination
        if(rc.isActive()){
            // Get our location
            cur = rc.getLocation();

            if((d = fight.fightClosestRobot(rc)) == Direction.OMNI)
                d = nav.moveFlock(rc, wait?4:16);

            if(d != Direction.NONE && d != Direction.OMNI) {
                // If there's a mine, defuse it
                target = cur.add(d);
                if(rc.senseMine(target) != null && rc.senseMine(target) != rc.getTeam())
                    rc.defuseMine(target);
                else
                    moveSafe(rc, d);
            }

            // If we're within range of the wait point and there are
            //  a number of allies around us, move to the kill point
            if(wait && cur.distanceSquaredTo(wait_point) <= WAIT_DIST &&
               rc.senseNearbyGameObjects(Robot.class, wait_point, WAIT_DIST, rc.getTeam()).length >= WAIT_NUM) {
                wait = false;
                nav.setDestination(rc, kill_point);
            }
        }

        // Keep the same ai for next round
        return this;
    }
}
