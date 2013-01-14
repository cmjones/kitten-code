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
    private static final double FLOCK_ALLY_WEIGHT = 16.0,
                                FLOCK_HORIZ_SCALE = 2.5;
    private static final int FLOCK_ALLY_RADIUS = 16;
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
     */
    public void setDestination(RobotController rc, MapLocation ml) {
        // Set the destination
        dest = ml;
        navRounds = 0;
    }

    // Helper function that finds a valid move direction, prioritizing
    //  the passed one.  Favors turning left, and returns Direction.NONE
    //  if no valid direction is found.
    private Direction findMove(RobotController rc, Direction d) {
        Direction d1, d2;

        if(rc.canMove(d)) return d;
        if(rc.canMove(d1 = d.rotateLeft())) return d1;
        if(rc.canMove(d2 = d.rotateRight())) return d2;
        if(rc.canMove(d1 = d1.rotateLeft())) return d1;
        if(rc.canMove(d2 = d2.rotateRight())) return d2;
        if(rc.canMove(d1 = d1.rotateLeft())) return d1;
        if(rc.canMove(d2 = d2.rotateRight())) return d2;
        if(rc.canMove(d = d.opposite())) return d;

        // No possible direction found
        return Direction.NONE;
    }

    /** Navigates directly to the destination.
     * Moves without bothering to navigate around mines.  The robot
     *  could be sensing mines external to this function and defusing
     *  them.
     */
    public Direction moveSimple(RobotController rc) {
        MapLocation cur;
        Direction d;

        // Grab the robot's current location, and increment NavRounds
        cur = rc.getLocation();
        navRounds++;

        // Check to see if we've arrived yet
        if(dest == null || (d = cur.directionTo(dest)) == Direction.OMNI)
            return Direction.OMNI;

        // Check directions to move, prioritizing forward
        return findMove(rc, d);
    }

    /** Navigates to the destination while keeping distance from allies.
     * Moves without navigating around mines towards the destination.
     *  Generally moves in the same direction as moveSimple, but is pushed
     *  away from allies within a certain radius.
     */
    public Direction moveFlock(RobotController rc) throws GameActionException {
        MapLocation cur, tmp;
        Robot[] allies;
        Direction d;
        double tx, ty, ux, uy, dx, dy, mult;

        // Grab the robot's current location and increment NavRounds
        cur = rc.getLocation();
        navRounds++;

        // Check to see if we've arrived yet
        if(dest == null || (d = cur.directionTo(dest)) == Direction.OMNI)
            return Direction.OMNI;

        // Grab a list of nearby allied robots.  If this array is blank,
        //  its fine!  The flock algorithm will just go straight to the
        //  destination.
        allies = rc.senseNearbyGameObjects(Robot.class, FLOCK_ALLY_RADIUS, rc.getTeam());

        // Record the direction towards the destination.
        dx = dest.x-cur.x;
        dy = dest.y-cur.y;

        // Step through the array of allies, moving away from each
        //  ally with a given weight.  Allies that are further away
        //  lend less weight towards the total.
        tx = 0;
        ty = 0;
        for(int i = 0; i < allies.length; i++) {
            tmp = rc.senseLocationOf(allies[i]);
            d = cur.directionTo(tmp);

            // Weight smaller distances closer to FLOCK_ALLY_WEIGHT
            mult = FLOCK_ALLY_WEIGHT*FLOCK_ALLY_RADIUS/(FLOCK_ALLY_RADIUS+cur.distanceSquaredTo(tmp));
            tx += -d.dx*mult;
            ty += -d.dy*mult;
        }

        // Now we want to accent the movement tangent to the direction towards
        //  the destination, to encourage fanning out.  So we need to calculate
        //  the horizontal and vertical components and scale the horizontal.
        mult = (tx*dx+ty*dy)/(dx*dx+dy*dy);
        ux = mult*dx;
        uy = mult*dy;
        dx += ux+(tx-ux)*FLOCK_HORIZ_SCALE;
        dy += uy+(ty-uy)*FLOCK_HORIZ_SCALE;

        // The final direction of movement is simply the direction closest
        //  to the calculated (dx, dy) offset.  If we can't move in that
        //  direction, don't move.
        d = cur.directionTo(cur.add((int)dx, (int)dy));
        if(d != Direction.OMNI)
            return findMove(rc, d);
        else
            return Direction.NONE;
    }
}
