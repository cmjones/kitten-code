package team197;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.Upgrade;

import team197.modules.RadioModule;


/** Example HQ ai, obtained from the example code.
 *  Just spawns new soldiers.
 */
public class MinerHQAI extends HQAI {
    public MinerHQAI() {
        super();
    }

    public MinerHQAI(HQAI oldme) {
        super(oldme);
    }

    public AI act(RobotController rc) throws Exception {
        if (rc.isActive()) {
            Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
            // First, work on the Pickaxe.
            if(robotCount == 0){
                radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_MINESWEEPER_L);
                if (rc.canMove(dir)) {
                        rc.spawn(dir);
                        robotCount++;
                }
            } else if(robotCount <= 5){
                radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_BUILDER);
                radio.write(rc, RadioModule.CHANNEL_BUILDER_DESTI, rc.senseAllEncampmentSquares()[robotCount].x * 10000 + rc.senseAllEncampmentSquares()[robotCount].y * 100 + TOBUILD_SUPPLIER);
                if (rc.canMove(dir)) {
                        rc.spawn(dir);
                        robotCount++;
                }
            } else{
                radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_STANDARD);
                if (rc.canMove(dir)) {
                        rc.spawn(dir);
                        robotCount++;
                }
            }
        }

        // Keep the same ai for next round
        return this;
    }
}
