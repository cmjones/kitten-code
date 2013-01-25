package team197;

import battlecode.common.RobotController;
import battlecode.common.MapLocation;
import battlecode.common.Clock;
import battlecode.common.Direction;

import team197.modules.MapModule;


//IN PROGRESS: Reducing from 3D array to 2D array.
//Will have the format of:
// x, y, groundval
//The groundval will be ground_open, ground_nearmine, ground_mine, or ground_encampment.
//ground_encampments are unwalkable, as you can't walk on a built encampment. That is why it is necessary.



public class FindPathAI extends AI {
    private MapModule map;
    private MapLocation[] waypoints;
    private MapLocation destination;
    int channelcheck;
    int message_sent;
	int curpoint;
	int totpoint;


    public FindPathAI(RobotController rc){
        super(rc);
        map = new MapModule(rc);
        message_sent = 1;
    }

    public AI act(RobotController rc) throws Exception {
        if(waypoints != null){
        	broadcast_waypoints(rc, waypoints[curpoint], curpoint, totpoint,radio.CHANNEL_PATH_ENCAMP);
        	System.out.println(waypoints[curpoint]);
        	if(curpoint < totpoint - 1){
        		curpoint += 1;
        	} else {
        		curpoint = 0;
        	}
        }
        if(Clock.getRoundNum()%15 == 1){
            channelcheck = radio.readTransient(rc, radio.CHANNEL_PATH_ENCAMP);
            //System.out.println(channelcheck >>> 22);
            if( channelcheck != 0 && waypoints == null){
            	curpoint = 0;
            	//map.shortpath(rc);
                System.out.println("Starting.");
               destination = new MapLocation(channelcheck >>> 13, (channelcheck >>> 6)&0x7F);
                message_sent = 0;
                waypoints = map.findPath(rc, rc.senseHQLocation(), destination,3);
                totpoint = waypoints.length;
                System.out.println("Finished!");
            }
        }

        if(Clock.getRoundNum()%15 == 0 && message_sent == 0){
                radio.write(rc, radio.CHANNEL_PATH_ENCAMP, 1);
                message_sent = 1;
        }

        return this;
    }
}
