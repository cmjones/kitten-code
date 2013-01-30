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
    private static final int MAX_ROUNDS = 5;

    int buildermessage;
    int xdesti;
    int ydesti;
    int buildingtype;
    MapLocation target;
    int channel_listen = 0;
    boolean hear_return;
    int bestdist;
    int roundsTrying;

    public BuilderAI(RobotController rc, SoldierAI oldme, int navdata){
            super(rc, oldme);
            buildermessage = navdata;
            if(buildermessage >>> 13 != 127){
                    xdesti = buildermessage >>> 13;
                    ydesti = (buildermessage >>> 6)&0x7F;
                    //System.out.println(xdesti + " " + ydesti);
                    buildingtype = buildermessage&0x3F;
                    target = new MapLocation(xdesti, ydesti);
                    bestdist = target.distanceSquaredTo(rc.getLocation());
                    nav.setDestination(rc,target);
            } else {
                    System.out.println(channel_listen);
                    channel_listen = (buildermessage >>> 6)&0x3F;
                    buildingtype = buildermessage&0x3F;
            }
    }

    public AI act(RobotController rc) throws Exception {
        Direction d;
        int dist;

        // If we can't do anything, don't do anything
        if(channel_listen != 0 && waypoint_heard == null){
            // Loop to hear waypoints from the HQ
            while(hear_waypoints(rc, channel_listen))
                rc.yield();

System.out.println("XXXXXXXXXXXXXXXXX  Heard all waypoints  XXXXXXXXXXXXXX");
            // Set the destination with the heard waypoints
            nav.setDestination(rc, waypoint_heard[waypoint_heard.length-1], waypoint_heard);

            // Step backwards through the waypoints to find the destination
            bestdist = 10000;
            for(int i = waypoint_heard.length-1; i >= 0; i--) {
                if(waypoint_heard[i] != null) {
                    target = waypoint_heard[i];
                    break;
                }
            }
            if(rc.isActive()) moveSafe(rc, nav.moveAStar(rc, map));
            rc.yield();
        }


        // Send confirmation if all the waypoints have been heard
        if(sendconfo == 1 && Clock.getRoundNum() % 15 == 0){
                radio.write(rc, channel_listen, 1);
        }

        // Now act
        if(rc.isActive()) {
            // Check current distance away from the destination.
            //  If we aren't getting closer for a number of rounds,
            //  give up and become a fighter.
            if(target != null) {
                dist = target.distanceSquaredTo(rc.getLocation());
                if(dist < bestdist) {
                    bestdist = dist;
                    roundsTrying = 0;
                } else if(roundsTrying >= MAX_ROUNDS) {
                    return new FighterAI(rc, this, 0);
                } else {
                    roundsTrying++;
                }
            }


            // If we're following waypoints, use the A* movement. Otherwise,
            //  just move directly
            if(waypoint_heard != null)
                d = nav.moveAStar(rc, map);
            else
                d = nav.moveSimple(rc);

            // Check to see if we've found our destination encampment
            if(d == Direction.OMNI && rc.senseEncampmentSquare(rc.getLocation()) == true){
                // Sense if we have enough power to capture this square
                if(rc.senseCaptureCost() < rc.getTeamPower()-10) {
                    // If we're low on energy, build a generator
                    //if(rc.getTeamPower() < 1.25*rc.senseCaptureCost())
                       // buildingtype = TOBUILD_GENERATOR;

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
            } else {
                moveSafe(rc, d);
            }
        }
        return this;
    }
}
