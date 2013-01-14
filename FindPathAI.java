package team197;

import java.util.ArrayList;

import battlecode.common.RobotController;
import battlecode.common.MapLocation;
import battlecode.common.Clock;
import battlecode.common.Direction;

//IN PROGRESS: Reducing from 3D array to 2D array.
//Will have the format of:
// x, y, groundval
//The groundval will be ground_open, ground_nearmine, ground_mine, or ground_encampment.
//ground_encampments are unwalkable, as you can't walk on a built encampment. That is why it is necessary.



public class FindPathAI extends AI {
	int[][] internalmap_3D;
	int mapwidth;
	int mapheight;
	MapLocation myhqloc;
	MapLocation enemyhqloc;
	MapLocation destination;
	boolean path_found = false;
	ArrayList<MapLocation> finalpath = new ArrayList<MapLocation>();
	public static final int NUM_WAYPOINTS = 10;
	MapLocation[] finalwaypoints = new MapLocation[NUM_WAYPOINTS];
	ArrayList<MapLocation> openset = new ArrayList<MapLocation>();
	ArrayList<MapLocation> closedset = new ArrayList<MapLocation>();
	ArrayList<MapLocation> havebeen = new ArrayList<MapLocation>();
	int gscore = 0;
	int fscore = 0;
        Direction[] directions;
        int channelcheck;
        int message_sent = 1;
        
                
	//These go into the 3rd dimension of the array.
	public static final int GROUND_OPEN = 1,
							GROUND_NEARMINE = 5,
							GROUND_MINE = 13,
                                GROUND_ENCAMPMENT = 2; //Placeholder no; doesn't matter b/c will be checked against
	
	public FindPathAI(RobotController rc){
		super();
		mapwidth = rc.getMapWidth();
		mapheight = rc.getMapHeight();
		internalmap_3D = new int[mapwidth][mapheight];
		myhqloc = rc.senseHQLocation();
		enemyhqloc = rc.senseEnemyHQLocation();
		//destination = enemyhqloc;
		bore_map(rc);
		refresh_map(rc);
	        directions = Direction.values();
        }
	
	public void bore_map(RobotController rc){
		//Fill in with temporary placeholders of "the world is boring"
		for(int i = 0; i < mapwidth; i ++){
			for(int j = 0; j < mapheight; j++){
				internalmap_3D[i][j] = GROUND_OPEN;
			}
		}
	}
	
	public void refresh_map(RobotController rc){
		MapLocation[] badmineslocs = rc.senseNonAlliedMineLocations(new MapLocation(0,0),mapheight * mapheight + mapwidth * mapwidth);
		for(int i = 0; i < badmineslocs.length; i ++){
			internalmap_3D[badmineslocs[i].x][badmineslocs[i].y] = GROUND_MINE;
		}
	}
	
	public AI act(RobotController rc) throws Exception {
		//System.out.println(enemyhqloc);
		if(Clock.getRoundNum()%15 == 1){
			channelcheck = radio.readTransient(rc, radio.CHANNEL_PATH_ENCAMP);
			if(channelcheck != 1 && channelcheck != 0){
				System.out.println("Starting.");
				destination = new MapLocation(channelcheck >>> 7, channelcheck&0x1);
				message_sent = 0;
				make_path(rc);
			}
		}
		if(Clock.getRoundNum()%15 == 0 && message_sent == 0){
			radio.write(rc, radio.CHANNEL_PATH_ENCAMP, 1);
			message_sent = 1;
		}
		//if(Clock.getRoundNum() % 5 == 0){
		//	bore_map(rc);
		//	refresh_map(rc);
		//}
		return this;
	}
	
	public void make_path(RobotController rc){
		openset.add(myhqloc);
		gscore = internalmap_3D[myhqloc.x][myhqloc.y];
		fscore = gscore + get_H(rc,myhqloc);
		MapLocation current;
		ArrayList<MapLocation> neighbors = new ArrayList<MapLocation>();
		int tentative_g;
		
		while(true){
			current = getlowestf_inopen(rc,fscore);
			havebeen.add(current);
                        if(current.equals(destination)){
				System.out.println("Finished!");
				return;
			} else {
				openset.remove(current);
				closedset.add(current);
				neighbors = get_neighbors(rc, current);
				
				int size = neighbors.size();
				for(int i = -1; ++i < size;){
					MapLocation neighbor = neighbors.get(i);
					tentative_g = gscore + internalmap_3D[neighbor.x][neighbor.y];
					
                                        if(!closedset.contains(neighbor) ||
                                           tentative_g <= internalmap_3D[neighbor.x][neighbor.y]){
						gscore = tentative_g;
						fscore = internalmap_3D[neighbor.x][neighbor.y]
                                                   + get_H(rc,neighbor);
						if(!openset.contains(neighbor)){
							openset.add(neighbor);
						}
					}
				}
			}
		}
	}
	
	public MapLocation getlowestf_inopen(RobotController rc, int curf){
		int lowest_f = Integer.MAX_VALUE;
		MapLocation lowestf_loc = null;
		int size = openset.size();
		for(int i = -1; ++i < size;){
			MapLocation loc = openset.get(i);
			if( internalmap_3D[loc.x][loc.y] + get_H(rc,loc) + curf < lowest_f){
				lowest_f = internalmap_3D[loc.x][loc.y] + get_H(rc,loc) + curf;
				lowestf_loc = loc;
			}
		}
		return lowestf_loc;
	}

    public int get_H(RobotController rc, MapLocation loc){
        return loc.distanceSquaredTo(destination);
    }
	
	public ArrayList<MapLocation> get_neighbors(RobotController rc, MapLocation central){
		ArrayList<MapLocation> temp = new ArrayList<MapLocation>();
		for(int i = directions.length; --i > 0;){
                    MapLocation temploc = central.add(directions[i]);
	 	    if(temploc.y >= 0 && temploc.x >= 0 && internalmap_3D[temploc.x][temploc.y] != GROUND_ENCAMPMENT && 
                         temploc.y < mapheight &&  temploc.x < mapwidth){
		 	    temp.add(temploc);
		    }
                }
		return temp;
	}

	
	public void make_waypoints(RobotController rc){
		int dist_btwnpnts = finalpath.size() / NUM_WAYPOINTS;
		for(int i = 0; i < NUM_WAYPOINTS; i ++){
			finalwaypoints[i] = finalpath.get(1 + dist_btwnpnts * i);
		}
		for(int i = 0; i < NUM_WAYPOINTS; i ++){
			System.out.print(finalwaypoints[i].x + "x " + finalwaypoints[i].y+"y ");
		}
		System.out.println();
	}
	
}
