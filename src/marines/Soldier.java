package marines;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class Soldier {

	MapLocation myLoc;
	RobotController rc;

	void run() {
		System.out.println("I'm a soldier!");
		Team enemy = rc.getTeam().opponent();

		// The code you want your robot to perform every round should be in this
		// loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {

				if ((int) (rc.getTeamBullets() / 10) + rc.getTeamVictoryPoints() >= 1000) {

					rc.donate(rc.getTeamBullets());
				}
				myLoc = rc.getLocation();
				Global.loc = myLoc;
				Global.dodge();

				// See if there are any nearby enemy robots
				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				// If there are some...
				if (robots.length > 0) {
					// And we have enough bullets, and haven't attacked yet this
					// turn...
					myLoc = rc.getLocation();

					MapLocation enemyLocation = robots[0].getLocation();

					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);
					Global.goTo(enemyLocation);

					myLoc = rc.getLocation();
					Direction toEnemy = myLoc.directionTo(enemyLocation);

					int d = (int) myLoc.distanceTo(enemyLocation);

					if (d < 2 && rc.canFirePentadShot())
						rc.firePentadShot(toEnemy);
					else if (d < 4 && rc.canFireTriadShot()) {
						rc.fireTriadShot(toEnemy);
					} else if (rc.canFireSingleShot()) {
						rc.fireSingleShot(toEnemy);
					}

				} else {

					if (!rc.hasMoved()) {
						if (rc.readBroadcast(2) != 0) {
							int x = rc.readBroadcast(3);
							int y = rc.readBroadcast(4);
							if (myLoc.isWithinDistance(new MapLocation(x, y), 5)) {
								rc.broadcast(2, 0);
							} else {
								Global.goTo(new MapLocation(x, y));
							}

						} else {
							Global.tryMove(Global.rndDir());
						}
					}
					if (!rc.hasAttacked()) {
						myLoc = rc.getLocation();

						TreeInfo[] trees = rc.senseNearbyTrees(3);

						if (trees.length > 0) {
							for (int x = 0; x < trees.length; x++) {
								TreeInfo this_tree = trees[x];
								if (!this_tree.getTeam().equals(rc.getTeam()) && !rc.hasAttacked()
										&& rc.canFireSingleShot()) {
									rc.fireSingleShot(myLoc.directionTo(this_tree.location));
									break;

								}
							}
						}
					}
				}
				// Clock.yield() makes the robot wait until the next turn, then
				// it will perform this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Soldier Exception");
				e.printStackTrace();
			}

		}
	}

	public Soldier(RobotController rc) {
		this.rc = rc;
		Global.rc = rc;
		run();
	}

}
