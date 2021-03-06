package com.game.magictower.util;

import java.util.Random;

public class MathUtil {

    public static final float EPSILON = 0.00001f;
    
    private static final Random sRandom = new Random();
    
    public static boolean equals(float left, float right) {
        return Math.abs(left -right) < EPSILON;
    }
    
    public static int distance(int x1, int y1, int x2, int y2) {
        return (int)Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
    
    public static double getRad(int x1, int y1, int x2, int y2) {
        float cosAngle = (x2 - x1) / (float)distance(x1, y1, x2, y2);
        float rad = (float)Math.acos(cosAngle);
        if (y2 < y1) {
            rad = -rad;
        }
        return rad;
    }
    
    public static int getRandom(int min, int max) {
        return sRandom.nextInt(max - min + 1) + min;
    }
    
    public static boolean percent(float rate) {
        return sRandom.nextFloat() < rate * 0.01f;
    }
}
