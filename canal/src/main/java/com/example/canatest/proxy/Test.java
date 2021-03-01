package com.example.canatest.proxy;

public class Test {
    private ThreadLocal<Integer> threadLocalNum = new ThreadLocal<>();

    public static void main(String[] args) {
        System.out.println("Runtime.getRuntime().availableProcessors() = " + Runtime.getRuntime().availableProcessors());
    }
}

	

