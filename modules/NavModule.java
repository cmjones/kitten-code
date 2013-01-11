package team197.modules;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;


/** Module for moving towards a destination.
 * After a destination is set, a robot can move towards it
 *  in a variety of ways.  Some methods will change tactics
 *  based on how long the robot has been navigating torwards
 *  the current goal.
 *
 * Each function returns the direction the robot should move
 *  in order to progress towards the destination. It is given
 *  that the direction returned should cause rc.canMove() to
 *  be true.
 *
 * Direction.NONE indicates that the robot shouldn't move,
 *  while returning NULL indicates that particular movement
 *  algorithm is stuck and knows it can't progress.
 *  Direction.OMNI indicates that the destination has been
 *  reached.
 */
public class NavModule {
    private static final int FLOCK_ALLY_BASE = 28,
                             FLOCK_ROUND_BASE = 4;
    private MapLocation dest;
    private int navRounds;

    public int mapWidth,
               mapHeight;


    /** Creates a new NavModule.
     */
    public NavModule(RobotController rc) {
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
    }

    /** Sets the current destination of this robot.
     * Resets any internal values.  Any move function may then
     *  be used to navigate towards the destination.
     *
     * Will check to ensure that the map location is reachable.
     */
    public void setDestination(RobotController rc, MapLocation ml) {
        // Set the destination
        dest = ml.add(0, 0);
        navRounds = 0;
    }

    /** Navigates directly to the destination.
     * Moves without bothering to navigate around mines.  The robot
     *  could be sensing mines external to this function and defusing
     *  them.
     */
    public Direction moveSimple(RobotController rc) {
        MapLocation cur;
        Direction d1, d2;

        // Grab the robot's current location, and increment NavRounds
        cur = rc.getLocation();
        navRounds++;

        // Check to see if we've arrived yet
        d1 = cur.directionTo(dest);
        if(d1 == Direction.OMNI) return d1;

        // Now check direction up to 90 degrees off from straight.
        if(rc.canMove(d1)) return d1;
        d2 = d1.rotateRight();
        if(rc.canMove(d2)) return d2;
        d1 = d1.rotateLeft();
        if(rc.canMove(d1)) return d1;
        d2 = d2.rotateRight();
        if(rc.canMove(d2)) return d2;
        d1 = d1.rotateLeft();
        if(rc.canMove(d1)) return d1;

        // We did our best
        return Direction.NONE;
    }

    /** Navigates to the destination while keeping distance from allies.
     * Moves without navigating around mines towards the destination.
     *  Generally moves in the same direction as moveSimple, but attempts
     *  to maximize distance between all allies within a certain radius.
     */
    public Direction moveFlock(RobotController rc) throws GameActionException {
        MapLocation cur, tmp;
        Direction[] dirs;
        MapLocation[] allies;
        int mini;
        double minval, val;

        // Grab the robot's current location and increment NavRounds
        cur = rc.getLocation();
        navRounds++;

        // Grab a list of nearby allied robots.  If this array is blank,
        //  its fine!  The flock algorithm will just go straight to the
        //  destination.
        Robot[] allyRobots = rc.senseNearbyGameObjects(Robot.class, FLOCK_ALLY_BASE, rc.getTeam());
        allies = new MapLocation[allyRobots.length];
        for(int i = 0; i < allyRobots.length; i++)
            allies[i] = rc.senseLocationOf(allyRobots[i]);

        // Search through 9 directions (not OMNI), preferring to move
        //  towards the destination.  This preferrence increases the
        //  longer the robot has been moving towards the current
        //  destination.
        //
        // Minimizes the following sum of values:
        //  - FLOCK_ALLY_BASE/(distance square) to each nearby ally
        //  - (rounds * distance square)/(FLOCK_ROUND_BASE+rounds)
        //    to the destination
        mini = -1;
        minval = 1000000;
        dirs = Direction.values();
        for(int i = 0; i < dirs.length; i++) {
            // Skip Direction.OMNI, and skip directions we can't move
            if(dirs[i] == Direction.OMNI ||
               (dirs[i] != Direction.NONE &&
               !rc.canMove(dirs[i])))
                continue;

            // Reset val and store the new location
            val = 0;
            tmp = cur.add(dirs[i]);

            // Loop through nearby allies
            for(int j = 0; j < allies.length; j++)
                val += FLOCK_ALLY_BASE/tmp.distanceSquaredTo(allies[j]);

            // Factor in the distance to the destination
            val += (navRounds*Math.pow(tmp.distanceSquaredTo(dest), .75))/(FLOCK_ROUND_BASE+navRounds);

            // Now check to see if we've found a good direction
            if(val < minval) {
                mini = i;
                minval = val;
            }
        }

        // Return the direction we've found, which should be at least Direction.NONE
        return dirs[mini];
    }
}
