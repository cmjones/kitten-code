package team197;

import java.util.ArrayList;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.GameActionException;
import battlecode.common.Team;

import team197.modules.RadioModule;
import team197.modules.MapModule;


public class SwarmHQAI extends HQAI {
	int msgbuf=0;
	 public SwarmHQAI(RobotController rc) {
		 super(rc);
	 }
	 
	 public SwarmHQAI(RobotController rc, HQAI oldme) {
		 super(rc, oldme);
	 }
	 
	 public AI act(RobotController rc) throws GameActionException{
		 if(check_msgs[0] == 0){
			 requestPath(rc, enemyhqloc);
		 }

		 
		 if(rc.isActive()){
			 if(check_msgs[0] == 1){
				 msgbuf = RadioModule.CHANNEL_PATH_ENCAMP;
	    		 makeRobot(rc, msgbuf, AI.JOB_STANDARD);
			 }
		 }
		 
		 return this;
	 }

}
