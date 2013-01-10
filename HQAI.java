package team197;

import battlecode.common.Clock;
import battlecode.common.RobotController;

import team197.modules.RadioModule;


/** Example HQ ai, obtained from the example code.
 *  Just spawns new soldiers.
 */
public class HQAI extends AI {
    protected int robotCount;

    public HQAI() {
        robotCount = 0;
    }

    public HQAI(HQAI oldme) {
        super(oldme);
        robotCount = oldme.robotCount;
    }

    public void do_upkeep(RobotController rc) {
         if(Clock.getRoundNum()%15 == 1)
            robotCount = radio.read(rc, RadioModule.CHANNEL_CHECKIN);
    }

    public AI act(RobotController rc) throws Exception {
        return new MinerHQAI(this);
    }
}
