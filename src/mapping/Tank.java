package mapping;

import static mapping.Global.dodge;
import static mapping.Global.randomDirection;
import static mapping.Global.tryMove;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class Tank {

	MapLocation myLocation;
	RobotController rc;

	void run() {
		System.out.println("I'm a tank!");
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
				myLocation = rc.getLocation();
				dodge(rc, myLocation);

				// See if there are any nearby enemy robots
				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				// If there are some...
				if (robots.length > 0) {
					// And we have enough bullets, and haven't attacked yet this
					// turn...
					MapLocation enemyLocation = robots[0].getLocation();
					Direction toEnemy = myLocation.directionTo(enemyLocation);

					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);

					if (!rc.hasMoved()) {
						if (myLocation.distanceTo(enemyLocation) > 6)
							tryMove(rc, toEnemy);
						else
							tryMove(rc, toEnemy.opposite());
					}

					if (robots.length > 4 && rc.canFirePentadShot()) {
						rc.firePentadShot(toEnemy);
					} else if (robots.length > 2 && rc.canFireTriadShot()) {
						rc.fireTriadShot(toEnemy);
					} else if (rc.canFireSingleShot())
						rc.fireSingleShot(toEnemy);
				} else {

					Direction d = randomDirection();
					if (rc.readBroadcast(2) != 0) {
						int x = rc.readBroadcast(3);
						int y = rc.readBroadcast(4);
						if (myLocation.isWithinDistance(new MapLocation(x, y), 5)) {
							rc.broadcast(2, 0);
						}
						d = new Direction(x - myLocation.x, y - myLocation.y);
					}

					if (!rc.hasMoved())
						tryMove(rc, d);

					TreeInfo[] trees = rc.senseNearbyTrees(3);

					if (trees.length > 0) {
						for (int x = 0; x < trees.length; x++) {
							TreeInfo this_tree = trees[x];
							if (!this_tree.getTeam().equals(rc.getTeam()) && !rc.hasAttacked()
									&& rc.canFireSingleShot()) {
								rc.fireSingleShot(myLocation.directionTo(this_tree.location));
								break;

							}
						}
					}
				}
				// Clock.yield() makes the robot wait until the next turn, then
				// it will perform this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Tank Exception");
				e.printStackTrace();
			}
		}
	}

	public Tank(RobotController rc) {
		this.rc = rc;
		run();
	}

}
