package mapping;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class Scout {
	
	MapLocation myLocation;
	RobotController rc;

	void run() {
		System.out.println("I'm an scout!");
		Team enemy = rc.getTeam().opponent();
		Direction d = Global.rndDir();

		// The code you want your robot to perform every round should be in this
		// loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				MapLocation[] targets = rc.getInitialArchonLocations(enemy);
				if ((int) (rc.getTeamBullets() / 10) + rc.getTeamVictoryPoints() >= 1000) {

					rc.donate(rc.getTeamBullets());
				}
				myLocation = rc.getLocation();
				Global.dodge( myLocation);
				// See if there are any nearby enemy robots
				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				// If there are some...
				if (robots.length > 0) {
					// And we have enough bullets, and haven't attacked yet this
					// turn...
					MapLocation enemyLocation = robots[0].getLocation();
					Direction toEnemy = myLocation.directionTo(targets[0]);

					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);

					if (!rc.hasMoved()) {
						if (myLocation.distanceTo(enemyLocation) > 9)
							Global.tryMove(toEnemy);
						else
							Global.tryMove(toEnemy.opposite());
					}
					if (rc.canFireSingleShot()) {
						rc.fireSingleShot(toEnemy);
					}
				} else {

					if (!rc.hasMoved())
						if (!Global.tryMove(d, 0, 0)) {
							d = Global.rndDir();
						}

				}

				// Clock.yield() makes the robot wait until the next turn, then
				// it will perform this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Scout Exception");
				e.printStackTrace();
			}
		}
	}

	public Scout(RobotController rc) {
		this.rc = rc;
		Global.rc = rc;
        run();
	}

}
