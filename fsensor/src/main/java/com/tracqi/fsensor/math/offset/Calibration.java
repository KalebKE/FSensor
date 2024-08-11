package com.tracqi.fsensor.math.offset;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/*
 * Copyright 2024, Tracqi Technology, LLC
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
/**
 * Represents a field calibration.
 * Created by kaleb on 3/18/18.
 */
public class Calibration {
    public final RealMatrix scalar;
    public final RealVector offset;

    /**
     * Create an instance.
     *
     * @param scalar The scalar of the calibration.
     * @param offset The offset of the calibration.
     */
    public Calibration(RealMatrix scalar, RealVector offset) {
        this.scalar = scalar;
        this.offset = offset;
    }

}
