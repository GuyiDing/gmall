package com.atguigu.gmall.product;

import java.io.*;

/**
 * @title: Person
 * @Author LiuXianKun
 * @Date: 2021/1/11 13:22
 */
public class Person {
    public static void main(String[] args) {
//        BufferedReader br = null;
//        try {
//            br = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\86186\\Desktop\\jenkins部署shell.txt")));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        String data = null;
//        while (true) {
//            try {
//                if (!((data = br.readLine()) != null)) break;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println(data);
//        }
        String str1 = "hello";
        String str2 = "he" + "llo";
        System.out.println(str1 == str2);
    }


}
