/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 *
 * @author xyang
 */
public class AggregateRESTClient {
    private String baseUrl = null;
    private String authCred = null;
    private String trustStore = null;
    private String tokenUrl = null;

    private Logger log = Logger.getLogger(AggregateRESTClient.class);

    public AggregateRESTClient(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        if (username != null && password != null && !username.isEmpty()) {
            authCred = username + ":" + password;
        }
    }

    public AggregateRESTClient() {
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAuthCred() {
        return authCred;
    }

    public void setAuthCred(String authCred) {
        this.authCred = authCred;
    }
    
    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        if (baseUrl != null && !baseUrl.isEmpty() && !tokenUrl.startsWith("http")) {
            tokenUrl = baseUrl + tokenUrl;
        }
        this.tokenUrl = tokenUrl;
    }
    
    //@TODO: add (a) SSL truststore path (default = null) (b) authServerPath (for getting Bearer token, default = null) (turn user, password into authString)
    public String[] executeHttpMethod(String method, String url, String body, String trustStore, String bearerToken) throws IOException {
        String methods[] = method.split("/");
        method = methods[0];
        String type = (methods.length > 1 ? methods[1] : "json");
        URL urlObj = new URL(url);
        URLConnection conn = urlObj.openConnection();
        if (url.startsWith("https:")) {
            ((HttpsURLConnection) conn).setRequestMethod(method);
            System.setProperty("javax.net.ssl.trustStore", trustStore);
            System.setProperty("javax.net.ssl.trustStoreType", "jks");
        } else {
            ((HttpURLConnection) conn).setRequestMethod(method);
        }
        if (bearerToken != null && !bearerToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bear " + bearerToken);
        } else {
            String userPassword = authCred;
            byte[] encoded = Base64.encodeBase64(userPassword.getBytes());
            String stringEncoded = new String(encoded);
            conn.setRequestProperty("Authorization", "Basic " + stringEncoded);
        }
        conn.setRequestProperty("Content-type", "application/" + type);
        conn.setRequestProperty("Accept", "application/"+type);
        if (body != null && !body.isEmpty()) {
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
        }
        log.debug(String.format("Sending %s request to URL : %s", method, url));
        String responseCode[] = new String[3];
        responseCode[0] = Integer.toString(((HttpURLConnection) conn).getResponseCode());
        responseCode[1] = ((HttpURLConnection) conn).getResponseMessage();
        StringBuilder responseStr;
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        responseStr = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            responseStr.append(inputLine);
        }
        responseCode[2] = responseStr.toString();
        log.debug(String.format("Response Code : %s", responseCode[0]));
        return responseCode;
    }

    public String[] executeHttpMethod(String method, String url, String body) throws IOException {
        if (baseUrl != null && !baseUrl.isEmpty() && !url.startsWith("http")) {
            url = baseUrl + url;
        }
        return this.executeHttpMethod(method, url, body, trustStore, null);
    }

    public String[] executeHttpBearerMethod(String method, String url, String body) throws IOException {
        if (baseUrl != null && !baseUrl.isEmpty() && !url.startsWith("http")) {
            url = baseUrl + url;
        }
        String[] response = this.executeHttpMethod("GET", tokenUrl, "");
        if (!response[0].equals("200")) {
            return response;
        }
        String bearerToken = response[2];
        return this.executeHttpMethod(method, url, body, trustStore, bearerToken);
    }
}
