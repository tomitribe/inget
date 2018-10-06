package org.tomitribe.trapease.movie.rest.client.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import javax.annotation.Generated;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.tomitribe.auth.signatures.Algorithm;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;
import org.tomitribe.churchkey.Key;
import org.tomitribe.churchkey.Keys;
import org.tomitribe.util.IO;

@Generated(value = "org.tomitribe.model.ModelGenerator")
public class SignatureAuthenticator implements ClientRequestFilter {

    private final ClientConfiguration config;

    private final org.tomitribe.trapease.movie.rest.client.base.SignatureConfiguration sigConfig;

    public SignatureAuthenticator(
            ClientConfiguration config) {
        this.config = config;
        this.sigConfig = config.getSignature();
    }

    public void filter(
            ClientRequestContext requestContext) throws IOException {
        addDefaultSignedHeaders();
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        String date = sdf.format(new Date());
        if(config.isVerbose()){
            System.out.println("Signature date: " + date);
        }
        headers.add("date", date);
        final String authentication = getSignatureAuthentication(requestContext, headers);
        if(authentication != null){
            headers.add(sigConfig.getHeader(), authentication);
        }
    }

    private String getSignatureAuthentication(
            ClientRequestContext requestContext,
            MultivaluedMap<String, Object> requestHeaders) {

        String privateKey = findKey();

        if(privateKey == null){
            System.out.println("Private key could not be found.");
            return null;
        }

        Key decodedKey = Keys.decode(privateKey.getBytes());
        Signature signature = new Signature(sigConfig.getKeyId(), Algorithm.RSA_SHA256, null, sigConfig.getSignedHeaders());

        HashMap<String, String> headersToBeSigned = new HashMap<>();
        Iterator<String> itHeaders = requestHeaders.keySet().iterator();
        while (itHeaders.hasNext()) {
            String key = itHeaders.next();
            if (sigConfig.getSignedHeaders().contains(key)) {
                headersToBeSigned.put(key, String.valueOf(requestHeaders.get(key)));
            }
        }

        Signer signer = new Signer(decodedKey.getKey(), signature);
        Signature sign = null;
        try {
            sign = signer.sign(requestContext.getMethod(), requestContext.getUri().toString(), headersToBeSigned);

            if (config.isVerbose()) {
                System.out.println("Request signed:" + sign.toString());
            }
        } catch (IOException e) {
            System.out.println("Fail to sign request:" + sign.toString());
        }

        return sign.toString();
    }

    private void addDefaultSignedHeaders() {
        if (sigConfig.getSignedHeaders() == null) {
            sigConfig.setSignedHeaders(new ArrayList<>());
        }
        sigConfig.getSignedHeaders().add("date");
        sigConfig.getSignedHeaders().add("(request-target)");
    }

    private String findKey() {
        String privateKey = null;
        try {
            if(StringUtils.isEmpty(sigConfig.getKeyLocation())){
                if (sigConfig.getKeyId() != null) {
                    File keyLocation = new File(System.getProperty("user.home") + File.separator
                            + ".ssh" + File.separator + sigConfig.getKeyId());
                    if(keyLocation.exists()){
                        privateKey = IO.slurp(new FileInputStream(keyLocation));
                        if(config.isVerbose()){
                            System.out.println("Using key: " + keyLocation.getAbsolutePath());
                        }
                    } else {
                        throw new FileNotFoundException("counldn't find key on "+ keyLocation.getAbsolutePath());
                    }
                }
            }else {
                String keyLocation = sigConfig.getKeyLocation();
                File keyLocationFile = new File(keyLocation);
                if(keyLocationFile.exists()){
                    privateKey = IO.slurp(new FileInputStream(keyLocationFile));
                    if(config.isVerbose()){
                        System.out.println("Using key: " + keyLocationFile.getAbsolutePath());
                    }
                }else{
                    throw new FileNotFoundException("counldn't find key on "+ keyLocationFile.getAbsolutePath());
                }

            }
        } catch (Exception e) {
            System.out.println("Failed to find key locally: " + e.getMessage());
        }

        return privateKey;
    }
}
