package team197.modules;

import battlecode.common.RobotController;
import battlecode.common.MapLocation;
import battlecode.common.Direction;


public class MapModule {
    public static final int MAX_WAYPOINTS = 10;

    private static final int GROUND_OPEN = 0,
                             GROUND_NEARMINE = 4,
                             GROUND_MINE = 12,
                             GROUND_ENCAMPMENT = 1; //Placeholder no; doesn't matter b/c will be checked against
    private static final Direction[] DIRECTIONS = new Direction[] {Direction.NORTH_WEST, Direction.NORTH, Direction.NORTH_EAST,
                                                                   Direction.WEST,                        Direction.EAST,
                                                                   Direction.SOUTH_WEST, Direction.SOUTH, Direction.SOUTH_EAST};

    private int[][] map_weights;
    private int mapwidth,
                mapheight,
                gscore,
                fscore;
    private MapLocation start,
                        end;
    boolean path_found;


    public MapModule(RobotController rc) {
        mapwidth = rc.getMapWidth();
        mapheight = rc.getMapHeight();
    }

    /** Resenses the weights array.
     * Clears the weights array as a side effect by instantializing
     *  a new array.  Java ensures that the newly created array will
     *  be filled by zeros.
     */
    private void recalc_weights(RobotController rc){
        MapLocation[] locs;
        int x, y;

        // Clear the map by creating a new array, avoiding the need
        // to walk through it.
        map_weights = new int[mapwidth][mapheight];

        locs = rc.senseNonAlliedMineLocations(new MapLocation(0,0),mapheight * mapheight + mapwidth * mapwidth);
        for(int i = 0; i < locs.length; i ++) {
            for(int j = -1; j <= 1; j++) {
                map_weights[locs[i].x][locs[i].y] = GROUND_MINE;
/*                for(int k = -1; k <= 1; k++) {
                    x = locs[i].x+j;
                    y = locs[i].y+k;
                    if(x >= 0 && x < mapwidth && y >= 0 && y < mapheight) {
                        if(j == 0 && k == 0)
                            map_weights[x][y] = GROUND_MINE;
                        else if(map_weights[x][y] == 0)
                            map_weights[x][y] = GROUND_NEARMINE;
                    }
                }*/
            }
        }
    }

    /** Searches for the shortest path between the start and end destinations.
     * This is a computationally intensive function that doesn't return until
     *  the computation is complete.  The returned MapLocation[] is guaranteed
     *  to be at most MAX_WAYPOINTS long, and should be a good approximation of
     *  the shortest path from start to end.
     */
    public MapLocation[] make_path(RobotController rc, MapLocation start, MapLocation end){
        PathNode[][] map_nodes;
        MapLocation[] retval;
        MapLocation curLoc;
        PathNode path,
                 cur,
                 tmp;
        int cost,
            estimate,
            skip;

        // First recalculate the weights for the map and initialize the closed set
        recalc_weights(rc);
        map_nodes = new PathNode[mapwidth][mapheight];
        System.out.println(" - Recalculated map weights\n");

        // We start with 'start' on the queue.  As long as there's a node
        //  on the queue, grab it and expand it.
        path = new PathNode(0, 0, start.distanceSquaredTo(end), start);
        map_nodes[path.loc.x][path.loc.y] = path;
        do {
            // Pop the current node off the queue.
            cur = path;
            path = path.pop();

            // If we've reached the destination, we're ready to read the
            //  resulting path!
            if(cur.loc.equals(end))
                break;

            // Now step through this squares neighbors and add any unvisited
            // locations to the priority queue.
            for(int i = 0; i < DIRECTIONS.length; i++) {
                // Calculate the new position
                curLoc = cur.loc.add(DIRECTIONS[i]);

                // Check if the next node is within the map bounds
                if(curLoc.x < 0 || curLoc.x >= mapwidth ||
                   curLoc.y < 0 || curLoc.y >= mapheight)
                    continue;

                // The new node should have a path cost of 1 + cur's path cost +
                //  the value in the weights array for this location.
                cost = cur.pathCost+map_weights[curLoc.x][curLoc.y]+1;
                estimate = cost+curLoc.distanceSquaredTo(end);

                // Check if this node is already in the queue.  If it is, and
                //  the new estimate is better than the old one, replace the old
                //  estimate.
                if(map_nodes[curLoc.x][curLoc.y] == null) {
                    tmp = new PathNode(cost, cur.pathLength+1, estimate, curLoc);
                    map_nodes[curLoc.x][curLoc.y] = tmp;
                } else if(estimate < map_nodes[curLoc.x][curLoc.y].estimateCost) {
                    tmp = map_nodes[curLoc.x][curLoc.y];
                    tmp.pathCost = cost;
                    tmp.estimateCost = estimate;
                    tmp.pathLength = cur.pathLength+1;

                    // Remove this node from its previous position
                    path = path.remove(tmp);
                } else {
                   continue;
                }

                // For this node, the next step in the final path would be the
                //  node we started with (cur)
                tmp.nextpath = cur;

                // Now add this node to the priority queue.
                if(path == null)
                    path = tmp;
                else
                    path = path.push(tmp);
            }
        } while(path != null);

        // cur should now contain end, and should form a path to
        //  start.  If there are less than MAX_WAYPOINTS steps, make
        //  each one a waypoint.  Otherwise, skip steps so that only
        //  MAX_WAYPOINTS steps are returned.
        //
        // When skipping, 'end' should be the last element of the
        //  the array, while it doesn't necessarily start with 'start'
        if(cur.pathLength < MAX_WAYPOINTS) {
            skip = 1;
            retval = new MapLocation[cur.pathLength];
        } else {
            skip = cur.pathLength/MAX_WAYPOINTS;
            retval = new MapLocation[MAX_WAYPOINTS];
        }

        // Now actually fill out the return value
        for(int i = retval.length-1; i >= 0; i--) {
            retval[i] = cur.loc;
            System.out.print("(" + retval[i].x + ", " + retval[i].y+")\n");

            // Skip 'skip' number of nodes
            for(int k = 0; k < skip; k++)
                cur = cur.nextpath;
        }

        // And we're done
        return retval;
    }
    
