package com.tracqi.fsensor.filter;

import org.junit.Test;

import static org.junit.Assert.*;

public class LowPassFilterTest {

    private static final float EPSILON = 0.5f;

    @Test
    public void filter_firstCall_returnsNonNull() {
        LowPassFilter filter = new LowPassFilter();
        float[] input = {1.0f, 2.0f, 3.0f};

        float[] output = filter.filter(input);

        assertNotNull(output);
        assertEquals(3, output.length);
    }

    @Test
    public void filter_constantInput_convergesEventually() throws InterruptedException {
        LowPassFilter filter = new LowPassFilter(0.05f);
        float[] constant = {5.0f, 3.0f, 1.0f};

        float[] output = null;
        // Run many iterations with small delay to simulate sensor rate
        for (int i = 0; i < 200; i++) {
            output = filter.filter(constant);
            Thread.sleep(1); // ~1ms between samples, simulates ~1000Hz
        }

        // Should converge toward the constant input value
        assertEquals(5.0f, output[0], EPSILON);
        assertEquals(3.0f, output[1], EPSILON);
        assertEquals(1.0f, output[2], EPSILON);
    }

    @Test
    public void filter_stepInput_outputBetweenOldAndNew() throws InterruptedException {
        LowPassFilter filter = new LowPassFilter(0.05f);
        float[] zero = {0f, 0f, 0f};
        float[] step = {10f, 10f, 10f};

        // Prime with zeros
        for (int i = 0; i < 50; i++) {
            filter.filter(zero);
            Thread.sleep(1);
        }

        // Apply step - output should move toward 10 but not reach it immediately
        float[] output = null;
        for (int i = 0; i < 5; i++) {
            output = filter.filter(step);
            Thread.sleep(1);
        }

        assertTrue("Output should be positive after step", output[0] > 0f);
        assertTrue("Output should not instantly reach step value", output[0] < 10f);
    }

    @Test
    public void filter_outputArrayIsReused() {
        LowPassFilter filter = new LowPassFilter();
        float[] input = {1.0f, 2.0f, 3.0f};

        float[] output1 = filter.filter(input);
        float[] output2 = filter.filter(input);

        assertSame("Filter should reuse the same output array", output1, output2);
    }
}
