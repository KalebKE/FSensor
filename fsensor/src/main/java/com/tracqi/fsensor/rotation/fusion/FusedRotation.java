package com.tracqi.fsensor.rotation.fusion;

import com.tracqi.fsensor.rotation.Rotation;

import org.apache.commons.math3.complex.Quaternion;

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

/**
 * A base implementation for an orientation IMU sensor fusion.
 */
public abstract class FusedRotation implements Rotation {

    protected static final float EPSILON = 0.000000001f;
    // Nano-second to second conversion
    protected static final float NS2S = 1.0f / 1000000000.0f;

    protected Quaternion rotationVector;

    public boolean isBaseOrientationSet() {
        return rotationVector != null;
    }

    public void setBaseOrientation(Quaternion baseOrientation) {
        rotationVector = baseOrientation;
    }
}
