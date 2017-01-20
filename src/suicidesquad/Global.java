package suicidesquad;

import battlecode.common.BulletInfo;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public strictfp class Global extends Bug {

	static void dodge() throws GameActionException {

		BulletInfo[] bi = rc.senseNearbyBullets();
		for (BulletInfo info : bi) {
			Direction propagationDirection = info.dir;
			MapLocation bulletLocation = info.location;

			// Calculate bullet relations to this robot
			Direction directionToRobot = bulletLocation.directionTo(loc);
			float distToRobot = bulletLocation.distanceTo(loc);
			float theta = propagationDirection.radiansBetween(directionToRobot);

			if ((theta < 0 ? -theta : theta) > 1.57079632679) {
				continue;
			}

			float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta));

			if (perpendicularDist <= rc.getType().bodyRadius) {
				tryMove(propagationDirection.rotateLeftDegrees(90));
				break;
			}
		}
	}

}
