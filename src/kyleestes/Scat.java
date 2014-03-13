package kyleestes;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Scat extends AdvancedRobot {
	/**
	 * Represents the enemy robot's current energy level.
	 */
	double previousEnergy = 100;

	/**
	 * Represents a movement direction, where positive is ahead and negative is
	 * behind.
	 */
	int movementDirection = 1;

	/**
	 * Represents a gun turn direction, where positive is clockwise and negative
	 * is counter-clockwise.
	 */
	int gunDirection = 1;

	private double bulletPower = 3;

	/*
	 * (non-Javadoc)
	 * 
	 * @see robocode.Robot#run()
	 */
	public void run() {
		// Decouple the gun movement from the tank movement.
		setAdjustGunForRobotTurn(true);
		
		// Decouple the radar movement from the gun movement.
		setAdjustRadarForGunTurn(true);
		
		// Perform an initial scan.
		setTurnRadarRight(99999);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see robocode.Robot#onScannedRobot(robocode.ScannedRobotEvent)
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Stay at right angles to the opponent.
		setTurnRight(e.getBearing() + 90 - 30 * movementDirection);

		// If the bot has small energy drop, assume it fired.
		double changeInEnergy = previousEnergy - e.getEnergy();

		if (changeInEnergy > 0 && changeInEnergy <= 3) {
			// Dodge!
			movementDirection = -movementDirection;

			setAhead((e.getDistance() / 4 + 25) * movementDirection);
		}

		// When an enemy is spotted, sweep the radar.
		gunDirection = -gunDirection;
		setTurnRadarRight(99999 * gunDirection);

		// Track the energy level.
		previousEnergy = e.getEnergy();
		
		/* Use Linear Targeting to fire at the projected position of the enemy.
		 * See:
		 *  - Linear Targeting
		 *    http://robowiki.net/wiki/Linear_Targeting
		 *  - Maximum Escape Angle
		 *    http://robowiki.net/wiki/Maximum_Escape_Angle 
		 */

		// Radar code
		 
	    double headOnBearing = getHeadingRadians() + e.getBearingRadians();
	    double linearBearing = headOnBearing + Math.asin(e.getVelocity() / Rules.getBulletSpeed(bulletPower) * Math.sin(e.getHeadingRadians() - headOnBearing));
	    setTurnGunRightRadians(Utils.normalRelativeAngle(linearBearing - getGunHeadingRadians()));
	    setFire(bulletPower);
	}
}
