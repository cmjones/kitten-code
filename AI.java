package team197;

import battlecode.common.RobotController;
import team197.modules.RadioModule;

/**
 * Abstract base class for a robot AI.  Each AI has an act
 *  function that is repeatedly called, with a call to yield
 *  inbetween.  The return value is used to switch strategies
 *  if the robot desires, a null return value will casue the
 *  robot to suicide.
 */
public abstract class AI {
    public static final int JOB_STANDARD = 1,
                            JOB_MINESWEEPER_L = 2,
                            JOB_MINESWEEPER_M = 3,
                            JOB_MINESWEEPER_R = 4,
                            JOB_BUILDER = 5;
    
    public static final int TOBUILD_GENERATOR = 1,
    		                TOBUILD_ARTILLERY = 2,
    		                TOBUILD_MEDBAY = 3,
    		                TOBUILD_SHIELDS = 4,
    		                TOBUILD_SUPPLIER = 5;
	
    protected RadioModule radio;

    public AI() {
            radio = new RadioModule();
    }

    public void do_checkin(RobotController rc){
            radio.write(rc, RadioModule.CHANNEL_CHECKIN, radio.readTransient(rc, RadioModule.CHANNEL_CHECKIN) + 1);
    }

    abstract public AI act(RobotController rc) throws Exception;
}
