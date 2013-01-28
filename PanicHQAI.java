package team197;

import java.util.ArrayList;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.GameActionException;
import battlecode.common.Team;

import team197.modules.RadioModule;
import team197.modules.MapModule;


public class PanicHQAI extends HQAI {
    int msgbuf;

    public PanicHQAI(RobotController rc) {
        super(rc);

        msgbuf = 0;
    }

    public PanicHQAI(RobotController rc, PanicHQAI oldme) {
        super(rc, oldme);

        msgbuf = oldme.msgbuf;
    }

    public PanicHQAI(RobotController rc, HQAI oldme) {
        super(rc, oldme);

        msgbuf = 0;
    }


    public AI act(RobotController rc) throws GameActionException{
        if(check_msgs[0] == 0){
            requestPath(rc, enemyhqloc);
        }

        if(rc.isActive()){
            // Spam Fighters
            makeRobot(rc, msgbuf, AI.JOB_PANIC);
        }

        return this;
    }
}
