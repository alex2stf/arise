package com.arise.geeks;

import java.util.Comparator;

public class Arrange {
    /**
     * Write a program to input a list of integers in an array and arrange them in a way similar to the to-and-fro movement of a Pendulum.
     * - The minimum element out of the list of integers, must come in center position of array.
     * - The number in the ascending order next to the minimum, goes to the right, the next higher number goes to the left of minimum number and it continues.
     * - As higher numbers are reached, one goes to one side in a to-and-fro manner similar to that of a Pendulum.
     * @see <a href="https://www.geeksforgeeks.org/program-print-array-pendulum-arrangement/">http://google.com</a>
     * @param array
     * @param comparator
     * @param <T>
     * @return
     */
    public static <T> T[] pendulum(T [] array, final Comparator<T> comparator){
        int n = array.length;
        //create a new instance of array type T
        T[] copy = (T[]) new Object[n];

        Sort.quick_impl(array, 0, n - 1, comparator);
        int mid = ( n- 1) / 2;
        copy[mid] = array[0];
        int j = 1;
        int i;
        for (i = 1; i <= mid; i++){
            copy[mid + i] = array[j++];
            copy[mid - i] = array[j++];
        }
        if (n%2==0){
            copy[mid + i] = array[j];
        }
        return copy;
    };
}
