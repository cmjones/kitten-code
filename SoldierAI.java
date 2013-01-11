package team197;

import battlecode.common.RobotController;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
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

    public SoldierAI(RobotController rc) {
        nav = new NavModule(rc);
        fight = new FightModule();
    }

    public SoldierAI(RobotController rc, SoldierAI oldme){
    	nav = oldme.nav;
    	fight = oldme.fight;
    }

    public AI act(RobotController rc) throws Exception {
        Direction d;
        MapLocation target;
        int jobget = radio.readTransient(rc, RadioModule.CHANNEL_GETJOB);
        if(jobget == AI.JOB_MINESWEEPER_L){
        	return new MinesweeperAI(rc, this, AI.JOB_MINESWEEPER_L);
        } else if(jobget == AI.JOB_MINESWEEPER_M){
        	return new MinesweeperAI(rc, this, AI.JOB_MINESWEEPER_M);
        } else if(jobget == AI.JOB_MINESWEEPER_R){
        	return new MinesweeperAI(rc, this, AI.JOB_MINESWEEPER_R);
        } else if(jobget == AI.JOB_BUILDER){
        	return new BuilderAI(rc, this);
        } else {
        	return new FighterAI(rc, this);
        }
    }
}
