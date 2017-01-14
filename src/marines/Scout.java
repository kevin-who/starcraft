package marines;

import static marines.Global.dodge;
import static marines.Global.randomDirection;
import static marines.Global.tryMove;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Scout {

	MapLocation myLocation;
	RobotController rc;

	void run() {
		Team team = rc.getTeam();
		Team enemy = team.opponent();
		myLocation = rc.getLocation();
		MapLocation[] enemies = rc.getInitialArchonLocations(enemy);
		int closest = 0;
		float min = 10000000;
		for (int e = 0; e < enemies.length; e++) {
			float temp = myLocation.distanceTo(enemies[e]);
			if (temp < min) {
				closest = e;
				min = temp;
			}
		}
		MapLocation enemyBase = rc.getInitialArchonLocations(enemy)[closest];
		boolean move;
		boolean toBase = true;

		// The code you want your robot to perform every round should be in this
		// loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				if (!rc.hasMoved()) {
					dodge(rc, myLocation);
				}
				move = true;

				myLocation = rc.getLocation();

				Direction dir = myLocation.directionTo(enemyBase);

				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				if (robots.length > 0) {
					boolean target = false;
					RobotInfo enemyGardener;
					for (int x = 0; x < robots.length; x++) {
						RobotInfo this_bot = robots[x];
						if (this_bot.getType().equals(RobotType.GARDENER)) {

							closest = 0;
							min = 10000000;
							for (int e = 0; e < enemies.length; e++) {
								float temp = myLocation.distanceTo(enemies[e]);
								if (temp < min) {
									closest = e;
									min = temp;
								}
							}
							target = true;
						}

					}
					if (target) {
						enemyGardener = robots[closest];
						MapLocation gardenerLocation = enemyGardener.getLocation();
						Direction toGardener = myLocation.directionTo(gardenerLocation);

						if (myLocation.distanceTo(gardenerLocation) > 5 && !rc.hasMoved()) {
							tryMove(rc, toGardener);
						}
						myLocation = rc.getLocation();
						toGardener = myLocation.directionTo(gardenerLocation);
						if (rc.canFireSingleShot()) {
							rc.fireSingleShot(toGardener);
						}
						move = false;
						toBase = false;
					} else {
						MapLocation enemyLocation = robots[0].getLocation();
						Direction toEnemy = myLocation.directionTo(enemyLocation);

						rc.broadcast(2, rc.getRoundNum());
						rc.broadcast(3, (int) enemyLocation.x);
						rc.broadcast(4, (int) enemyLocation.y);

						if (!rc.hasMoved()) {
							if (myLocation.distanceTo(enemyLocation) > 9)
								tryMove(rc, toEnemy);
							else
								tryMove(rc, toEnemy.opposite());
							myLocation = rc.getLocation();
							toEnemy = myLocation.directionTo(enemyLocation);
						}
						if (rc.canFireSingleShot()) {
							rc.fireSingleShot(toEnemy);
						}
					}
				}
				if (!rc.hasMoved() && move) {
					if (toBase) {
						tryMove(rc, dir);
					} else if (!tryMove(rc, dir, 0, 0)) {
						dir = randomDirection();
					}
				}

				Clock.yield();

			} catch (Exception e) {
				System.out.println("Scout Exception");
				e.printStackTrace();
			}
		}

	}

	public Scout(RobotController rc) {
		this.rc = rc;
		run();
	}

}
