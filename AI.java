package team197;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import battlecode.common.MapLocation;
import team197.modules.MapModule;

import team197.modules.RadioModule;


/**
 * Abstract base class for a robot AI.  Each AI has an act
 *  function that is repeatedly called, with a call to yield
 *  inbetween.  The return value is used to switch strategies
 *  if the robot desires, a null return value will casue the
 *  robot to suicide.
 */
public abstract class AI {
	int sendconfo = 0;
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
    protected MapModule map;
    
    public AI(RobotController rc){
    	radio = new RadioModule();
    	map = new MapModule(rc);
    }

    public AI(RobotController rc,AI oldme) {
        radio = oldme.radio;
        map = oldme.map;
        num_heard = 0;
    }

    public void do_upkeep(RobotController rc){
        if(Clock.getRoundNum()%15 == 0)
            radio.write(rc, RadioModule.CHANNEL_CHECKIN, radio.readTransient(rc, RadioModule.CHANNEL_CHECKIN) + 1);
    }

    abstract public AI act(RobotController rc) throws Exception;
    
    public void broadcast_waypoints(RobotController rc, MapLocation waypoints, int curpoint, int totpoint, int channel_inp){

    	//Format:
    	// 0000000 0000000 00000 00000
    	//    x   |   y   | cur |tot
    	// * = marker to tell that the findpathAI that yes, this is its own message.
    		int msgbuf = 0;
    		//msgbuf = 1 << 23;
    		//System.out.print("1");
    		msgbuf = waypoints.x << 17;
    		//System.out.print(" " + waypoints[curpoint].x);
    		msgbuf += waypoints.y << 10;
    		//System.out.print(" " + waypoints[curpoint].y);
    		msgbuf += curpoint << 5;
    		//System.out.print(" " + curpoint +"\n");
    		msgbuf += totpoint;
    		//System.out.println(curpoint + " " + totpoint);
    		//System.out.println(((msgbuf>>>15)) +" " + ((msgbuf>>>8)&0x7F) + " " + ((msgbuf>>>4)&0xF));
    		radio.write(rc, channel_inp, msgbuf);
    		
    }
    
    
    public boolean hear_waypoints(RobotController rc, int channel){
    	
    		//System.out.println("I fired");
    		int message = radio.readTransient(rc, channel);
    		//System.out.println(((message >>> 5)&0x1F) + " " + ((message >>> 17)&0x7F) + " " + ((message >>> 10)&0x7F));
    		//System.out.println( (radio.read(rc, radio.CHANNEL_PATH_ENCAMP) >>> 15)&0x7F)
    		if(message != 0 && message >>> 17 != 0){ //&& message != 1 << 22){
	    		if(waypoint_heard == null){
	    			waypoint_heard = new MapLocation[message&0x1F];
		    		waypoint_heard[(message >>> 5)&0x1F] = new MapLocation((message >>> 17)&0x7F, (message >>> 10)&0x7F);
		    		num_heard += 1;
		    		System.out.println("I just got the point " + waypoint_heard[(message >>> 5)&0x1F].x + " " + waypoint_heard[(message >>> 5)&0x1F].y);
	    		} else if(num_heard == waypoint_heard.length){
	    			sendconfo = 1;
	    			return false;
	    		}else if(waypoint_heard[(message >>> 5)&0x1F] == null){
		    		waypoint_heard[(message >>> 5)&0x1F] = new MapLocation((message >>> 17)&0x7F, (message >>> 10)&0x7F);
		    		num_heard += 1;
		    		System.out.println("I just got the point " + waypoint_heard[(message >>> 5)&0x1F].x + " " + waypoint_heard[(message >>> 5)&0x1F].y);
		    	}
		    	return true;
    		
    		}
    		return true;
    }
}
