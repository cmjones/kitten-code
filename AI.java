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
    protected RadioModule radio;

    public AI() {
            radio = new RadioModule();
    }

    public void do_checkin(RobotController rc){
            radio.write(rc, RadioModule.CHANNEL_CHECKIN, radio.readTransient(rc, RadioModule.CHANNEL_CHECKIN) + 1);
    }

    abstract public AI act(RobotController rc) throws Exception;
}
