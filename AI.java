package team197;

import battlecode.common.RobotController;


/**
 * Abstract base class for a robot AI.  Each AI has an act
 *  function that is repeatedly called, with a call to yield
 *  inbetween.  The return value is used to switch strategies
 *  if the robot desires, a null return value will casue the
 *  robot to suicide.
 */
public abstract class AI {
    abstract public AI act(RobotController rc) throws Exception;
}
