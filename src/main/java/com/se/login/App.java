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
            System.out.println("Please check your apple certificate in resource folder.");
            System.out.println(cne.getMessage());
        }catch (EmptyParametersException epe){
            System.out.println("Please check your apple credentials.");
            System.out.println(epe.getMessage());
        } catch (Exception e) {
            System.out.println("was error ");
            e.printStackTrace();
        }

        System.out.println("closing ...");
        int a =0;
    }
}
