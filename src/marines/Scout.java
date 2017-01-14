package marines;

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
				myLocation = rc.getLocation();
				Global.loc = myLocation;

				if (!rc.hasMoved()) {
					Global.dodge();
				}
				move = true;

				Direction dir = myLocation.directionTo(enemyBase);

				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				if (robots.length > 0) {
					closest = 0;
					min = 10000000;
					boolean target = false;
					RobotInfo enemyGardener;
					for (int x = 0; x < robots.length; x++) {
						RobotInfo bot = robots[x];
						if (bot.getType().equals(RobotType.GARDENER)) {

							float temp = myLocation.distanceTo(bot.location);
							if (temp < min) {
								closest = x;
								min = temp;
							}
							target = true;
						}

					}
					if (target) {
						enemyGardener = robots[closest];
						MapLocation gardenerLocation = enemyGardener.getLocation();
						Direction toGardener = myLocation.directionTo(gardenerLocation);
						if (!rc.hasMoved())
							Global.goTo(gardenerLocation);

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
							Global.tryMove(toEnemy.opposite());
							myLocation = rc.getLocation();
							toEnemy = myLocation.directionTo(enemyLocation);
						}
						if (rc.canFireSingleShot()) {
							rc.fireSingleShot(toEnemy);
						}
						move = false;
					}
				}
				if (!rc.hasMoved() && move) {
					if (toBase) {
						Global.tryMove(dir);
					} else if (!Global.tryMove(dir, 0, 0)) {
						dir = Global.rndDir();
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
		Global.rc = rc;
		run();
	}

}
