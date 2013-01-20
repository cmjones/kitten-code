package team197;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.Upgrade;
import battlecode.common.MapLocation;
import battlecode.common.Team;

import java.util.ArrayList;
import java.util.List;


import team197.modules.RadioModule;


public class Begin1HQAI extends HQAI {	
	//Array of the 'breakpoints' regarding supplier production
	//DEF: breakpoint: point at which a breakpost actually causes a change in rounds per robot
	int[] sup_breakpoints = {1,2,4,6,9,13,19,31};
	//Temporary list to create bullshit encamps
	MapLocation[] close_encamps;
	MapLocation[] far_encamps;
	//Things that are actually going to exist + be used.
	MapLocation[] encamps_of_int;
	int numencamps_int;
	int msgbuf;
	int dist_btwn_far;
	int build_art_num;
	int close_encamps_num;
	int build_sup_num;
	boolean encamp_sup;
	
    public Begin1HQAI(RobotController rc) {
        super(rc);
    }

    public Begin1HQAI(RobotController rc, HQAI oldme) {
        super(rc, oldme);
        boolean encamp_sup = false;
    }
    
    public AI act(RobotController rc) throws Exception {
    	if(rc.isActive()){
    		//to keep the HQ from going OMG OMG OMG KEE PDOING THINGS AHAAAHAHAHHHHHHH
    		if(!encamp_sup){
	    		//This is where the search function will be... I just realized you never told me
	    		// a name for the function. So, uh, i'm just going to make an array here
	    		// that includes a scratched-up array of nearby encamps and a scratched-up
	    		// array of encamps in the (physical) center.
	    		//Until we make the real function, I suggest running on britain map.
	    		close_encamps = rc.senseEncampmentSquares(rc.senseHQLocation(), 100, Team.NEUTRAL);
	    		far_encamps = rc.senseEncampmentSquares(new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2),
	    				16, Team.NEUTRAL);
	    		encamps_of_int = new MapLocation[close_encamps.length + 2];
	    		numencamps_int = encamps_of_int.length;
	    		for(int i = 0; i < close_encamps.length; i ++){
	    			encamps_of_int[i] = close_encamps[i];
	    		}
	    		encamps_of_int[numencamps_int-2] = far_encamps[0];
	    		encamps_of_int[numencamps_int-1] = far_encamps[1];
	    		//From here on, will act like I don't have access to close_encamps[] and far_encamps[]
	    		//because those will be replaced by the search function's return.
	    		
	    		//Checks to see how far away the two far encamps are from eachother.
	    		//Depending on whether they're far or close, it builds one or two artillery--
	    		// close = 1, far = 2.
	    		int dist_btwn_far = encamps_of_int[numencamps_int-2].distanceSquaredTo(encamps_of_int[numencamps_int-1]);
	    		if(Math.round(Math.sqrt(dist_btwn_far)) > 1){
	    			build_art_num = 2;
	    		} else {
	    			build_art_num = 1;
	    		}
	    		
	    		//checks to see how many close encamps it saw and divides by three and checks to see
	    		//what's the closest breakpoint, and then saves that number to build that many suppliers
	    		close_encamps_num = encamps_of_int.length - 2;
	    		for(int i = 0; i < sup_breakpoints.length; i ++){
	    			if(close_encamps_num/3 < sup_breakpoints[i]){
	    				build_sup_num = sup_breakpoints[i-1];
	    				break;
	    			} else {
	    				build_sup_num = 0;
	    			}
	    		} 
	    		
	    		encamp_sup = true;
    		}
    		
    		//Checks to see whether it's been told to build 1 or 2 artilleries. If it was told to
    		// build one, goes to the first one and builds.
    		//Else, it sends one to the farther one first and then sends one to the closer one.
    		//System.out.println(build_art_num);
    		if(build_art_num == 1){
            	msgbuf = encamps_of_int[numencamps_int-2].x << 13;
            	msgbuf += encamps_of_int[numencamps_int-2].y << 6;
            	msgbuf += AI.TOBUILD_ARTILLERY;
            	build_art_num -= 1;
            	makeRobot(rc, msgbuf, AI.JOB_BUILDER);
    		} else if(build_art_num == 2){
            	msgbuf = encamps_of_int[numencamps_int-1].x << 13;
            	msgbuf += encamps_of_int[numencamps_int-1].y << 6;
            	msgbuf += AI.TOBUILD_ARTILLERY;
            	build_art_num -= 1;
            	makeRobot(rc, msgbuf, AI.JOB_BUILDER);
    		} else if(build_sup_num != 0){
    			System.out.println(build_sup_num-1 +" " + encamps_of_int[build_sup_num-1].x + " " + encamps_of_int[build_sup_num-1].y);
    			msgbuf = encamps_of_int[build_sup_num-1].x << 13;
    			msgbuf += encamps_of_int[build_sup_num-1].y << 6;
    			msgbuf += AI.TOBUILD_SUPPLIER;
    			makeRobot(rc, msgbuf, AI.JOB_BUILDER);
    			build_sup_num -= 1;
    		}
   		
    
    	}
    	return this;
    }
}
