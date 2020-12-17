package com.example.canatest.singleton;

/**
 *
 *
 * @title: SingleTon2
 * @Author LiuXianKun
 * @Date: 2020/12/12 14:27
 */



public class SingleTon2 {

    private static SingleTon2 instance;

    private SingleTon2() {
    }

    public SingleTon2 getInstance() {
        if (instance == null) {
            instance = new SingleTon2();
        }

        return instance;
    }
}
