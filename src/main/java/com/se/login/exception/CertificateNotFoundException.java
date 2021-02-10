package com.se.login.exception;

public class CertificateNotFoundException extends RuntimeException {

    private  String certificatePath;
    public CertificateNotFoundException(String certificatePath) {
        super(String.format("Can't find certificate '%s' in resources folder.", certificatePath ));
        this.certificatePath = certificatePath;
    }

    public String getCertificatePath() {
        return certificatePath;
    }
}
