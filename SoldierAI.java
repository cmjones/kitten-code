package team197;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.Upgrade;

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
        case JOB_DEFENDER:
        	return new DefenderAI(rc, this, dataget);
        case JOB_FIGHTER:
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
        MapLocation[] mines;
        MapLocation target, cur;
        Team t;
        int vision;

        if(d == Direction.NONE || d == Direction.OMNI)
            return;

        // Figure out our sight radius
        vision = RobotType.SOLDIER.sensorRadiusSquared;
        if(rc.hasUpgrade(Upgrade.VISION))
            vision += GameConstants.VISION_UPGRADE_BONUS;

        // If we have defusion, first attempt to defuse any
        //  visible enemy mines.  Don't do so if we have shields.
        cur = rc.getLocation();
        if(rc.getShields() == 0 &&
           rc.hasUpgrade(Upgrade.DEFUSION) &&
           (mines = rc.senseMineLocations(cur, vision, rc.getTeam().opponent())).length != 0) {
            rc.defuseMine(mines[0]);
        } else {
            target = cur.add(d);
            t = rc.senseMine(target);
            if(t != null && t != rc.getTeam())
                rc.defuseMine(target);
            else
                rc.move(d);
        }
    }
}
