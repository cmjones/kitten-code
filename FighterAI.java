package team197;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

import team197.modules.FightModule;
import team197.modules.NavModule;
import team197.modules.RadioModule;


/** Simple rusher using the fight code.
 * First, moves towrds a wait point.  Once enough
 *  allies are nearby, proceeds towards the kill
 *  point.  This should make fighters attack in
 *  waves.
 */
public class FighterAI extends SoldierAI {
    private static final int WAIT_DIST = 30,
                             WAIT_NUM = 20,
                             WAIT_ROUNDS = 150;

    MapLocation wait_point,
                kill_point;
    private int roundsWaiting;
    boolean wait;

    public FighterAI(RobotController rc, int data) {
        super(rc);
        setTargetPoints(rc, data);
        nav.setDestination(rc, wait_point);
        wait = true;
    }

    public FighterAI(RobotController rc, FighterAI oldme) {
    	super(rc, oldme);
        wait_point = oldme.wait_point;
        kill_point = oldme.kill_point;
        wait = oldme.wait;
        nav.setDestination(rc, wait_point);
    }

    public FighterAI(RobotController rc, SoldierAI oldme, int data) {
    	super(rc, oldme);
        setTargetPoints(rc, data);
        nav.setDestination(rc, wait_point);
        wait = true;
    }

    private void setTargetPoints(RobotController rc, int data) {
        MapLocation tmp;
        int x, y, num;

        tmp = rc.senseHQLocation();

        // Set the kill point to be the enemy.
        kill_point = rc.senseEnemyHQLocation();

        // If either coordinate is 0, set the wait point to be the center
        //  of the map.  Otherwise, use the passed wait point.
        x = (data >>> 13);
        y = (data >>> 6)&0x7F;
        if(x == 0 || y == 0)
            wait_point = new MapLocation((tmp.x+kill_point.x)/2, (tmp.y+kill_point.y)/2);
        else
            wait_point = new MapLocation(data >>> 13, (data >>> 6)&0x7F);
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

        // If we wait too long, just move forward
        if(wait) {
            roundsWaiting++;
            if(roundsWaiting >= WAIT_ROUNDS) {
                wait = false;
                nav.setDestination(rc, kill_point);
                radio.write(rc, RadioModule.CHANNEL_CHARGE, 0xCADCAD);
            }
        }

        // If there are enemies to fight, fight! Otherwise,
        // continue towards the current destination
        if(rc.isActive()){
            // Get our location
            cur = rc.getLocation();

            // If we're within range of the wait point and there are
            //  a number of allies around us, move to the kill point
            if(wait && cur.distanceSquaredTo(wait_point) <= WAIT_DIST) {
                // Check the radio to see if we should charge
                if(radio.read(rc, RadioModule.CHANNEL_CHARGE) == 0xCADCAD ||
                   rc.senseNearbyGameObjects(Robot.class, wait_point, WAIT_DIST, rc.getTeam()).length >= WAIT_NUM) {
                    wait = false;
                    nav.setDestination(rc, kill_point);
                } else if(rc.senseMine(cur) == null) {
                    if((d = fight.fightClosestRobot(rc)) == Direction.OMNI) {
                        // Lay some mines while we're waiting
                        rc.layMine();
                        return this;
                    }

                    // Only move if we stay within the wait radius
                    if(wait_point.distanceSquaredTo(cur.add(d)) <= WAIT_DIST)
                        moveSafe(rc, d);
                    return this;
                }
            }

            // Move or fight
            if((d = fight.fightClosestRobot(rc)) == Direction.OMNI) {
                if(wait) {
                    d = nav.moveSimple(rc);
                } else {
                    d = nav.moveFlock(rc, 3);
                }
            }

            moveSafe(rc, d);
        }

        // Keep the same ai for next round
        return this;
    }
}
