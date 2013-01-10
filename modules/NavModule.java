package team197.modules;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
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
    private MapLocation dest;
    private int mapWidth,
                mapHeight;


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
    }

    /** Navigates directly to the destination.
     * Moves without bothering to navigate around mines.  The robot
     * could be sensing mines external to this function and defusing
     * them.
     */
    public Direction moveSimple(RobotController rc) {
        MapLocation cur;
        Direction d1, d2;

        // Grab the robot's current location.  If we're at the
        //  destination, return Direction.OMNI.
        cur = rc.getLocation();

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
}
