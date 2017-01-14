package marines;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class Lumberjack {

	MapLocation myLocation;
	RobotController rc;

	void phase1() {
		boolean move = true;
		TreeInfo[] trees = rc.senseNearbyTrees(5, Team.NEUTRAL);
		int limit = 0;
		if (trees.length > 0) {
			while (true) {
				try {
					move = true;
					int done = 0;
					int closest = 0;
					float min = 10000000;
					myLocation = rc.getLocation();
					for (int x = 0; x < trees.length; x++) {
						MapLocation this_tree = trees[x].location;
						if (rc.canSenseLocation(this_tree)) {
							if (rc.senseTreeAtLocation(this_tree) == null
									|| !(rc.senseTreeAtLocation(this_tree).team.equals(Team.NEUTRAL))) {
								done++;
								continue;
							} else {
								if (rc.canChop(trees[x].ID)) {
									rc.chop(trees[x].ID);
									move = false;
									rc.setIndicatorDot(this_tree, 0, 0, 128);
									break;
								}

								float distance_to = myLocation.distanceTo(this_tree);
								if (min > distance_to) {
									closest = x;
									min = distance_to;
								}

							}
						} else {
							float distance_to = myLocation.distanceTo(this_tree);
							if (min > distance_to) {
								closest = x;
								min = distance_to;
							}
						}
					}
					boolean far = min > (RobotType.LUMBERJACK.bodyRadius + trees[closest].radius);
					if (!rc.hasMoved() && move && far && !trees[closest].getTeam().equals(rc.getTeam())) {
						Global.tryMove(myLocation.directionTo(trees[closest].location), 45, 3);
						rc.setIndicatorDot(trees[closest].location, 0, 0, 128);
					} else if (!far) {
						move = false;
					}
					if (done >= trees.length || limit > 125)
						break;
					limit++;
					Clock.yield();

				} catch (Exception e) {
					System.out.println("Lumberjack Exception");
					e.printStackTrace();

				}
			}
		}
	}

	void phase2() {
		Team enemy = rc.getTeam().opponent();
		TreeInfo[] trees;
		boolean move;

		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your
			// robot to explode
			try {
				if ((int) (rc.getTeamBullets() / 10) + rc.getTeamVictoryPoints() >= 1000) {
					rc.donate(rc.getTeamBullets());
				}
				move = true;
				// See if there are any enemy robots within striking range
				// (distance 1 from lumberjack's radius)

				myLocation = rc.getLocation();
				if (!rc.hasMoved())
					Global.dodge(myLocation);

				RobotInfo[] robots = rc.senseNearbyRobots(
						RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

				if (robots.length > 0 && !rc.hasAttacked()) {
					// Use strike() to hit all nearby robots!
					rc.strike();
					rc.broadcast(2, rc.getRoundNum());
					rc.broadcast(3, (int) myLocation.x);
					rc.broadcast(4, (int) myLocation.y);
				} else {
					robots = rc.senseNearbyRobots(-1, enemy);

					if (robots.length > 0) {

						MapLocation enemyLocation = robots[0].getLocation();
						Direction toEnemy = myLocation.directionTo(enemyLocation);
						rc.broadcast(2, rc.getRoundNum());
						rc.broadcast(3, (int) enemyLocation.x);
						rc.broadcast(4, (int) enemyLocation.y);
						if (!rc.hasMoved())
							Global.tryMove(toEnemy);
					} else {
						trees = rc.senseNearbyTrees(-1);

						if (trees.length > 0) {
							int closest_index = 0;
							float mindist = 10000000;
							for (int x = 0; x < trees.length; x++) {
								TreeInfo this_tree = trees[x];
								if (!this_tree.getTeam().equals(rc.getTeam())) {
									float distance_to = myLocation.distanceSquaredTo(this_tree.location);
									if (mindist > distance_to) {
										closest_index = x;
										mindist = distance_to;
									}
									if (rc.canChop(this_tree.ID)) {
										rc.chop(this_tree.ID);
										move = false;
									}
								}
							}
							boolean far = mindist > (RobotType.LUMBERJACK.bodyRadius + trees[closest_index].radius);
							if (!rc.hasMoved() && move && far && !trees[closest_index].getTeam().equals(rc.getTeam())) {
								Global.tryMove(myLocation.directionTo(trees[closest_index].location));
							} else if (!far) {
								move = false;
							}
						}
						if (!rc.hasMoved() && move) {

							Direction d = Global.rndDir();
							if (rc.readBroadcast(2) != 0) {
								int x = rc.readBroadcast(3);
								int y = rc.readBroadcast(4);
								if (myLocation.isWithinDistance(new MapLocation(x, y), 5)) {
									rc.broadcast(2, 0);
								}
								d = new Direction(x - myLocation.x, y - myLocation.y);
							}
							Global.tryMove(d);

						}
					}

				}
				Clock.yield();

			} catch (Exception e) {
				System.out.println("Lumberjack Exception");
				e.printStackTrace();
			}
		}
	}

	void run() {
		System.out.println("I'm a lumberjack!");
		phase1();
		phase2();
	}

	public Lumberjack(RobotController rc) {
		this.rc = rc;
		Global.rc = rc;
		run();
	}

}
