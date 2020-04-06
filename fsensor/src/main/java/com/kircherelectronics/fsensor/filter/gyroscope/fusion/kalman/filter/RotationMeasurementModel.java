package com.kircherelectronics.fsensor.filter.gyroscope.fusion.kalman.filter;

import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/*
 * Copyright 2018, Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class RotationMeasurementModel implements MeasurementModel
{
	private final double noiseCoefficient  = 0.001;
	
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
