package team197;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.Upgrade;

import team197.modules.MapModule;
import team197.modules.RadioModule;


public class HQAI extends AI {
    //Array of the 'breakpoints' regarding supplier production
    //DEF: breakpoint: point at which a breakpost actually causes a change in rounds per robot
    protected static int[] sup_breakpoints = {1,2,4,6,9,13,19,31};
    protected static Direction[] alldir = {Direction.NORTH_WEST, Direction.NORTH, Direction.NORTH_EAST,
                                           Direction.WEST,                        Direction.EAST,
                                           Direction.SOUTH_WEST, Direction.SOUTH, Direction.SOUTH_EAST};

    MapLocation[][] encamps_of_int;
    int numencamps_int;
    int msgbuf;
    int dist_btwn_far;
    int build_art_num;
    int curbuild_art_num;
    int close_encamps_num;
    int build_sup_num;
    boolean encamp_sup;
    int curpoint_1 = 0;
    int curpoint_2 = 0;
    int totpoint_1;
    int totpoint_2;
    int sendmsg_2=0;
    int sendmsg_1=0;
    int initbcount; 
    MapLocation desti;
    MapLocation medbayloc;
    MapLocation enemyhqloc;
    int enemyhqdist;
    MapLocation myhqloc;
    int[] check_msgs = new int[1];
    protected int robotCount;
    protected int medCount;
    protected int artCount;
    protected int shiCount;
    protected int genCount;
    protected int supCount;
    private Direction enemyHQ;
    int suptogenrat;
    int genbuildpt;
    int curencamp;
    int cursup;

    public HQAI(RobotController rc) {
        super(rc);

        robotCount = 0;
        medCount = 0;
        artCount = 0;
        shiCount = 0;
        genCount = 0;
        supCount = 0;
        enemyhqloc = rc.senseEnemyHQLocation();
        myhqloc = rc.getLocation();
        enemyHQ = myhqloc.directionTo(enemyhqloc);
        curencamp = 0;
        suptogenrat = 2;
        genbuildpt = suptogenrat * (genCount + 1);
        cursup = 0;
    }

    public HQAI(RobotController rc, HQAI oldme) {
        super(rc, oldme);

        encamps_of_int = oldme.encamps_of_int;
        numencamps_int = oldme.numencamps_int;
        msgbuf = oldme.msgbuf;
        dist_btwn_far = oldme.dist_btwn_far;
        build_art_num = oldme.build_art_num;
        curbuild_art_num = oldme.curbuild_art_num;
        close_encamps_num = oldme.close_encamps_num;
        build_sup_num = oldme.build_sup_num;
        encamp_sup = oldme.encamp_sup;
        curpoint_1 = oldme.curpoint_1;
        curpoint_2 = oldme.curpoint_2;
        totpoint_1 = oldme.totpoint_1;
        totpoint_2 = oldme.totpoint_2;
        sendmsg_2 = oldme.sendmsg_2;
        sendmsg_1 = oldme.sendmsg_1;
        desti = oldme.desti;
        medbayloc = oldme.medbayloc;
        robotCount = oldme.robotCount;
        enemyHQ = oldme.enemyHQ;
        alldir = oldme.alldir;
        enemyhqloc = oldme.enemyhqloc;
        myhqloc = oldme.myhqloc;
        alldir = oldme.alldir;

    }

    public void start(RobotController rc) {
        // Make a robot, then recalculate encampments.  The
        //  calculation should take about as long as required
        //  to spawn another robot.
        try {
            enemyhqdist = myhqloc.distanceSquaredTo(enemyhqloc);
System.out.println("Distance between hqs: " + enemyhqdist);
            findEncampments(rc);
        } catch(Exception e) {
            System.out.println("Exception during start:\n" + e);
        }
    }

    public boolean makeRobot(RobotController rc, int data, int job) throws GameActionException{
        Team mineTeam;

        // Only make a robot if there's enough power to do so
        if(rc.getTeamPower() < 10)
            return false;

        Direction dir = enemyHQ;
        do{
            if(rc.canMove(dir) &&
               ((mineTeam = rc.senseMine(rc.getLocation().add(dir))) == null ||
                mineTeam == rc.getTeam())){
                    int msgbuf = (data << 4) + job;
                    radio.write(rc, RadioModule.CHANNEL_GETJOB, msgbuf);
                    System.out.println("HQ sent a build message");
                    rc.spawn(dir);
                    return true;
            }
        } while((dir = dir.rotateLeft()) != enemyHQ);

        // Failed to find an open spot
        return false;
    }

    public void requestPath(RobotController rc, MapLocation desti){
    	int buf = desti.x << 14;
    	buf += desti.y << 7;
    	radio.write(rc, RadioModule.CHANNEL_PATH_ENCAMP, buf);
    	check_msgs[0] = buf;
    }

