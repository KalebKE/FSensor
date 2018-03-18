package com.kircherelectronics.fsensor.util.offset;

/**
 * A representation of a three space point with double precision.
 * 
 * @author Kaleb
 * @version 1.0
 * 
 */
public class ThreeSpacePoint
{
	public double x;
	public double y;
	public double z;

	/**
	 * Instantiate a new object.
	 * @param x the point on the x-axis
	 * @param y the point on the y-axis
	 * @param z the point on the z-axis
	 */
	public ThreeSpacePoint(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
