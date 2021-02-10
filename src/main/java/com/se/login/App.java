package com.se.login;

public class App {
    public static void main(String[] args) {


        try {
            String appleAuth = AppleLoginUtil.appleAuth("authorizationCode", true);
            System.out.println("Apple auth result: " +  appleAuth);

        } catch (Exception e) {
            int b= 0;
            e.printStackTrace();
        }

        int a =0;
    }
}