    public void do_upkeep(RobotController rc) {
    	
        if(Clock.getRoundNum()%15 == 1){
            robotCount = radio.read(rc, RadioModule.CHANNEL_CHECKIN);
            artCount = radio.read(rc, RadioModule.CHANNEL_ART_CHECK);
            genCount = radio.read(rc, RadioModule.CHANNEL_GEN_CHECK);
            supCount = radio.read(rc, RadioModule.CHANNEL_SUP_CHECK);
            shiCount = radio.read(rc, RadioModule.CHANNEL_SHI_CHECK);
            medCount = radio.read(rc, RadioModule.CHANNEL_MED_CHECK);
            //System.out.println(robotCount + " " + artCount + " " + genCount  + " " + supCount + " " + shiCount + " " + medCount);
            for(int i = check_msgs.length - 1; i >= 0; i --){
                 if(check_msgs[i] != 1){
                     if(((radio.read(rc,RadioModule.CHANNEL_PATH_ENCAMP) == 1 || ((radio.read(rc,RadioModule.CHANNEL_PATH_ENCAMP))&0x7F) != 0 ))){
                         check_msgs[i] = 1;
                         System.out.println("Heeey!");
                     } else {
                         radio.write(rc, RadioModule.CHANNEL_PATH_ENCAMP, check_msgs[i]);
                         System.out.println("No one has replied.");
                     }
                 }
             }
         }
    }



    public void do_broadcast(RobotController rc){
        if(sendmsg_2 == 1){
            if(curpoint_2 < totpoint_2){
System.out.println(" ===== HQ Broadcasting to 2: " + encamps_of_int[2][curpoint_2] + "  - " + curpoint_2 + "/" + totpoint_2);
                broadcast_waypoints(rc, encamps_of_int[2][curpoint_2], curpoint_2, totpoint_2, radio.CHANNEL_WAYPOINTS_2);
                curpoint_2++;
            } else {
                sendmsg_2 = 0;
            }
        }

        if(sendmsg_1 == 1){
            if(curpoint_1 < totpoint_1){
System.out.println(" ===== HQ Broadcasting to 1: " + encamps_of_int[1][curpoint_1] + "  - " + curpoint_1 + "/" + totpoint_1);
                broadcast_waypoints(rc, encamps_of_int[1][curpoint_1], curpoint_1, totpoint_1, radio.CHANNEL_WAYPOINTS_1);
                curpoint_1++;
            } else {
/*                for(int i = 0; i < totpoint_1; i ++){
                        System.out.println(encamps_of_int[1][i]);
                }*/
                sendmsg_1 = 0;
            }
        }
    }

    public MapLocation findencamp_nearpoint(RobotController rc, MapLocation center, int rsquare, boolean includeCenter) throws GameActionException {
        MapLocation[] encamps;
        MapLocation best_encamp;
        int best_dist,
            dist;

        // Grab a list of nearby encampments
        encamps = rc.senseEncampmentSquares(center, rsquare, null);

        // Now walk through and find the closest encamp to the center
        best_dist = rsquare*2;
        best_encamp = null;
        for(int i = 0; i < encamps.length; i++) {
            dist = encamps[i].distanceSquaredTo(center);

            if(dist == 0) {
                // If 'includeCenter' is true, it's fine to return the center
                if(includeCenter)
                    return center;
            } else if(dist < best_dist) {
                // This is the best location so far, store it
                best_dist = dist;
                best_encamp = encamps[i];
            }
        }

        // Return the encampment found, could be null
        return best_encamp;
    }

    private void findEncampments(RobotController rc) throws GameActionException {
        // Find nearby encampments, making scouts in the mean-time
        makeRobot(rc, msgbuf, AI.JOB_SCOUT);
        while((encamps_of_int = map.findEncampments(rc, 20, 3)) == null) {
            makeRobot(rc, msgbuf, AI.JOB_SCOUT);
        }

        encamps_of_int[1][encamps_of_int[1].length - 1] = findencamp_nearpoint(rc, encamps_of_int[1][encamps_of_int[1].length - 1], 2, true);
        encamps_of_int[2][encamps_of_int[2].length - 1] = findencamp_nearpoint(rc, encamps_of_int[2][encamps_of_int[2].length - 1], 2, true);

        while(encamps_of_int == null || encamps_of_int[1] == null){
            rc.yield();
        }

        medbayloc = findencamp_nearpoint(rc, encamps_of_int[1][encamps_of_int[1].length - 1], 9, false);

        // Ensure there aren't more waypoints than can be broadcast
        totpoint_1 = encamps_of_int[1].length;
        if(totpoint_1 > 31) {
            encamps_of_int[1][30] = encamps_of_int[2][totpoint_1-1];
            totpoint_1 = 31;
        }

        totpoint_2 = encamps_of_int[2].length;
        if(totpoint_2 > 31) {
            encamps_of_int[2][30] = encamps_of_int[2][totpoint_2-1];
            totpoint_2 = 31;
        }

        //Checks to see how far away the two far encamps are from eachother.
        //Depending on whether they're far or close, it builds one or two artillery--
        // close = 1, far = 2.
        int dist_btwn_far = encamps_of_int[1][totpoint_1-1].distanceSquaredTo(encamps_of_int[2][totpoint_2-1]);
        if(dist_btwn_far > 14){
            build_art_num = 2;
        } else {
            build_art_num = 1;
        }

        // Calculate exactly where encampments are for close-by ones
        MapLocation[] actualClose = new MapLocation[encamps_of_int[0].length*9];
        MapLocation[] temp;
        numencamps_int = 0;
        for(int i = encamps_of_int[0].length-1; i >= 0; i--) {
            // If this center is near the hq, don't build anything there.
            //  This is so we don't get trapped.
            if(encamps_of_int[0][i].distanceSquaredTo(myhqloc) <= 6)
                continue;

            temp = rc.senseEncampmentSquares(encamps_of_int[0][i], 2, null);
            for(int j = 0; j < temp.length; j++) {
                // Store away the actual encampment
                actualClose[numencamps_int++] = temp[j];
            }
        }

        // Now store the actual encampments in the array
        encamps_of_int[0] = actualClose;
        

        //checks to see how many close encamps it saw and divides by three and checks to see
        //what's the closest breakpoint, and then saves that number to build that many suppliers
        close_encamps_num = numencamps_int;
        build_sup_num = 0;
        for(int i = 0; i < sup_breakpoints.length; i ++){
            if(close_encamps_num < sup_breakpoints[i])
                break;

            build_sup_num = sup_breakpoints[i];
        }

        encamp_sup = true;
    }



