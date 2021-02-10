package com.se.login;

import com.se.login.exception.CertificateNotFoundException;
import com.se.login.exception.EmptyParametersException;
import com.se.login.util.AppleLoginUtil;

public class App {
    public static void main(String[] args) {
        try {
            String appleAuth = AppleLoginUtil.appleAuth("authorizationCode", true);
            System.out.println("Apple auth result: " + appleAuth);
        }catch (CertificateNotFoundException  cne) {
            System.out.println(cne.getMessage());
            System.out.println("Please check your apple certificate in resource folder.");
        }catch (EmptyParametersException epe){
            System.out.println(epe.getMessage());
            System.out.println("Please check your apple credentials.");
        } catch (Exception e) {
            int b= 0;
            e.printStackTrace();
        }

        System.out.println("closing ...");
        int a =0;
    }
}
