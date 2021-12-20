package com.munca.in.pandemie;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Test1 {
    
    static int roads[][]= new int[][] {
            {0, 1, 0},
            {4, 1, 2},
            {4, 3, 4},
            {2, 3, 1},
            {2, 0, 3}};


    static int invl[][]= new int[][] {
            {2, 3, 1},
            {3, 0, 0},
            {2, 0, 2}};

    static boolean solution(int [][] roads){
        Map<Integer, int[]> buf = new HashMap<>();

        for(int i = 0; i < roads.length; i++){

            int road = roads[i][2];
            int from = roads[i][0];
            int to = roads[i][1];
            int neighbour1 = road + 1;
            int neighbour2 = road - 1;

            int thisTh[] = new int[]{from, to};
            buf.put(road, thisTh);

            if(buf.containsKey(neighbour1) && isMatch(buf.get(neighbour1), thisTh)){
                return false;
            }

            if(buf.containsKey(neighbour2) && isMatch(buf.get(neighbour1), thisTh)){
                return false;
            }


        }
        return true;
    }


    static boolean isMatch(int[] a, int[] b){
        if (a == null || b == null){
            return false;
        }
        return a[0] == b[0] || a[0] == b[1] || a[1] == b[0] || a[1] == b[1];
    }






    public static String getPrettyPrintAmount(Double amount) {
        Double newAmount = amount == null ? 0d : amount;

        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator(',');
        dfs.setGroupingSeparator('.');
        df.setDecimalSeparatorAlwaysShown(false);
        df.setDecimalFormatSymbols(dfs);
        df.setMaximumFractionDigits(2);
        return df.format(newAmount);
    }


    public static void main(String[] args) {
        for (Double i : new Double[]{12d, 0.45d, 100d, 2d, 34.556, null, -23d, -0.45, 0.00000005}){
            String x = getPrettyPrintAmount(i);
            System.out.println(x);
        }
    }
}
