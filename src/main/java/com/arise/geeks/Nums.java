package com.arise.geeks;

public class Nums {
    public static void main(String[] args) {
       String s = "abc1234c5";
    }



    public static String reverse(String s){
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < s.length(); i++){
            int l = Math.abs(i - s.length());
            r.append(s.charAt(l -1));
        }
        return r.toString();
    }

    public static int extractInt(String input){
        String s = reverse(input);
        int r = 0;
        int v = 1;
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            int n = (c - '0');
            if (n < 10){
                r += n*v;
                v = v * 10;
            }
        }
        return r;
    }


    //Iteration method
    public static int fibIteration(int n) {
        int x = 0, y = 1, z = 1;
        for (int i = 0; i < n; i++) {
            x = y;
            y = z;
            z = x + y;
        }
        return x;
    }

    //Recursive method
    public static int fibRecursion(int  n) {
        if ((n == 1) || (n == 0)) {
            return n;
        }
        return fibRecursion(n - 1) + fibRecursion(n - 2);
    }

    public static void sieveOfEratosthenes(int n) {
        // Create a boolean array "prime[0..n]" and initialize
        // all entries it as true. A value in prime[i] will
        // finally be false if i is Not a prime, else true.
        boolean prime[] = new boolean[n+1];
        for(int i=0;i<n;i++) {
            prime[i] = true;
        }

        for(int p = 2; p*p <=n; p++) {
            // If prime[p] is not changed, then it is a prime
            if(prime[p] == true) {
                // Update all multiples of p
                for(int i = p*2; i <= n; i += p) {
                    prime[i] = false;
                }
            }
        }

        // Print all prime numbers
        for(int i = 2; i <= n; i++) {
            if(prime[i] == true)
                System.out.println(i + " ");
        }
    }
}
