package com.arise.core.tools;

public class MathUtil {
    /**
     * Find the minimum value from the given array.
     */
    public static int min(int[] values) {
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < values.length; ++i)
        {
            if (values[i] < min)
            {
                min = values[i];
            }
        }

        return min;
    }


    /**
     * Find the maximum value from the given array.
     */
    public static int max(int[] values) {
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < values.length; ++i)
        {
            if (max < values[i])
            {
                max = values[i];
            }
        }

        return max;
    }


}
