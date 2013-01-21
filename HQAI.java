package team197;
import java.util.ArrayList;


import battlecode.common.Clock;
import battlecode.common.RobotController;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.GameActionException;
import battlecode.common.Team;

import team197.modules.RadioModule;


/** Example HQ ai, obtained from the example code.
 *  Just spawns new soldiers.
 */
public class HQAI extends AI {
    protected int robotCount;
    private Direction enemyHQ;
	int[][] internalmap;
	int mapwidth;
	int mapheight;
	ArrayList<MapLocation> openset = new ArrayList<MapLocation>();
	ArrayList<MapLocation> closedset = new ArrayList<MapLocation>();
	ArrayList<MapLocation> havebeen = new ArrayList<MapLocation>();
	int gscore;
	int fscore;    
	Direction[] directions = new Direction[3];
	MapLocation enemyhqloc;
	MapLocation myhqloc;
	boolean mapmade = false;
	int[] check_msgs = new int[1];
	
	
 public HQAI(RobotController rc) {
	 super(rc);
        robotCount = 0;
        enemyHQ = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
    }

    public HQAI(RobotController rc, HQAI oldme) {
        super(oldme);
        robotCount = oldme.robotCount;
        enemyHQ = oldme.enemyHQ;
        internalmap = oldme.internalmap;
        mapwidth = oldme.mapwidth;
        mapheight = oldme.mapheight;
	directions[0] = enemyHQ;
	directions[1] = enemyHQ.rotateLeft();
	directions[2] = enemyHQ.rotateRight(); 
    enemyhqloc = rc.senseEnemyHQLocation();
	myhqloc = rc.senseHQLocation();
	for(int i = check_msgs.length - 1; --i >= 0;){
		check_msgs[i] = 0;
	}
	}
    
    public void makeInternalMap(RobotController rc){
        mapwidth = rc.getMapWidth();
        mapheight = rc.getMapHeight();
        internalmap = new int [mapwidth][mapheight];
        MapLocation[] badmineslocs = rc.senseNonAlliedMineLocations(new MapLocation(0,0),mapheight * mapheight + mapwidth * mapwidth);
        for(int i = 0; i < badmineslocs.length; i++){
        	internalmap[badmineslocs[i].x][badmineslocs[i].y] = 13;
        }
        
        
        MapLocation[] encamps = rc.senseAllEncampmentSquares();
        for(int i = 0; i < encamps.length; i ++){
        	internalmap[encamps[i].x][encamps[i].y] = 2;
        }
    }

    public void do_upkeep(RobotController rc) {
         if(Clock.getRoundNum()%15 == 1){
            robotCount = radio.read(rc, RadioModule.CHANNEL_CHECKIN);
            	for(int i = check_msgs.length - 1; i >= 0; i --){
	        	 if(check_msgs[i] != 0){
	        		 if(radio.read(rc,RadioModule.CHANNEL_PATH_ENCAMP) >>> 22 == 1){
	        			 check_msgs[i] = 0;
	        			 System.out.println("Heeey!");
	        		 } else {
	        			 radio.write(rc, RadioModule.CHANNEL_PATH_ENCAMP, check_msgs[i]);
	        			 System.out.println("No one has replied.");
	        		 }
	        	 }
	         }
         }
    }

    public AI act(RobotController rc) throws Exception {
    	makeInternalMap(rc);
        return new Begin1HQAI(rc, this);
    }
    
    public void makeRobot(RobotController rc, int data, int job) throws GameActionException{
        Team mineTeam;

    	int msgbuf = (data << 4) + job;
        radio.write(rc, RadioModule.CHANNEL_GETJOB, msgbuf);
        Direction dir = enemyHQ;
        do{
        	if(rc.canMove(dir) &&
                   ((mineTeam = rc.senseMine(rc.getLocation().add(dir))) == null ||
                    mineTeam == rc.getTeam())){
        		rc.spawn(dir);
        		break;
        	}
        } while((dir = dir.rotateLeft()) != enemyHQ);
    }
    

    public void requestPath(RobotController rc, MapLocation desti){
    	int buf = desti.x << 7;
    	buf += desti.y;
    	radio.write(rc, RadioModule.CHANNEL_PATH_ENCAMP, buf);
    	check_msgs[0] = buf;
    }
    
    
    
    
    
    
    public void make_path(RobotController rc){
		openset.add(myhqloc);
		int x = myhqloc.x;
		int y = myhqloc.y;
		gscore = internalmap[x][y];
		fscore = gscore + get_H(rc,myhqloc);
		MapLocation current;
		ArrayList<MapLocation> neighbors = new ArrayList<MapLocation>();
		int tentative_g;
		
		while(true){
			current = getlowestf_inopen(rc,fscore);
			x = current.x;
			y = current.y;
			havebeen.add(current);
                if(current.equals(enemyhqloc)){
				System.out.println("Finished!");
				return;
			} else {
				openset.remove(current);
				closedset.add(current);
				neighbors = get_neighbors(rc, current);
				
				int size = neighbors.size();
				for(int i = -1; ++i < size;){
					MapLocation neighbor = neighbors.get(i);
					tentative_g = gscore + internalmap[x][y];
					
                                        if(!closedset.contains(neighbor) ||
                                           tentative_g <= internalmap[x][y]){
						gscore = tentative_g;
						fscore = internalmap[x][y]
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
			if( internalmap[loc.x][loc.y] + get_H(rc,loc) + curf < lowest_f){
				lowest_f = internalmap[loc.x][loc.y] + get_H(rc,loc) + curf;
				lowestf_loc = loc;
			}
		}
		return lowestf_loc;
         }
    public int get_H(RobotController rc, MapLocation loc){
        return loc.distanceSquaredTo(enemyhqloc);
    }
	
	public ArrayList<MapLocation> get_neighbors(RobotController rc, MapLocation central){
		ArrayList<MapLocation> temp = new ArrayList<MapLocation>();
		for(int i = directions.length; --i > 0;){
                    MapLocation temploc = central.add(directions[i]);
	 	    if(temploc.y >= 0 &&
                         temploc.y < mapheight && temploc.x >= 0 && temploc.x < mapwidth){
		 	    temp.add(temploc);
		    }
                }
		return temp;
	}

}
