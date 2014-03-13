package kyleestes;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Scat extends AdvancedRobot {
	/**
	 * Represents the enemy robot's current energy level.
	 */
	private double enemyCurrentEnergyLevel = 100;

	/**
	 * Represents a tank bearing modulator for our robot, where positive is clockwise and negative is
	 * counter-clockwise.
	 */
	private int tankBearingModulator = 1;

	/**
	 * Represents a gun bearing modulator for our robot, where positive is clockwise and negative
	 * is counter-clockwise.
	 */
	private int gunBearingModulator = 1;
	
	/**
	 * Represents the power of the bullets we fire.
	 */
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
		setTurnRight(e.getBearing() + 90 - 30 * tankBearingModulator);

		// If the bot has small energy drop, assume it fired.
		double changeInEnergy = enemyCurrentEnergyLevel - e.getEnergy();

		if (changeInEnergy > 0 && changeInEnergy <= 3) {
			// Dodge!
			tankBearingModulator = -tankBearingModulator;

			setAhead((e.getDistance() / 4 + 25) * tankBearingModulator);
		}

		// When an enemy is spotted, sweep the radar.
		gunBearingModulator = -gunBearingModulator;
		setTurnRadarRight(99999 * gunBearingModulator);

		// Update the enemy's current energy level.
		enemyCurrentEnergyLevel = e.getEnergy();
		
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
