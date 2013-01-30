package team197;

import java.util.ArrayList;

import battlecode.common.Clock;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.GameActionException;
import battlecode.common.Team;
import battlecode.common.Upgrade;

import team197.modules.RadioModule;
import team197.modules.MapModule;


public class SwarmHQAI extends HQAI {
    private static final int DEFUSION_THRESH = 5,
                             ROBOT_THRESH = 14,
                             ENEMY_THRESH = 10,
                             PANIC_ROUND = 850;
    int msgbuf;
    boolean upgraded;

    public SwarmHQAI(RobotController rc) {
        super(rc);
        msgbuf = 0;
    }

    public SwarmHQAI(RobotController rc, HQAI oldme) {
        super(rc, oldme);
        msgbuf = oldme.msgbuf;
    }

    private void getUpgrade(RobotController rc, Upgrade upgrade) throws GameActionException {
        do {
            if(rc.isActive()) {
                if(robotCount >= ROBOT_THRESH)
                    rc.researchUpgrade(upgrade);
                else
                    makeSwarmer(rc);
            }

            rc.yield();
            do_upkeep(rc);
        } while(!rc.hasUpgrade(upgrade));
    }

    private void checkUpgrades(RobotController rc) throws GameActionException {
        // Check the enemy base for mines as long as there are
        //  a sufficient number of robots.  If there are enemy
        //  mines around the base, research diffusion.
        if(!upgraded &&
           robotCount >= ROBOT_THRESH &&
           rc.senseMineLocations(enemyhqloc, 32, rc.getTeam().opponent()).length >= DEFUSION_THRESH) {
            // Now do research, keeping up the robot count
            if(!rc.hasUpgrade(Upgrade.VISION))
                getUpgrade(rc, Upgrade.VISION);

            if(!rc.hasUpgrade(Upgrade.DEFUSION))
                getUpgrade(rc, Upgrade.DEFUSION);

            upgraded = true;
        }
    }

    private void makeSwarmer(RobotController rc) throws GameActionException {
        msgbuf = 0;
        if(shieldloc != null) {
            msgbuf = shieldloc.x << 13;
            msgbuf += shieldloc.y << 6;
        }
        makeRobot(rc, msgbuf, AI.JOB_STANDARD);
    }

    public AI act(RobotController rc) throws GameActionException{
        if(check_msgs[0] == 0){
            requestPath(rc, enemyhqloc);
        }

        // Check to see if we need to upgrade
        checkUpgrades(rc);

        if(rc.isActive()) {
            makeSwarmer(rc);
        }

        // If it gets past round 800 or so, or the enemy has a nuke program,
        //  switch to our endgame panic
        if(Clock.getRoundNum() >= PANIC_ROUND || rc.senseEnemyNukeHalfDone())
            return new PanicHQAI(rc, shieldloc);
        else
            return this;
    }
}
