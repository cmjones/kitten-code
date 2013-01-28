package team197;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;

import team197.modules.RadioModule;
import team197.modules.NavModule;
import team197.modules.FightModule;


/** Base AI for Soldier-class robots.
 * Soldiers can move and fight, so modules for those
 * things are required.
 */
public class SoldierAI extends AI {
    protected NavModule nav;
    protected FightModule fight;
    String maskbuffer;

    public SoldierAI(RobotController rc) {
    	super(rc);
        nav = new NavModule(rc);
        fight = new FightModule();
    }

    public SoldierAI(RobotController rc, SoldierAI oldme){
    	super(rc, oldme);
    	nav = oldme.nav;
    	fight = oldme.fight;
    }

    public AI act(RobotController rc) throws Exception {
        Direction d;
        MapLocation target;
        int msgget = radio.readTransient(rc, RadioModule.CHANNEL_GETJOB);
        int jobget = msgget&0xF;
        int dataget = msgget >>> 4;

        switch(jobget) {
        case JOB_MINESWEEPER_L:
            return new MinesweeperAI(rc, this, AI.JOB_MINESWEEPER_L);
        case JOB_MINESWEEPER_M:
            return new MinesweeperAI(rc, this, AI.JOB_MINESWEEPER_M);
        case JOB_MINESWEEPER_R:
            return new MinesweeperAI(rc, this, AI.JOB_MINESWEEPER_R);
        case JOB_BUILDER:
            return new BuilderAI(rc, this, dataget);
        case JOB_PANIC:
            return new PanicSoldierAI(rc, this);
        case JOB_SCOUT:
            return new ScoutAI(rc, this);
        default:
            return new FighterAI(rc, this, dataget);
        }
    }

    /** Handy method that diffuses mines as the robot moves.
     * Robot tries to move in the given direction (not moving if passed
     *  Direction.NONE or Direction.OMNI).  If the direction takes it
     *  into a mine, the robot first diffuses the mine.
     *
     * Assumes that the robot is currently free to act.
     */
    public void moveSafe(RobotController rc, Direction d) throws Exception {
        MapLocation target;
        Team t;

        if(d == Direction.NONE || d == Direction.OMNI)
            return;

        // If there's a mine, defuse it
        target = rc.getLocation().add(d);
        t = rc.senseMine(target);
        if(t != null && t != rc.getTeam())
            rc.defuseMine(target);
        else
            rc.move(d);
    }
}
