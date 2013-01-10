package team197;

import battlecode.common.RobotController;

import team197.modules.NavModule;
import team197.modules.FightModule;


/** Base AI for Soldier-class robots.
 * Soldiers can move and fight, so modules for those
 * things are required.
 */
public abstract class SoldierAI extends AI {
    protected NavModule nav;
    protected FightModule fight;

    public SoldierAI(RobotController rc) {
        nav = new NavModule(rc);
        fight = new FightModule();
    }
}
