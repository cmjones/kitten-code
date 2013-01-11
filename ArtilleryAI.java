package team197;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.RobotController;


/** Artillery AI that aims to do the most damage possible to enemies.
 * The artillery does a search in its attack radius for robots, then
 *  aims to do as much damage as possible while limiting damage to
 *  allies.  Enemies are analyzed to determine their shield ratings,
 *  and health.  The artillery tries not to waste damage done, and
 *  also prioritizes non-shielded enemies.
 */
public class ArtilleryAI extends AI {
    private int r;
    private double[][][] map;
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
        map = new double[2*r+1][2*r+1][2];
    }

    public AI act(RobotController rc) throws Exception {
        Robot[] robots;
        RobotInfo info;
        MapLocation best, tmp;
        int x, y;
        double damage, maxval, val;
        boolean fire;

        // If we aren't active, use this time to clear the map
        if(!rc.isActive()) {
            if(!clear) {
                // Clear out the map
                for(int i = 0; i <= 2*r; i++) {
                    for(int j = 0; j <= 2*r; j++) {
                        map[i][j][0] = 0;
                        map[i][j][1] = 0;
                    }
                }
                clear = true;
            }

            return this;
        }


        // Now grab a list of nearby robots and store them in the map.
        // Also check if there are any enemies in the first place.
        fire = false;
        robots = rc.senseNearbyGameObjects(Robot.class, r*r);
        if(robots.length > 0) clear = false;

        for(int i = 0; i < robots.length; i++) {
            // Grab the robot info
            info = rc.senseRobotInfo(robots[i]);

            // Calculate where in the array the info should be stored
            x = info.location.x-rc.getLocation().x+r;
            y = info.location.y-rc.getLocation().y+r;

            // And store it
            if(info.shields > info.energon)
                map[x][y][0] = .25;
            else
                map[x][y][0] = 1;

            if(info.team == rc.getTeam()) {
                map[x][y][0] *= -4;
            } else {
                // Found an enemy
                fire = true;

                // Mark the surrounding squares with a non-zero
                // health, so the next search doesn't bother with
                // squares not next to an enemy.
                for(int j = x-1; j <= x+1; j++)
                    for(int k = y-1; k <= y+1; k++)
                        if(j != x && k != y &&
                           j >= 0 && j < 2*r && k >= 0 && k < 2*r &&
                           map[j][k][1] == 0)
                            map[j][k][1] = -1;
            }

            map[x][y][1] = info.shields+info.energon;
        }

        // If there are no enemies, don't bother firing.
        if(!fire) return this;

        // Finally, walk through the array and find a suitable target,
        // using distance to break ties (prefer shooting closer).  We
        // can skip trying to shoot at the boundaries of the map, since
        // they shouldn't be within firing range anyway.  This allows
        // us to avoid index-out-of-bounds issues as well.
        best = null;
        maxval = 0;
        for(int i = 1; i <= 2*r-1; i++) {
            for(int j = 1; j <= 2*r-1; j++) {
                // Check that this shot would be in range and that this
                //  square is next to an enemy.
                if(map[i][j][1] == 0 ||
                   (i-r)*(i-r)+(j-r)*(j-r) > RobotType.ARTILLERY.attackRadiusMaxSquared)
                    continue;

                // Calculate the value of a shot at offset (i-r, j-r)
                val = 0;
                for(x = -1; x <= 1; x++) {
                    for(y = -1; y <= 1; y++) {
                        // Set the damage done to this location
                        damage = RobotType.ARTILLERY.attackPower;
                        if(x != 0 && y != 0)
                            damage *= GameConstants.ARTILLERY_SPLASH_RATIO;

                        // Damage is good, killing is good, waste is bad,
                        //  so apply a little detriment for excess damage
                        val += map[i+x][j+y][0]*(damage-Math.abs(map[i+x][j+y][1]-damage));
                    }
                }

                // Now check val against the current max
                tmp = rc.getLocation().add(i-r, j-r);
                if(best == null || val > maxval ||
                   (val == maxval &&
                   rc.getLocation().distanceSquaredTo(best) < rc.getLocation().distanceSquaredTo(tmp))) {
                    best = tmp;
                    maxval = val;
                }
            }
        }

        // If the best map location is not null, FIRE!
        if(best != null) rc.attackSquare(best);

        // Keep the same ai for next round
        return this;
    }
}
