package team197.modules;

import java.util.ArrayList;


import battlecode.common.RobotController;
import battlecode.common.MapLocation;
import battlecode.common.Clock;
import battlecode.common.Direction;


public class PathNode {
		int weight;
		MapLocation myloc;
		PathNode next;
		PathNode nextpath;
		
		public PathNode(int weight, MapLocation myloc){
			this.weight = weight;
			this.myloc = myloc;
		}
	

}