    public void shortpath(RobotController rc){
    	PathNode[][] map_nodes;
    	
    	map_nodes = new PathNode[mapwidth][mapheight];
    	//First need to find what, exactly, the rotational line of the map is.
    	//Checks to see what the direction from the center to the hq is, and bases its deduction from that.
    	//Also fills an array full of points along the line.
    	//Problem: Sometimes, even though the HQ is a diagonal from the center the movement AI still tells it to go north/east/south/west instead. Returns wrong
    	//		rotational line. Can be fixed by using distance-from-corner calculations instead. Might take up more memory.
    	MapLocation desti = new MapLocation(mapwidth -1, mapheight -1);
       	MapLocation temp = new MapLocation(0, 0);
       	int diagonal = (int)Math.sqrt(mapwidth * mapwidth + mapheight*mapheight);
    	MapLocation[] linelocs = new MapLocation[diagonal];
    	switch (temp.directionTo(rc.senseHQLocation())) {
    	case NORTH_EAST: case SOUTH_WEST:
    		temp = new MapLocation(0, 0);
    		desti = new MapLocation(mapwidth - 1, mapheight - 1);
    		break;
    	case NORTH: case SOUTH:
    		temp = new MapLocation(0, mapheight/2);
    		desti = new MapLocation(mapwidth - 1, mapheight/2);
    		break;
    	case NORTH_WEST: case SOUTH_EAST:
    		temp = new MapLocation(mapwidth-1,0);
    		desti = new MapLocation(0, mapheight - 1);
    		break;
    	case WEST: case EAST:
    		temp = new MapLocation(mapwidth/2,0);
    		desti = new MapLocation(mapwidth/2, mapheight - 1);
    		break;
    	}
     	linelocs[0] = temp;
     	for(int i = 1; i < diagonal; i ++){
     		temp = temp.add(temp.directionTo(desti));
     		linelocs[i] = temp;
     	}
    	
    	//I honestly don't know where to go from here. Sorry.
    	
    }
}


/** Node for a priority queue of map locations, used to find shortest paths.
 * Each node holds a reference to the next node in the queue as well as
 *  the next node in a possible path.  Once the algorithm is complete, following
 *  'nextPath' should bring you from the start to the end.
 */
class PathNode {
    PathNode nextpath,
             prev,
             next;
    MapLocation loc;
    int pathCost,
        pathLength,
        estimateCost;

    public PathNode(int pathCost, int pathLength, int estimateCost, MapLocation loc) {
        this.pathCost = pathCost;
        this.pathLength = pathLength;
        this.estimateCost = estimateCost;
        this.loc = loc;
    }

    /** Adds the given node to the list starting at this node.
     * Returns the head of the new list, which is either 'node'
     *  or this node.
     */
    public PathNode push(PathNode node) {
    	PathNode temp = this;
    	do{
	        if(node.estimateCost <= temp.estimateCost) {
	            // Handle previous links
	            if(temp.prev != null)
	                temp.prev.next = node;
	            node.prev = temp.prev;
	
	            // Handle next links
	            node.next = temp;
	            temp.prev = node;
	            if(temp == this){
	            	return node;
	            } else {
	            	return this;
	            }
	        }
	        if(temp.next != null){
	        	temp = temp.next;
	        } else {
	        	break;
	        }
    	}while(true);
	    // Handle appending to the end of the list
	    temp.next = node;
	    node.prev = temp;
	
	    return this;
    }

    /** Pops this node off of the list.
     * Returns the next node in the queue, which may be null.
     */
    public PathNode pop() {
        PathNode tmp;

        if(next != null)
            next.prev = prev;

        tmp = next;
        next = null;
        return tmp;
    }

    /** Removes the node from this list.
     * Returns the new head of the list.
     */
    public PathNode remove(PathNode node) {
        if(this == node)
            return pop();

        // Take care of the previous links
        if(node.prev != null)
            node.prev.next = node.next;
        node.prev = null;

        // Take care of the next links
        if(node.next != null)
            node.next.prev = node.prev;
        node.next = null;

        return this;
    }
}
