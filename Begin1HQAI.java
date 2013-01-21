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
	MapLocation[][] encamps_of_int;
	int numencamps_int;
	int msgbuf;
	int dist_btwn_far;
	int build_art_num;
	int curbuild_art_num;
	int close_encamps_num;
	int build_sup_num;
	boolean encamp_sup;
	int curpoint_1;
	int curpoint_2;
	int totpoint_1;
	int totpoint_2;
	int sendmsg_2;
	int sendmsg_1;
	
    public Begin1HQAI(RobotController rc) {
        super(rc);
    }

    public Begin1HQAI(RobotController rc, HQAI oldme) {
        super(rc, oldme);
        boolean encamp_sup = false;
        curpoint_1 = 0;
        curpoint_2 = 0;
    }
    
    public AI act(RobotController rc) throws Exception {
    	if(rc.isActive()){
    		//to keep the HQ from going OMG OMG OMG KEE PDOING THINGS AHAAAHAHAHHHHHHH
    		if(!encamp_sup){
	    		encamps_of_int = map.findEncampments(rc, 10);
	    		while(encamps_of_int == null || encamps_of_int[1] == null){
	    			rc.yield();
	    		}
	    		numencamps_int = 12;
	    		totpoint_1 = encamps_of_int[1].length;
	    		totpoint_2 = encamps_of_int[2].length;
	    		//Checks to see how far away the two far encamps are from eachother.
	    		//Depending on whether they're far or close, it builds one or two artillery--
	    		// close = 1, far = 2.
	    		int dist_btwn_far = encamps_of_int[1][encamps_of_int[1].length - 1].distanceSquaredTo(encamps_of_int[2][encamps_of_int[2].length - 1]);
	    		if(Math.round(Math.sqrt(dist_btwn_far)) > 4){
	    			build_art_num = 2;
	    		} else {
	    			build_art_num = 1;
	    		}
	    		
	    		//checks to see how many close encamps it saw and divides by three and checks to see
	    		//what's the closest breakpoint, and then saves that number to build that many suppliers
	    		close_encamps_num = numencamps_int - 2;
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
    			msgbuf = 127 << 13;
            	msgbuf += radio.CHANNEL_WAYPOINTS_1 << 6;
            	msgbuf += AI.TOBUILD_ARTILLERY;
            	build_art_num -= 1;
            	makeRobot(rc, msgbuf, AI.JOB_BUILDER);
            	sendmsg_1 = 1;
    		} else if(build_art_num == 2){
    			msgbuf = 127 << 13;
                msgbuf += radio.CHANNEL_WAYPOINTS_2 << 6;
            	msgbuf += AI.TOBUILD_ARTILLERY;
            	build_art_num -= 1;
            	sendmsg_2 = 1;
            	makeRobot(rc, msgbuf, AI.JOB_BUILDER);
    		} else if(build_sup_num != 0){
    			//System.out.println(build_sup_num-1 +" " + encamps_of_int[build_sup_num-1].x + " " + encamps_of_int[build_sup_num-1].y);
    			msgbuf = encamps_of_int[0][build_sup_num - 1].x << 13;
    			msgbuf += encamps_of_int[0][build_sup_num-1].y << 6;
    			msgbuf += AI.TOBUILD_SUPPLIER;
    			makeRobot(rc, msgbuf, AI.JOB_BUILDER);
    			build_sup_num -= 1;
    		}
    		
    		if(Clock.getRoundNum() % 15 == 1){
	    		if(sendmsg_2 == 1){
	    			if(radio.read(rc, radio.CHANNEL_WAYPOINTS_2) != 1){
	    				if(curpoint_2 < totpoint_2){
	    					curpoint_2 += 1;
	    				} else {
	    					curpoint_2 = 0;
	    				}
	                	broadcast_waypoints(rc, encamps_of_int[1][curpoint_2], curpoint_2, totpoint_2);
	    			} else {
	    				sendmsg_2 = 0;
	    			}
	    		}
	    		if(sendmsg_1 == 1){
	    			if(radio.read(rc, radio.CHANNEL_WAYPOINTS_1) != 1){
	    				if(curpoint_1 < totpoint_1){
	    					curpoint_1 += 1;
	    				} else {
	    					curpoint_1 = 0;
	    				}
	                	broadcast_waypoints(rc, encamps_of_int[2][curpoint_1], curpoint_1, totpoint_1);
	    			} else {
	    				sendmsg_1 = 0;
	    			}
	    		}
   		
    		}
    	}
    	return this;
    }
}
