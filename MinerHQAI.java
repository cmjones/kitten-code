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
            if(rc.hasUpgrade(Upgrade.PICKAXE) != true){
                rc.researchUpgrade(Upgrade.PICKAXE);
            } else {
                if(Clock.getRoundNum()%15 == 1){
                    if(robotCount == 1){
                        radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_MINESWEEPER_L);
                        if (rc.canMove(dir))
                                rc.spawn(dir);
                    } else if(robotCount == 2){
                        radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_MINESWEEPER_M);
                        if (rc.canMove(dir))
                                rc.spawn(dir);
                    } else if(robotCount == 3){
                        radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_MINESWEEPER_R);
                        if (rc.canMove(dir))
                                rc.spawn(dir);
                    } else if(robotCount < 3){
                        radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_STANDARD);
                        if (rc.canMove(dir))
                                rc.spawn(dir);
                    } else if(robotCount == 4){
                        radio.write(rc, RadioModule.CHANNEL_GETJOB, AI.JOB_BUILDER);
                        radio.write(rc, RadioModule.CHANNEL_BUILDER_DESTI, rc.senseAllEncampmentSquares()[0].x * 10000 + rc.senseAllEncampmentSquares()[0].y * 100 + TOBUILD_GENERATOR);
                        if(rc.canMove(dir))
                                rc.spawn(dir);
                    } else{
                        rc.researchUpgrade(Upgrade.NUKE);
                    }
                }
            }
        }

        // Keep the same ai for next round
        return this;
    }
}
