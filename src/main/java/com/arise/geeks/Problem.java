package com.arise.geeks;

import com.arise.core.tools.Assert;

public class Problem {

    static double solution(double[] start, double[] end) {
        return diff(start[0], end[0]) + diff(start[1], end[1]);
    }


    static double diff(double s, double e){


        if (Math.ceil(s) == Math.ceil(e)){
            return (Math.ceil(e) - s) + (Math.ceil(e) - e);
        }
        return Math.abs(e - s);
    }

//    static double solution(double[] start, double[] end) {
//        return 0;
//    }






        public static void main(String[] args) {

        Assert.assertEquals(2.0, diff(2, 4));
        Assert.assertEquals(0.7, diff(0.4, 0.9));
       Assert.assertEquals(2.6,  diff(2.4, 5));
       Assert.assertEquals(7.0,  diff(0, 7));
       Assert.assertEquals(1.0,  diff(6, 5));
       Assert.assertEquals(1.3,  diff(0.2, 0.5));
       Assert.assertEquals(0.20000000000000007,  diff(0.9, 1.1));

//        Assert.assertEquals(8.9, solution(new double[]{2.4, 1}, new double[]{5, 7.3}));
//        Assert.assertEquals(2.7, solution(new double[]{0.4, 1}, new double[]{0.9, 3}));
        Assert.assertEquals(7.7, solution(new double[]{0, 0.2}, new double[]{7, 0.5}));
//        Assert.assertEquals(1.2000000000000002, solution(new double[]{0.9, 6}, new double[]{1.1, 5}));


    }
}
