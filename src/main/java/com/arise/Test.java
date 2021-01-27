package com.arise;

public class Test {

    static int arrayChange(int[] a) {
        int cnt=0, t;

        for(int i=0; i<a.length-1; i++)
            if(a[i]>=a[i+1]) {
                t = a[i]-a[i+1]+1;
                a[i+1] += t;
                cnt+=t;
            }

        return cnt;
    }


    public static void main(String[] args) {
        System.out.println(arrayChange(new int[]{1, 1, 1}));
        System.out.println(arrayChange(new int[]{-1000, 0, -2, 0}));
    }


}