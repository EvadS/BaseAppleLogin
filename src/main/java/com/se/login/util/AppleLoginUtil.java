package com.se.login.util;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.se.login.exception.CertificateNotFoundException;
import com.se.login.exception.EmptyParametersException;
import com.se.login.exception.TokenResponseIncorrectFormat;
import com.se.login.model.IdTokenPayload;
import com.se.login.model.TokenResponse;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.FileReader;
import java.net.URL;
import java.security.PrivateKey;
import java.util.Date;

public class AppleLoginUtil {
    // APPLE CREDENTIAL BLOCK -->
    public static final String CERTIFICATE_PATH = "";
    private static final String APPLE_AUTH_URL = "https://appleid.apple.com/auth/token";
    private static final String KEY_ID = "";
    private static final String TEAM_ID = "";
    private static final String CLIENT_ID = "";
    private static final String WEB_CLIENT_ID = "";
    private static final String WEB_REDIRECT_URL = "";
    private static PrivateKey pKey;
    // <-- APPLE CREDENTIAL BLOCK

    private static PrivateKey getPrivateKey() throws Exception {

        ClassLoader classLoader = AppleLoginUtil.class.getClassLoader();

        URL url = classLoader.getResource(CERTIFICATE_PATH);
        if (url == null) {
            throw new CertificateNotFoundException(CERTIFICATE_PATH);
        }

        String path = classLoader.getResource(CERTIFICATE_PATH).getPath();

        final PEMParser pemParser = new PEMParser(new FileReader(path));
        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        final PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
        final PrivateKey pKey = converter.getPrivateKey(object);

        return pKey;
    }

    private static String generateJWT() throws Exception {
        if (pKey == null) {
            pKey = getPrivateKey();
        }

        String token = Jwts.builder()
                .setHeaderParam(JwsHeader.KEY_ID, KEY_ID)
                .setIssuer(TEAM_ID)
                .setAudience("https://appleid.apple.com")
                .setSubject(CLIENT_ID)
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 5)))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(pKey, SignatureAlgorithm.ES256)
                .compact();

        System.out.println("generated token\n" + token  + "\n----------------------------\n");
        return token;
    }

    private static String generateWebJWT() throws Exception {
        String token = Jwts.builder()
                .setHeaderParam(JwsHeader.KEY_ID, KEY_ID)
                .setIssuer(TEAM_ID)
                .setAudience("https://appleid.apple.com")
                .setSubject(WEB_CLIENT_ID)
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 5)))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();

        System.out.println("generated token for web \n" + token + "\n----------------------------\n");
        return token;
    }


    /*
     * Returns unique user id from apple
     * */
    public static String appleAuth(String authorizationCode, boolean forWeb) throws Exception {

        validateCurrentParams();
        System.out.println("validated");

        HttpResponse<String> response = Unirest.post(APPLE_AUTH_URL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("client_id", forWeb ? WEB_CLIENT_ID : CLIENT_ID)
                .field("client_secret", forWeb ? generateWebJWT() : generateJWT())
                .field("grant_type", "authorization_code")
                .field("code", authorizationCode)
                .field("redirect_uri", forWeb ? WEB_REDIRECT_URL : null)
                .asString();

        Gson gson = new Gson();

        try {
            Object responseObj = gson.fromJson(response.getBody(), Object.class);
            System.out.println("Response : " + responseObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        TokenResponse tokenResponse = gson.fromJson(response.getBody(), TokenResponse.class);
        if(tokenResponse.getToken_type() == null){
           throw new TokenResponseIncorrectFormat(response.getBody());
        }

        String idToken = tokenResponse.getId_token();
        String payload = idToken.split("\\.")[1];//0 is header we ignore it for now
        String decoded = new String(Decoders.BASE64.decode(payload));

        IdTokenPayload idTokenPayload = new Gson().fromJson(decoded, IdTokenPayload.class);

        return idTokenPayload.getSub();
    }


    private static void validateCurrentParams() {

        if (StringUtils.isEmpty(CERTIFICATE_PATH) || StringUtils.isBlank(CERTIFICATE_PATH)) {
            throw new EmptyParametersException("CERTIFICATE_PATH");
        }

        if (StringUtils.isEmpty(APPLE_AUTH_URL) || StringUtils.isBlank(APPLE_AUTH_URL)) {
            throw new EmptyParametersException("APPLE_AUTH_URL");
        }

        if (StringUtils.isEmpty(KEY_ID) || StringUtils.isBlank(KEY_ID)) {
            throw new EmptyParametersException("KEY_ID");
        }

        if (StringUtils.isEmpty(TEAM_ID) || StringUtils.isBlank(TEAM_ID)) {
            throw new EmptyParametersException("TEAM_ID");
        }


        if (StringUtils.isEmpty(WEB_CLIENT_ID) || StringUtils.isBlank(WEB_CLIENT_ID)) {
            throw new EmptyParametersException("WEB_CLIENT_ID");
        }

        if (StringUtils.isEmpty(WEB_REDIRECT_URL) || StringUtils.isBlank(WEB_REDIRECT_URL)) {
            throw new EmptyParametersException("WEB_REDIRECT_URL");
        }
    }


}
