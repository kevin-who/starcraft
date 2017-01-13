package marines;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public strictfp class RobotPlayer {

	public static void run(RobotController rc) throws GameActionException {

		FastMath.initRand(rc);

		switch (rc.getType()) {
		case ARCHON:
			new Archon(rc);
			break;
		case GARDENER:
			new Gardener(rc);
			break;
		case SOLDIER:
			new Soldier(rc);
			break;
		case LUMBERJACK:
			new Lumberjack(rc);
			break;
		case TANK:
			new Tank(rc);
			break;
		case SCOUT:
			new Scout(rc);
			break;
		}
	}

}