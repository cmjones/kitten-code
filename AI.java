package team197;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import battlecode.common.MapLocation;

import team197.modules.RadioModule;


/**
 * Abstract base class for a robot AI.  Each AI has an act
 *  function that is repeatedly called, with a call to yield
 *  inbetween.  The return value is used to switch strategies
 *  if the robot desires, a null return value will casue the
 *  robot to suicide.
 */
public abstract class AI {

	MapLocation[] waypoint_heard;
	int num_heard;
    public static final int JOB_STANDARD = 1,
                            JOB_MINESWEEPER_L = 2,
                            JOB_MINESWEEPER_M = 3,
                            JOB_MINESWEEPER_R = 4,
                            JOB_BUILDER = 5;

    public static final int TOBUILD_GENERATOR = 1,
    		                TOBUILD_ARTILLERY = 2,
    		                TOBUILD_MEDBAY = 3,
    		                TOBUILD_SHIELDS = 4,
    		                TOBUILD_SUPPLIER = 5;

    protected RadioModule radio;

    public AI() {
        radio = new RadioModule();
    }

    public AI(AI oldme) {
        radio = oldme.radio;
    }

    public void do_upkeep(RobotController rc){
        if(Clock.getRoundNum()%15 == 0)
            radio.write(rc, RadioModule.CHANNEL_CHECKIN, radio.readTransient(rc, RadioModule.CHANNEL_CHECKIN) + 1);
    }

    abstract public AI act(RobotController rc) throws Exception;
    
    public void broadcast_waypoints(RobotController rc, MapLocation waypoints, int curpoint, int totpoint){

    	//Format:
    	// 00 0000000 0000000 0000 0000
    	// * |   x   |   y   |cur |tot
    	// * = marker to tell that the findpathAI that yes, this is its own message.
    		int msgbuf = 0;
    		//msgbuf = 1 << 23;
    		//System.out.print("1");
    		msgbuf = waypoints.x << 15;
    		//System.out.print(" " + waypoints[curpoint].x);
    		msgbuf += waypoints.y << 8;
    		//System.out.print(" " + waypoints[curpoint].y);
    		msgbuf += curpoint << 4;
    		//System.out.print(" " + curpoint +"\n");
    		msgbuf += totpoint;
    		//System.out.println(curpoint + " " + totpoint);
    		//System.out.println(((msgbuf>>>15)) +" " + ((msgbuf>>>8)&0x7F) + " " + ((msgbuf>>>4)&0xF));
    		radio.write(rc, radio.CHANNEL_PATH_ENCAMP, msgbuf);
    		
    }
    
    //THIS FUNCTION WORKS
    //IT JUST FIRES AT RANDOM ROUNDS!!!! (angry face here)
    
    public boolean hear_waypoints(RobotController rc){
    	
    		//System.out.println("I fired");
    		int message = radio.read(rc, radio.CHANNEL_PATH_ENCAMP);
    		//System.out.println( (radio.read(rc, radio.CHANNEL_PATH_ENCAMP) >>> 15)&0x7F)
    		if(message != 0 && message != 1 << 22){
	    		if(waypoint_heard == null){
	    			waypoint_heard = new MapLocation[message&0xF];
	    		} else if(num_heard == waypoint_heard.length){
	    			return false;
	    		}else if(waypoint_heard[(message >>> 4)&0xF] == null){
		    		waypoint_heard[(message >>> 4)&0xF] = new MapLocation((message >>> 15)&0x7F, (message >>> 8)&0x7F);
		    		num_heard += 1;
		    		System.out.println("I just got the point " + waypoint_heard[(message >>> 4)&0xF].x + " " + waypoint_heard[(message >>> 4)&0xF].y);
		    	}
		    	return true;
    		
    		}
    		return true;
    }
}