    public AI act(RobotController rc) throws Exception {
        if(encamps_of_int == null) {
            // Perform the starting routine
            start(rc);

            // If the closest encampments are far away, change to
            //  a rush strategy
System.out.println("Closest encampment is " + myhqloc.distanceSquaredTo(encamps_of_int[0][numencamps_int-1]));
            if(enemyhqdist <= myhqloc.distanceSquaredTo(encamps_of_int[0][numencamps_int-1]))
                return new PanicHQAI(rc, this);
        }
        for(int i = numencamps_int - 1; i >= 0; i --){
        	if(encamps_of_int[0][i].distanceSquaredTo(enemyhqloc) > rc.getLocation().distanceSquaredTo(enemyhqloc)){
        		initbcount = numencamps_int - i;
        		break;
        	}
        }
        genbuildpt = suptogenrat * (genCount + 1);
        if(initbcount  == 0){
        	initbcount = numencamps_int;
        }
        // Broadcast anything necessary
    	do_broadcast(rc);

        // to keep the HQ from going OMG OMG OMG KEE PDOING THINGS AHAAAHAHAHHHHHHH
    	if(rc.isActive()){
            //Checks to see whether it's been told to build 1 or 2 artilleries. If it was told to
            // build one, goes to the first one and builds.
            //Else, it sends one to the farther one first and then sends one to the closer one.
            //System.out.println(build_art_num);
            if(build_art_num == 1){
                msgbuf = 127 << 13;
                msgbuf += radio.CHANNEL_WAYPOINTS_1 << 6;
                msgbuf += AI.TOBUILD_ARTILLERY;
                build_art_num -= 1;
                sendmsg_1 = 1;
                makeRobot(rc, msgbuf, AI.JOB_BUILDER);
            } else if(build_art_num == 2){
                msgbuf = 127 << 13;
                msgbuf += radio.CHANNEL_WAYPOINTS_2 << 6;
                msgbuf += AI.TOBUILD_ARTILLERY;
                build_art_num -= 1;
                sendmsg_2 = 1;
                makeRobot(rc, msgbuf, AI.JOB_BUILDER);
            } else if(cursup < genbuildpt && curencamp < initbcount && cursup < build_sup_num){
            	desti = encamps_of_int[0][curencamp];
            	msgbuf = desti.x << 13;
            	msgbuf += desti.y << 6;
            	msgbuf += AI.TOBUILD_SUPPLIER;
            	makeRobot(rc, msgbuf, AI.JOB_BUILDER);
            	curencamp ++;
            	cursup ++;
            } else if(cursup == genbuildpt && curencamp < initbcount && cursup < build_sup_num){
            	desti = encamps_of_int[0][curencamp];
            	msgbuf = desti.x << 13;
            	msgbuf += desti.y << 6;
            	msgbuf += AI.TOBUILD_GENERATOR;
            	makeRobot(rc, msgbuf, AI.JOB_BUILDER);
            	curencamp ++;
            } else {
                return new SwarmHQAI(rc, this);
            }
                /*
            } else if(build_sup_num != 0){
                desti = findencamp_nearpoint(rc, encamps_of_int[0][build_sup_num - 1], 2, true);
                if(desti != null){
                    msgbuf = desti.x << 13;
                    msgbuf += desti.y << 6;
                    msgbuf += AI.TOBUILD_SUPPLIER;
                    makeRobot(rc, msgbuf, AI.JOB_BUILDER);
                    build_sup_num -= 1;
                }
            } else if(medbayloc != null){
                msgbuf = medbayloc.x << 13;
                msgbuf += medbayloc.y << 6;
                msgbuf += AI.TOBUILD_MEDBAY;
                makeRobot(rc,msgbuf, AI.JOB_BUILDER);
                medbayloc = null;
            } else {
                return new SwarmHQAI(rc, this);
                */
            
            
    	}

    	return this;
    }
}
