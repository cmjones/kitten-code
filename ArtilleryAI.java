package team197;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.RobotController;

import java.util.List;
import java.util.LinkedList;


/** Artillery AI that aims to do the most damage possible to enemies.
 * The artillery does a search in its attack radius for robots, then
 *  aims to do as much damage as possible while limiting damage to
 *  allies.  Enemies are analyzed to determine their shield ratings,
 *  and health.  The artillery tries not to waste damage done, and
 *  also prioritizes non-shielded enemies.
 */
public class ArtilleryAI extends AI {
    private static final MapLocation COMP = new MapLocation(0, 0);
    private static final int KILL_BONUS = 40;
    private static final double ALLY_MULT = -4,
                                SHIELD_MULT = .1;

    private int r;
    private double[][] map;
    boolean clear;


    public ArtilleryAI() {
        // Set the radius of locations to check to be slightly
        // higher than the attack radius of Artillery.  This allows
        // the unit to judge splash damage.
        r = (int)Math.sqrt(RobotType.ARTILLERY.attackRadiusMaxSquared)+2;

        // Now create the map, which is a 2d array of 2 int values:
        //  the amount of health the robot has at that location, and
        //  a multiplier for priority (negative for allies, small for
        //  shielded enemies)
        map = new double[2*r+1][2*r+1];
    }

    public void clearMap() {
        // Clear out the map
        for(int i = 0; i < map.length; i++)
            for(int j = 0; j < map[i].length; j++)
                map[i][j] = 0;

        // Put ourselves on the map
        map[r][r] = -40;
        for(int i = -1; i <= 1; i++)
            for(int j = -1; j <= 1; j++)
                if(i == 0 && j == 0)
                    map[r+i][r+j] = -40;
                else
                    map[r+i][r+j] = -20;
    }

    /** Fills out values in the map using the given array of allies.
     * Does not attempt to find the maximum.
     */
    public void fillMapAllies(RobotController rc, Robot[] robots) throws Exception {
        RobotInfo info;
        MapLocation tmp;
        int x, y;
        double direct, splash;

        for(int i = 0; i < robots.length; i++) {
            // Do some sensing on the robot, and calculate it's position
            //  in the array.
            info = rc.senseRobotInfo(robots[i]);
            tmp = info.location.add(r-rc.getLocation().x, r-rc.getLocation().y);

            // Calculate the damage for attacking this square
            // If the robot is shielded, damage is less useful
            // If the damage would kill the robot, there's a bonus
            direct = RobotType.ARTILLERY.attackPower;
            splash = direct*GameConstants.ARTILLERY_SPLASH_RATIO;
            direct = (Math.max(direct-info.shields, 0) +
                      Math.min(direct, info.shields)*SHIELD_MULT +
                      ((direct > info.shields+info.energon)?KILL_BONUS:0))*ALLY_MULT;
            splash = (Math.max(splash-info.shields, 0) +
                      Math.min(splash, info.shields)*SHIELD_MULT +
                      ((splash > info.shields+info.energon)?KILL_BONUS:0))*ALLY_MULT;

            // Now add these damages to the array.  The current square
            //  should get the direct bonus, while the surrounding squares
            //  should get the splash bonus.
            for(int j = 1; j <= 1; j++) {
                x = tmp.x+j;
                for(int k = -1; k <= 1; k++) {
                    y = tmp.y+k;
                    if(x >= 0 && x < map.length && y >= 0 && y < map[x].length) {
                        if(j == 0 && k == 0)
                            map[x][y] += direct;
                        else
                            map[x][y] += splash;
                    }
                }
            }
        }
    }

    public AI act(RobotController rc) throws Exception {
        Robot[] robots, allies;
        RobotInfo info;
        MapLocation tmp;
        int x, y, maxx, maxy;
        double direct, splash, maxval;

        // If we aren't active, use this time to clear the map
        if(!rc.isActive()) {
            if(!clear) {
                clearMap();
                clear = true;
            }

            return this;
        }

        // Grab a list of nearby enemies.  If there are none, don't
        //  bother doing anything else.
        robots = rc.senseNearbyGameObjects(Robot.class, r*r, rc.getTeam().opponent());
        if(robots.length == 0)
            return this;
        else
            clear = false;

        // Now grab a list of nearby allies and fill out the map with
        //  them first.
        allies = rc.senseNearbyGameObjects(Robot.class, r*r, rc.getTeam());
        fillMapAllies(rc, allies);

        // Step through the list, computing the value of attacking each square
        maxx = maxy = -1000;
        maxval = 0;
        for(int i = 0; i < robots.length; i++) {
            // Do some sensing on the robot, and calculate it's position
            //  in the array.
            info = rc.senseRobotInfo(robots[i]);
            tmp = info.location.add(r-rc.getLocation().x, r-rc.getLocation().y);

            // Calculate the damage for attacking this square
            // If the robot is shielded, damage is less useful
            // If the damage would kill the robot, there's a bonus
            direct = RobotType.ARTILLERY.attackPower;
            splash = direct*GameConstants.ARTILLERY_SPLASH_RATIO;
            direct = Math.max(direct-info.shields, 0) +
                     Math.min(direct, info.shields)*SHIELD_MULT +
                     ((direct > info.shields+info.energon)?KILL_BONUS:0);
            splash = Math.max(splash-info.shields, 0) +
                     Math.min(splash, info.shields)*SHIELD_MULT +
                     ((splash > info.shields+info.energon)?KILL_BONUS:0);

            // Now add these damages to the array.  The current square
            //  should get the direct bonus, while the surrounding squares
            //  should get the splash bonus.  Calculate the maximum at the
            //  same time.
            for(int j = 1; j <= 1; j++) {
                x = tmp.x+j;
                for(int k = -1; k <= 1; k++) {
                    y = tmp.y+k;
                    if(x >= 0 && x < map.length && y >= 0 && y < map[x].length) {
                        if(j == 0 && k == 0)
                            map[x][y] += direct;
                        else
                            map[x][y] += splash;

                        if(map[x][y] > maxval || (map[x][y] == maxval &&
                           (x-r)*(x-r)+(y-r)*(y-r) < maxx*maxx+maxy*maxy)) {
                            maxx = x-r;
                            maxy = y-r;
                            maxval = map[x][y];
                        }
                    }
                }
            }
        }

        // If we've found a good coordinate, fire!
        if(maxx != -1000) {
            rc.setIndicatorString(0, "Last attack at (" + maxx + ", " + maxy + ")");
            rc.attackSquare(rc.getLocation().add(maxx, maxy));
        }

        // Keep the same ai for next round
        return this;
    }
}
