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
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 *
 * @author xyang
 */
public class AggregateRESTClient {
    private String baseUrl = null;
    private String username = null;
    private String password = null;
    private Logger log = Logger.getLogger(AggregateRESTClient.class);

    public AggregateRESTClient(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
    }

    public AggregateRESTClient() {
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String[] executeHttpMethod(String username, String password,  String method, String url, String body) throws IOException {
        String methods[] = method.split("/");
        method = methods[0];
        String type = (methods.length > 1 ? methods[1] : "json");
        URL urlObj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
        conn.setRequestMethod(method);
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            String userPassword = username + ":" + password;
            byte[] encoded = Base64.encodeBase64(userPassword.getBytes());
            String stringEncoded = new String(encoded);
            conn.setRequestProperty("Authorization", "Basic " + stringEncoded);
        }
        conn.setRequestProperty("Content-type", "application/"+type);
        conn.setRequestProperty("Accept", "application/"+type);
        if (body != null && !body.isEmpty()) {
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
        }
        log.debug(String.format("Sending %s request to URL : %s", method, url));
        String responseCode[] = new String[3];
        responseCode[0] = Integer.toString(conn.getResponseCode());
        responseCode[1] = conn.getResponseMessage();
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
        return this.executeHttpMethod(username, password, method, url, body);
    }
}
