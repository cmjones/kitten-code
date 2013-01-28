package team197.modules;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Upgrade;

import java.util.ArrayList;


/** Module for attacking targets.
 * This module has methods to move a robot about while
 *  attacking, which operate by sensing enemy and friendly
 *  robots and returning directions to position oneself
 *  accordingly.
 *
 * Direction.NONE indicates that the robot shouldn't move,
 *  while returning NULL indicates that the attack algorithm
 *  doesn't have anything to fight.
 */
public class FightModule {
    private static final Direction[] DIRECTIONS = new Direction[] {Direction.NORTH_WEST, Direction.NORTH, Direction.NORTH_EAST,
                                                                   Direction.WEST,       Direction.NONE,  Direction.EAST,
                                                                   Direction.SOUTH_WEST, Direction.SOUTH, Direction.SOUTH_EAST};
    private static final double STRAFE_ADJ_COST = 25;
    private static final double STRAFE_ALLY_BASE = 36;
    private static final double STRAFE_ENEMY_BASE = 36;
    private static final int IDLE_THRESH = 3;

    private MapLocation cur;
    private boolean wait;


    /** Creates a new FightModule
     */
    public FightModule() {
        wait = true;
    }


    /** Creates a list of directions that move the robot can move
     *   in order to be adjacent to one of the targets.
     * Returns an empty array if it is impossible to approach any
     *  target with a single move action.
     */
    private Direction[] getApproaches(RobotController rc, MapLocation[] targets) {
        ArrayList<Direction> dirs;

        // Create an array list to hold valid directions
        dirs = new ArrayList<Direction>();

        // Step through each possible direction
        //  and test if it is a valid direction.
        for(int i = 0; i < DIRECTIONS.length; i++) {
            // Test if the new direction can be moved to
            if(DIRECTIONS[i] != Direction.NONE && !rc.canMove(DIRECTIONS[i])) continue;

            // Test if the new location is adjacent to
            // any of the targets
            MapLocation m = cur.add(DIRECTIONS[i]);
            int j;
            for(j = 0; j < targets.length; j++)
                if(m.isAdjacentTo(targets[j])) break;
            if(j == targets.length) continue;

            // Test if the new location is located
            //  on a mine
            if(rc.senseMine(m) != rc.getTeam() && rc.senseMine(m) != null)
                continue;

            // This is a valid direction
            dirs.add(DIRECTIONS[i]);
        }

        // Finally, return the array
        return dirs.toArray(new Direction[0]);
    }

    /** Creates a list of directions that move the robot that can
     *   move in order to escape from nearby enemies.
     * Returns an empty array if any possible move brings it closer than
     *  2 steps away from an enemy  threat (which is no good, since being
     *  chased would damage us and not them).
     *
     * Threats are defined as active enemy soldiers.
     */
    private Direction[] getEscapes(RobotController rc, RobotInfo[] targets) {
        ArrayList<Direction> dirs;

        // Create an array list to hold valid directions
        dirs = new ArrayList<Direction>();

        // Step through each possible direction
        //  and test if it is a valid direction.
        for(int i = 0; i < DIRECTIONS.length; i++) {
            // Test if the new direction can be moved to
            if(DIRECTIONS[i] != Direction.NONE && !rc.canMove(DIRECTIONS[i])) continue;

            // Test if the new location is adjacent to
            // any threats
            MapLocation m = cur.add(DIRECTIONS[i]);
            int j;
            for(j = 0; j < targets.length; j++)
                if(isThreat(rc, targets[j]) &&
                   m.distanceSquaredTo(targets[j].location) <= 2)
                    break;
            if(j != targets.length) continue;

            // Test if the new location has a mine
            if(rc.senseMine(m) != rc.getTeam() && rc.senseMine(m) != null)
                continue;

            // This is a valid direction
            dirs.add(DIRECTIONS[i]);
        }

        return dirs.toArray(new Direction[0]);
    }

    /** Finds an optimum direction to move for fighting.
     * Returns a direction from a list that optimizes the following:
     *  - least number of adjacent enemies
     *  - most distance from nearby allies
     *  - most distance from nearby enemies
     *
     * If all directions place the robot adjacent to an enemy, then
     *  this function will pick the safest place to be.
     */
    private Direction strafe(RobotController rc, Direction[] dirs, MapLocation[] enemies, MapLocation[] allies) {
        MapLocation tmp;
        int besti;
        double bestval, val;

        // Loop through each direction and compute a score based
        // on the following:
        //  - Subtract STRAFE_ADJ_COST for each adjacent enemy
        //  - Add 36/(distance squared) for each nearby enemy
        //  - Add 36/(distance squared) to nearby allies
        // The lowest total score is the best location.
        besti = 0;
        bestval = 10000;
        for(int i = 0; i < dirs.length; i++) {
            val = 0;
            tmp = cur.add(dirs[i]);

            // First step through allies and add distance
            for(int j = 0; j < allies.length; j++)
                val += STRAFE_ALLY_BASE/tmp.distanceSquaredTo(allies[j]);

            // Next step through enemies.  Adjacent enemies subtract
            //  the adjacent cost (because they can attack), while
            //  non-adjacent enemies add their distance.
            for(int j = 0; j < enemies.length; j++)
                if(tmp.isAdjacentTo(enemies[j]))
                    val += STRAFE_ADJ_COST;
                else
                    val += STRAFE_ENEMY_BASE/tmp.distanceSquaredTo(enemies[j]);

            // If this score is better than the old best, record
            //  the score and the index.
            if(val < bestval) {
                besti = i;
                bestval = val;
            }
        }

        // Finally, return the computed best direction
        return dirs[besti];
    }

