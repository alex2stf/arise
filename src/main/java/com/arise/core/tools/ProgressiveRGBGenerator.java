package com.arise.core.tools;

public class ProgressiveRGBGenerator {


    private  int max = 255;
    private  int intensity = 0;
    int r = 0;
    int g = 0;
    int b = 0;
    boolean up = true;
    int limit = 4;


    public ProgressiveRGBGenerator(){

    }

    public ProgressiveRGBGenerator(int max, int intensity) {
        this.max = max;

        this.intensity = intensity;
        this.r = max;
        g = intensity;
        b = intensity;
    }





    public RGB next(){
        return next(1);
    }

    public int increment(int x, int amount, int max){
        return x + amount < max ? x + amount : max;
    }

    public int decrement(int x, int amount, int min){
        return x - amount > min ? x - amount : min;
    }

    public RGB next(int step) {
        //X Y Y
        if (r == max && g < max && b == intensity){ //X y++ Y
            g = increment(g, step, max);
        }

        //X X Y
        else if (r > intensity && g == max && b == intensity){  // x-- X Y
            r = decrement(r, step, intensity);
        }

        //Y X Y
        else if (r == intensity && g == max && b < max){
            b = increment(b, step, max);
        }

        //Y X X
        else if (r == intensity && g > intensity && b == max){
            g = decrement(g, step, intensity);
        }
        //Y Y X
        else if (r < max && g == intensity && b == max){
            r = increment(r, step, max);
        }

        //X Y X
        else if (r == max && g == intensity && b > intensity){
            b = decrement(b, step, intensity);
        }

        else {
            throw new RuntimeException("FAiled algorith,");
        }

        RGB rgb = new RGB();
        rgb.r = r;
        rgb.g = g;
        rgb.b = b;
        return rgb;
    }



    public class RGB {


        int r;
        int g;
        int b;

        public int R() {
            return r;
        }

        public int G() {
            return g;
        }

        public int B() {
            return b;
        }

        @Override
        public String toString() {
            return "RGB{" +
                    "r=" + r +
                    ", g=" + g +
                    ", b=" + b +
                    '}';
        }
    }
}
