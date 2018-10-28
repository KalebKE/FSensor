package com.kircherelectronics.fsensor.filter.gyroscope.fusion.kalman.filter;

import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/*
 * Acceleration Explorer
 * Copyright (C) 2013-2015, Kaleb Kircher - Kircher Engineering, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class RotationMeasurementModel implements MeasurementModel
{
	private double noiseCoefficient  = 0.001;
	
	/**
	 * The measurement matrix, used to associate the measurement vector to the
	 * internal state estimation vector.
	 */
	private RealMatrix measurementMatrix;

	/**
	 * The measurement noise covariance matrix.
	 */
	private RealMatrix measurementNoise;

	public RotationMeasurementModel()
	{
		super();

		// H = measurementMatrix
		measurementMatrix = new Array2DRowRealMatrix(new double[][]
		{
		{ 1, 0, 0, 0 },
		{ 0, 1, 0, 0 },
		{ 0, 0, 1, 0 },
		{ 0, 0, 0, 1 } });

		// R = measurementNoise
		measurementNoise = new Array2DRowRealMatrix(new double[][]
		{
		{ noiseCoefficient, 0, 0, 0 },
		{ 0, noiseCoefficient, 0, 0 },
		{ 0, 0, noiseCoefficient, 0 },
		{ 0, 0, 0, noiseCoefficient } });
	}

	/** {@inheritDoc} */
	public RealMatrix getMeasurementMatrix()
	{
		return measurementMatrix;
	}

	/** {@inheritDoc} */
	public RealMatrix getMeasurementNoise()
	{
		return measurementNoise;
	}
}
