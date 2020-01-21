package me.lucko.helper.utils;

/**
 * Utility for quickly performing Maths calculations.
 */
public final class Maths {

    private static final int SIN_BITS, SIN_MASK, SIN_COUNT;
    private static final float radFull, radToIndex;
    private static final float degFull, degToIndex;
    private static final float DISTANCE = 0.5f;
    private static final float[] sin, cos;

    static {
        SIN_BITS = 12;
        SIN_MASK = ~(-1 << SIN_BITS);
        SIN_COUNT = SIN_MASK + 1;

        radFull = (float) (Math.PI * 2.0);
        degFull = (float) (360.0);
        radToIndex = SIN_COUNT / radFull;
        degToIndex = SIN_COUNT / degFull;

        sin = new float[SIN_COUNT];
        cos = new float[SIN_COUNT];

        for (int i = 0; i < SIN_COUNT; i++) {
            sin[i] = (float) Math.sin((i + DISTANCE) / SIN_COUNT * radFull);
            cos[i] = (float) Math.cos((i + DISTANCE) / SIN_COUNT * radFull);
        }

        for (int i = 0; i < 360; i += 90) {
            sin[(int) (i * degToIndex) & SIN_MASK] = (float) Math.sin(i * Math.PI / 180.0);
            cos[(int) (i * degToIndex) & SIN_MASK] = (float) Math.cos(i * Math.PI / 180.0);
        }
    }

    /**
     * Returns the trigonometric sine of a radians.
     *
     * @param rad the radians to get a sine of
     * @return the sine of a radians
     */
    public static float sin(float rad) {
        return sin[(int) (rad * radToIndex) & SIN_MASK];
    }

    /**
     * Returns the trigonometric cosine of a radians.
     *
     * @param rad the radians to get a cosine of
     * @return the cosine of a radians
     */
    public static float cos(float rad) {
        return cos[(int) (rad * radToIndex) & SIN_MASK];
    }

    /**
     * Converts an angle measured in degrees to an
     * approximately equivalent angle measured in radians.
     *
     * @param degree the degree to be converted to radians
     * @return the measurement of the degree in radians
     */
    public static float toRadians(double degree) {
        return (float) Math.toRadians(degree);
    }

    private Maths() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
