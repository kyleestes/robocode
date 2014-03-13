package kyleestes;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class BallGagBot extends AdvancedRobot {
	/**
	 * Represents the enemy robot's current energy level.
	 */
	private double enemyCurrentEnergyLevel = 100;

	/**
	 * Represents a tank bearing modulator for our robot, where positive is
	 * clockwise and negative is counter-clockwise.
	 */
	private int tankBearingModulator = 1;

	/**
	 * Represents a gun bearing modulator for our robot, where positive is
	 * clockwise and negative is counter-clockwise.
	 */
	private int gunBearingModulator = 1;

	/**
	 * Represents the power of the bullets we fire.
	 */
	private double bulletPower = 3;

	/**
	 * Represents the number of radians in a circle.
	 */
	private static final double RADIANS_IN_CIRCLE = Math.PI * 2;
	
	/**
	 * Represents the arclength of our radar scans.
	 */
	private static final double RADAR_SCAN_ARCLENGTH = RADIANS_IN_CIRCLE + Math.PI / 2;
	
	/**
	 * Run the robot.
	 * 
	 * <p>
	 * Note that there is no need for an infinite loop, nor is there a need to
	 * call {@link robocode.AdvancedRobot#execute() execute}. The initial scan
	 * will locate the enemy robot, which will trigger the event handler. The
	 * event handler moves our robot, points our gun and radar, and continuously
	 * re-generates new scan events, thus causing the event handler to repeat
	 * continuously. The event handler does not call
	 * {@link robocode.AdvancedRobot#execute() execute} either, because
	 * {@link robocode.AdvancedRobot#execute() execute} is called by default
	 * when the turn (i.e., tick) is complete.
	 * </p>
	 * 
	 * @see robocode.Robot#run()
	 */
	public void run() {
		// Decouple the gun movement from the tank movement.
		setAdjustGunForRobotTurn(true);

		// Decouple the radar movement from the gun movement.
		setAdjustRadarForGunTurn(true);

		// Perform an initial scan.
		setTurnRadarRightRadians(RADAR_SCAN_ARCLENGTH);
	}

	/**
	 * Handle scanned robot events.
	 * 
	 * <p>
	 * Performs linear targeting to fire at the projected position of the enemy.
	 * </p>
	 * 
	 * @see robocode.Robot#onScannedRobot(robocode.ScannedRobotEvent)
	 * @see <a href='http://robowiki.net/wiki/Linear_Targeting'>Linear
	 *      Targeting</a>
	 * @see <a href='http://robowiki.net/wiki/Maximum_Escape_Angle'>Maximum
	 *      Escape Angle</a>
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
		setTurnRadarRightRadians(RADAR_SCAN_ARCLENGTH * gunBearingModulator);

		// Update the enemy's current energy level.
		enemyCurrentEnergyLevel = e.getEnergy();

		double headOnBearing = getHeadingRadians() + e.getBearingRadians();
		double linearBearing = headOnBearing
				+ Math.asin(e.getVelocity() / Rules.getBulletSpeed(bulletPower)
						* Math.sin(e.getHeadingRadians() - headOnBearing));
		setTurnGunRightRadians(Utils.normalRelativeAngle(linearBearing
				- getGunHeadingRadians()));
		setFire(bulletPower);
	}
}
