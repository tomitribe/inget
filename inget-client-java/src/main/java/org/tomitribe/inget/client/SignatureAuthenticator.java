package org.tomitribe.inget.client;

import org.tomitribe.auth.signatures.Algorithm;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signatures;
import org.tomitribe.auth.signatures.Signer;
import org.tomitribe.churchkey.Key;
import org.tomitribe.churchkey.Keys;
import org.tomitribe.util.IO;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
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

@Priority(Priorities.AUTHENTICATION)
public class SignatureAuthenticator implements ClientRequestFilter {

    private final ClientConfiguration config;

    private final SignatureConfiguration sigConfig;

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
        headers.add("date", date);
        final String token = getSignatureAuthentication(requestContext, headers);
        if (token != null) {
            headers.add(sigConfig.getHeader(), token);
        } else {
            System.out.println("Signature token was not generated.");
        }
    }

    private String getSignatureAuthentication(
            ClientRequestContext requestContext,
            MultivaluedMap<String, Object> requestHeaders) {

        String privateKey = findKey();

        if(config.getSignature().getKeyId() == null){
            System.out.println("Key id could not be found.");
            return null;
        }

        if (privateKey == null) {
            System.out.println("Private key could not be found.");
            return null;
        }

        Key decodedKey = Keys.decode(privateKey.getBytes());
        Signature signature = new Signature(sigConfig.getKeyId(), Algorithm.RSA_SHA256, null, sigConfig.getSignedHeaders());

        HashMap<String, String> headersToBeSigned = addHeadersToBeSigned(requestHeaders);

        Signer signer = new Signer(decodedKey.getKey(), signature);
        Signature sign = null;
        try {
            sign = signer.sign(requestContext.getMethod(), requestContext.getUri().getPath(), headersToBeSigned);
            addSignatureDetails(requestContext, headersToBeSigned);
        } catch (IOException e) {
            System.out.println("Fail to sign request:" + sign.toString());
            return null;
        }

        return sign.toString();
    }

    private void addSignatureDetails(ClientRequestContext requestContext, HashMap<String, String> headersToBeSigned) {
        if(config.getSignature().isSignatureDetails()){
            String signingString = Signatures.createSigningString(config.getSignature().getSignedHeaders(), requestContext.getMethod(),
                    requestContext.getUri().getPath(), headersToBeSigned);
            requestContext.getHeaders().add("X-Signing-String", signingString);

        }
    }

    private HashMap<String, String> addHeadersToBeSigned(MultivaluedMap<String, Object> requestHeaders) {
        HashMap<String, String> headersToBeSigned = new HashMap<>();
        Iterator<String> itHeaders = requestHeaders.keySet().iterator();
        while (itHeaders.hasNext()) {
            String key = itHeaders.next();
            if (sigConfig.getSignedHeaders().contains(key)) {
                headersToBeSigned.put(key, requestHeaders.get("date").stream().findFirst().get().toString());
            }
        }
        return headersToBeSigned;
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
            if (sigConfig.getKeyLocation() != null) {
                String keyLocation = sigConfig.getKeyLocation();
                File keyLocationFile = new File(keyLocation);
                if (keyLocationFile.exists()) {
                    privateKey = IO.slurp(new FileInputStream(keyLocationFile));
                    if (config.isVerbose()) {
                        System.out.println("Using key: " + keyLocationFile.getAbsolutePath());
                    }
                } else {
                    throw new FileNotFoundException("couldn't find key on " + keyLocationFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to find key locally: " + e.getMessage());
        }
        return privateKey;
    }
}
