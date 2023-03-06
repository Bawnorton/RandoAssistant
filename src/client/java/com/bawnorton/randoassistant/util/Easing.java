package com.bawnorton.randoassistant.util;

public class Easing {
    public static float ease(float a, float b, float t) {
        t *= 2.0f;
        if (t < 1.0f) {
            return a + (b - a) * 0.5f * t * t;
        }
        else {
            t -= 2.0f;
            return a + (a - b) * 0.5f * (t * t - 2.0f);
        }
    }
}
