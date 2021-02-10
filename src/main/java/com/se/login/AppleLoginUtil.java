package com.se.login;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Date;

public class AppleLoginUtil {
    // APPLE CREDENTIAL BLOCK -->
    public static final String CERTIFICATE_PATH = "";
    private static final String APPLE_AUTH_URL = "";
    private static final String KEY_ID = "";
    private static final String TEAM_ID = "";
    private static final String CLIENT_ID = "";
    private static final String WEB_CLIENT_ID = "";
    private static final String WEB_REDIRECT_URL = "";
    private static PrivateKey pKey;
    // <-- APPLE CREDENTIAL BLOCK

    private static PrivateKey getPrivateKey() throws Exception {

        ClassLoader classLoader = AppleLoginUtil.class.getClassLoader();
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

        return token;
    }


    /*
     * Returns unique user id from apple
     * */
    public static String appleAuth(String authorizationCode, boolean forWeb) throws Exception {

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
        String idToken = tokenResponse.getId_token();
        String payload = idToken.split("\\.")[1];//0 is header we ignore it for now
        String decoded = new String(Decoders.BASE64.decode(payload));

        IdTokenPayload idTokenPayload = new Gson().fromJson(decoded, IdTokenPayload.class);

        return idTokenPayload.getSub();
    }


}
