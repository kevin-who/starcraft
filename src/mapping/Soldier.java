package mapping;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class Soldier {

	MapLocation myLocation;
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
				myLocation = rc.getLocation();
				Global.dodge(myLocation);

				// See if there are any nearby enemy robots
				RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

				// If there are some...
				if (robots.length > 0) {
					// And we have enough bullets, and haven't attacked yet this
					// turn...
					myLocation = rc.getLocation();

					MapLocation enemyLocation = robots[0].getLocation();
					Direction toEnemy = myLocation.directionTo(enemyLocation);

					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) enemyLocation.x);
					rc.broadcast(4, (int) enemyLocation.y);
					boolean isZerg = robots[0].getType().equals(RobotType.LUMBERJACK);
					if (!rc.hasMoved()) {
						if (!isZerg)
							Global.tryMove(toEnemy);
						else
							Global.tryMove(toEnemy.opposite());
					}
					myLocation = rc.getLocation();
					toEnemy = myLocation.directionTo(enemyLocation);

					int d = (int) myLocation.distanceTo(enemyLocation);
					if (isZerg) {
						if (d < 2 && rc.canFirePentadShot())
							rc.firePentadShot(toEnemy);
						else if (rc.canFireTriadShot()) {
							rc.fireTriadShot(toEnemy);
						} else if (rc.canFireSingleShot()) {
							rc.fireSingleShot(toEnemy);
						}
					} else {
						if (d < 2 && rc.canFirePentadShot())
							rc.firePentadShot(toEnemy);
						else if (d < 4 && rc.canFireTriadShot()) {
							rc.fireTriadShot(toEnemy);
						} else if (rc.canFireSingleShot()) {
							rc.fireSingleShot(toEnemy);
						}
					}
				} else {

					Direction d = Global.rndDir();
					try {
						if (rc.readBroadcast(2) != 0) {
							int x = rc.readBroadcast(3);
							int y = rc.readBroadcast(4);
							if (myLocation.isWithinDistance(new MapLocation(x, y), 5)) {
								rc.broadcast(2, 0);
							}
							d = new Direction(x - myLocation.x, y - myLocation.y);
						}
					} catch (GameActionException e) {

					}
					if (!rc.hasMoved())
						Global.tryMove(d);
					if (!rc.hasAttacked()) {
						myLocation = rc.getLocation();

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
