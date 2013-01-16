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


    public FindPathAI(RobotController rc){
        super();
        map = new MapModule(rc);
        message_sent = 1;
    }

    public AI act(RobotController rc) throws Exception {
        if(Clock.getRoundNum()%15 == 1){
            channelcheck = radio.readTransient(rc, radio.CHANNEL_PATH_ENCAMP);
            if(channelcheck != 1 && channelcheck != 0){
                System.out.println("Starting.");
                destination = new MapLocation(channelcheck >>> 7, channelcheck&0x1);
                message_sent = 0;
                waypoints = map.make_path(rc, rc.senseHQLocation(), destination);
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
