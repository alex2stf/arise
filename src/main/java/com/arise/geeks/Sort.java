package com.arise.geeks;

import java.util.Comparator;

public class Sort {

    public static void main(String[] args) {
        Integer ar[] = new Integer[]{1, 1, 24, 1, 4, 5, 8, 9, 20};

    }



    public static <T> void bubble(T [] array, Comparator<T> comparator){
        for(int i = 1; i < array.length; i++) {
            T temp;
            if(_compare(array[i - 1], array[i], comparator) > 0) {
                temp = array[i-1];
                array[i-1] = array[i];
                array[i] = temp;
            }
        }
    }


    public static <T> void quick(T [] array, Comparator<T> comparator){
        quick_impl(array, 0, array.length - 1, comparator);
    }

    static <T>  void quick_impl(T [] array, int startIndex, int endIndex, Comparator<T> c){
        int i = startIndex;
        int j = endIndex;

        // calculate pivot number, I am taking pivot as middle index number
        T pivot = array[startIndex + (endIndex-startIndex) / 2];

        // Divide into two arrays
        while (i <= j) {
            while (_compare(array[i], pivot, c) < 0) {
                i++;
            }
            while (_compare(array[j], pivot, c) > 0) {
                j--;
            }
            if (i <= j) {
                _switchElements(i, j, array);
                //move index to next position on both sides
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (startIndex < j){
            quick_impl(array, startIndex, j, c);
        }
        if (i < endIndex) {
            quick_impl(array, i, endIndex, c);
        }
    }

    private static <T> int _compare(T l, T r, Comparator<T> comparator) {
        if (comparator != null){
            return comparator.compare(l, r);
        }
        if (l instanceof Comparable){
            return ((Comparable) l).compareTo(r);
        }
        if (r instanceof Comparable){
            return ((Comparable) r).compareTo(l);
        }
        return 0;
    }




    static <T> void _switchElements(int i, int j, T[] array) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }


}
