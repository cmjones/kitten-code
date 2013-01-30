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
                            JOB_PANIC = 5,
                            JOB_SCOUT = 6,
                            JOB_BUILDER = 7,
                            JOB_FIGHTER = 8;

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

    public AI do_upkeep(RobotController rc){
        if(Clock.getRoundNum()%15 == 0){
            switch(rc.getType()) {
            case ARTILLERY:
            	 radio.write(rc, RadioModule.CHANNEL_ART_CHECK, radio.readTransient(rc, RadioModule.CHANNEL_ART_CHECK) + 1);
            	break;
            case MEDBAY:
            	 radio.write(rc, RadioModule.CHANNEL_MED_CHECK, radio.readTransient(rc, RadioModule.CHANNEL_MED_CHECK) + 1);
            	 break;
            case GENERATOR:
            	 radio.write(rc, RadioModule.CHANNEL_GEN_CHECK, radio.readTransient(rc, RadioModule.CHANNEL_GEN_CHECK) + 1);
            	 break;
            case SUPPLIER:
            	 radio.write(rc, RadioModule.CHANNEL_SUP_CHECK, radio.readTransient(rc, RadioModule.CHANNEL_SUP_CHECK) + 1);
            	 break;
            case SHIELDS:
            	 radio.write(rc, RadioModule.CHANNEL_SHI_CHECK, radio.readTransient(rc, RadioModule.CHANNEL_SHI_CHECK) + 1);
            	 break;
            case SOLDIER:
                radio.write(rc, RadioModule.CHANNEL_CHECKIN, radio.readTransient(rc, RadioModule.CHANNEL_CHECKIN) + 1);

                // Check the panic channel.  If we're panicking, become a PanicSoldier
                if(radio.read(rc, RadioModule.CHANNEL_PANIC) != 0 && !(this instanceof PanicSoldierAI))
                    return new PanicSoldierAI(rc, (SoldierAI)this);
                break;
            }
        }

        return this;
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
        int message,
            tot,
            cur,
            x,
            y;
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++");

        // Grab the next message.  If it is zero, something was corrupted
        if((message = radio.readTransient(rc, channel)) == 0)
            return waypoint_heard != null && num_heard == waypoint_heard.length;

        // Decode the message
        tot = message&0x1F;
        message = message >>> 5;
        cur = message&0x1F;
        message = message >>> 5;
        y = message&0x7F;
        message = message >>> 7;
        x = message&0x7F;

        System.out.println("Message heard: " + cur + "/" + tot + " (" + x + ", " + y + ")");
        //System.out.println( (radio.read(rc, radio.CHANNEL_PATH_ENCAMP) >>> 15)&0x7F)
        if(x != 0) {
            // Check if we don't know about this path yet
            if(waypoint_heard == null || tot != waypoint_heard.length){
                waypoint_heard = new MapLocation[tot];
                num_heard = 0;
                System.out.println("Hearing a new set of waypoints, length = " + waypoint_heard.length);
            }

            // Store the new waypoint if we haven't heard it yet
            if(waypoint_heard[cur] == null) {
                waypoint_heard[cur] = new MapLocation(x, y);
                num_heard += 1;
                System.out.println("I just got the point " + waypoint_heard[cur] + ": (total heard = " + num_heard + ")");
            }

            // Check if we've heard all of the waypoints
            if(num_heard == waypoint_heard.length){
                sendconfo = 1;
                return false;
            }
        }

        // Still more waypoints to hear
        return true;
    }
}