    /**
     * Determines if the given RobotInfo constitutes a threat.
     */
    private boolean isThreat(RobotController rc, RobotInfo info) {
        return info.type == RobotType.SOLDIER &&
               info.roundsUntilMovementIdle < IDLE_THRESH;
    }


    /** Moves towards the nearest enemy robot in sight.
     * Simply chases down the closest enemy and attacks it.  If the
     *  robot can move adjacent to one or more enemies, it chooses to
     *  move next to fewer enemies.
     */
    public Direction fightClosestRobot(RobotController rc) throws GameActionException {
        MapLocation[] enemies,
                      allies;
        MapLocation tmp;
        Direction[] dirs;
        Robot[] robots;

        // Store current location
        cur = rc.getLocation();

        // Get a list of nearby enemies
        robots = rc.senseNearbyGameObjects(Robot.class, 33, rc.getRobot().getTeam().opponent());

        // If there are no enemies, there is nothing to fight
        if(robots.length == 0) return Direction.OMNI;

        // Record enemy locations
        enemies = new MapLocation[robots.length];
        for(int i = 0; i < enemies.length; i++)
            enemies[i] = rc.senseLocationOf(robots[i]);

        // If we can move adjacent to someone, find the best way
        //  to do so.  Otherwise, find the closest enemy and move
        //  towards it.
        dirs = getApproaches(rc, enemies);
        if(dirs.length == 0) {
            // Find the closest enemy and move towards it, but try
            //  to stay 2 steps away (so we can appropach first)
            int mindist = 100;
            int mini = 0;
            int dist;
            for(int i = 0; i < enemies.length; i++) {
                if((dist = cur.distanceSquaredTo(enemies[i])) < mindist) {
                    mindist = dist;
                    mini = i;
                }
            }

            // Return the direction to the closest enemy if you can move
            // there.  If you can't, then this robot cannot move.
            Direction d = cur.directionTo(enemies[mini]);
            tmp = cur.add(d);
            if(rc.canMove(d)) {
                if(rc.senseRobotInfo(robots[mini]).type != RobotType.SOLDIER ||
                   (Math.abs(cur.x-tmp.x) > 2 && Math.abs(cur.y-tmp.y) > 2) ||
                   !wait) {
                    wait = true;
                    return d;
                } else {
                    wait = false;
                }
            }
            return Direction.NONE;
        } else if(dirs.length == 1) {
            // Only one direction to move, ATTACK!
            return dirs[0];
        } else {
            // Get a list of nearby allies
            robots = rc.senseNearbyGameObjects(Robot.class, 33, rc.getRobot().getTeam());
            allies = new MapLocation[robots.length];
            for(int i = 0; i < allies.length; i++)
                allies[i] = rc.senseLocationOf(robots[i]);

            // Now return the best direction to move
            return strafe(rc, dirs, enemies, allies);
        }
    }

    /** Fights by keeping away from enemies as long as possible while
     *   keeping them within sight.
     * Will rush towards buildings but stay away from soldiers unless they
     *  are doing something.  If the robot can't run, returns null to
     *  indicate that it needs to fight.  If there's nothing to run from,
     *  returns Direction.OMNI.
     */

    public Direction fightCoward(RobotController rc) throws GameActionException {
        RobotInfo[] info;
        Robot[] robots;
        int vision,
            j,
            bestdir_i;
        double bestdir_val,
               val;

        // First, calculate the robot's visual radius
        vision = RobotType.SOLDIER.sensorRadiusSquared;
        if(rc.hasUpgrade(Upgrade.VISION)) vision += GameConstants.VISION_UPGRADE_BONUS;

        // Store current location
        cur = rc.getLocation();

        // Now get a list of enemies nearby
        robots = rc.senseNearbyGameObjects(Robot.class, vision, rc.getTeam().opponent());

        // If there are no enemies, there is nothing to run from
        if(robots.length == 0) return Direction.OMNI;

        // Record enemy info
        info = new RobotInfo[robots.length];
        for(int i = 0; i < info.length; i++)
            info[i] = rc.senseRobotInfo(robots[i]);

        // Walk through the list of directions and find the best
        //  escape route
        bestdir_i = -1;
        bestdir_val = -10000;
        for(int i = 0; i < DIRECTIONS.length; i++) {
            // Test if the new direction can be moved to
            if(DIRECTIONS[i] != Direction.NONE && !rc.canMove(DIRECTIONS[i])) continue;

            // Test if the new location has a mine
            MapLocation m = cur.add(DIRECTIONS[i]);
            if(rc.senseMine(m) != rc.getTeam() && rc.senseMine(m) != null)
                continue;

            // Walk through the target array and calculate
            //  the value of moving in this direction.
            //  Prefer moving away from our hq
            val = Math.min(Math.sqrt(m.distanceSquaredTo(rc.senseHQLocation())),
                           Math.sqrt(rc.senseHQLocation().distanceSquaredTo(rc.senseEnemyHQLocation())));
            for(j = 0; j < info.length; j++) {
                if(isThreat(rc, info[j])) {
                    // If this movement takes us too close to a threat,
                    //  ignore it.
                    if(m.distanceSquaredTo(info[j].location) <= 4) {
                        val = -20000;
                        break;
                    } else {
                        // Prefer larger squared distances
                        val += Math.min(m.distanceSquaredTo(info[j].location), vision);
                    }
                } else {
                    // If this robot is not a threat, prefer to move towards it!
                    val += vision/Math.sqrt(m.distanceSquaredTo(info[j].location));
                }
            }

            // If the value of this direction is the best so far, record it
            if(val > bestdir_val) {
                bestdir_i = i;
                bestdir_val = val;
            }
        }

        // If we've gotten a best direction, move there.  Otherwise,
        //  we can't move and must fight.
        if(bestdir_i == -1)
            return null;
        else
            return DIRECTIONS[bestdir_i];
    }
}
