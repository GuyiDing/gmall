package com.example.canatest.singleton;

/**
 * @title: singleTon
 * @Author LiuXianKun
 * @Date: 2020/12/12 14:15
 */


public class SingleTon {
   private final static SingleTon instance = new SingleTon();

   private SingleTon() {
   }

   public SingleTon getInstance() {
      return instance;
   }
}


