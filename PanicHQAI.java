package team197;

import java.util.ArrayList;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Team;
import battlecode.common.Upgrade;

import team197.modules.RadioModule;
import team197.modules.MapModule;


public class PanicHQAI extends HQAI {
    MapLocation center;
    int rounds;
    int msgbuf;

    public PanicHQAI(RobotController rc, MapLocation ml) {
        super(rc);
        start(rc, ml);
    }

    public PanicHQAI(RobotController rc, PanicHQAI oldme) {
        super(rc, oldme);

        msgbuf = oldme.msgbuf;
        rounds = oldme.rounds;
        center = oldme.center;
    }

    public PanicHQAI(RobotController rc, HQAI oldme, MapLocation ml) {
        super(rc, oldme);
        start(rc, ml);
    }

    public void start(RobotController rc, MapLocation ml) {
        // Calculate how many rounds we should build up for.
        //  If the nuke is coming, make it a third of the rounds
        //  it takes to make a nuke.  Otherwise, twice that.
        rounds = Upgrade.NUKE.numRounds/3;
        try {
            if(!rc.senseEnemyNukeHalfDone())
                rounds *= 2;
        } catch(GameActionException e) {}

        // The congregation point for our panic fighters
        //  should be in the center of the map if we haven't
        //  been passed a gathering point.
        if(ml == null)
            center = new MapLocation((myhqloc.x+enemyhqloc.x)/2, (myhqloc.y+enemyhqloc.y)/2);
        else
            center = ml;
    }


    public AI act(RobotController rc) throws GameActionException{
        // Check on the nuke progress.  If we've hit halfway, make
        //  sure we aren't waiting too long
        if(rounds > Upgrade.NUKE.numRounds/3 && rc.senseEnemyNukeHalfDone())
            rounds = Upgrade.NUKE.numRounds/3;
        rounds--;

        // If we're still building up, broadcast the hold signal
        // 7 bits: x, 7 bits: y
        //
        // Only broadcast every 10 rounds
        if(Clock.getRoundNum()%10 == 0) {
            if(rc.getTeamPower() > 5 && rounds > 0)
                radio.write(rc, RadioModule.CHANNEL_PANIC, (center.x << 7) + center.y);
            else
                radio.write(rc, RadioModule.CHANNEL_PANIC, 0xFADCAD);
        }

        if(rc.isActive()) {
            // Spam Fighters
            makeRobot(rc, 0, AI.JOB_PANIC);
        }

        return this;
    }
}
