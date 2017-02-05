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
import java.util.logging.Level;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author xyang
 */
public class AggregateRESTClient {
    private String baseUrl = null;
    private String username = null;
    private String password = null;
    private String trustStore = null;
    private String authServer = null;

    private Logger log = Logger.getLogger(AggregateRESTClient.class);

    public AggregateRESTClient(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
    }

    public AggregateRESTClient() {
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getAuthServer() {
        return authServer;
    }

    public void setAuthServer(String authServer) {
        this.authServer = authServer;
    }
    
    public String[] executeHttpMethod(String method, String url, String body, String trustStore, String authServer) throws IOException {
        this.prepareSSL();
        String methods[] = method.split("/");
        method = methods[0];
        String type = (methods.length > 1 ? methods[1] : "json");
        URL urlObj = new URL(url);
        URLConnection conn = urlObj.openConnection();
        if (url.startsWith("https:")) {
            ((HttpsURLConnection) conn).setRequestMethod(method);
        } else {
            ((HttpURLConnection) conn).setRequestMethod(method);
        }
        if (authServer != null && !authServer.isEmpty()) {
            this.prepareSSL();
            URL urlObjAuth = new URL(authServer);
            // assume https for authentication server
            HttpsURLConnection authConn = (HttpsURLConnection) urlObjAuth.openConnection();
            authConn.setRequestMethod("POST");
            //authConn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            String authBody = "";
            authConn.setDoOutput(true);
            try {
                DataOutputStream wr = new DataOutputStream(authConn.getOutputStream());
                wr.writeBytes(authBody);
                wr.flush();
            } catch (Exception ex) {
                log.error("Error writing to authServer"+authConn.toString()+":"+ex);
                return null; // throw ?
            }
            StringBuilder responseStr = null;
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(authConn.getInputStream()));
                String inputLine;
                responseStr = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    responseStr.append(inputLine);
                }
            } catch (Exception ex) {
                log.error("Error reading from authServer"+ex);
                return null; // throw ?
            }
            log.info("Return from authServer"+responseStr);
            JSONObject responseJSON = new JSONObject();
            try {
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(responseStr.toString());
                responseJSON = (JSONObject) obj;
            } catch (ParseException ex) {
                log.error("Error parsing json"+responseStr.toString());
                return null; // throw ?
            }
            String bearerToken = (String) responseJSON.get("access_token");
            log.info("Got token from authServer"+bearerToken);
            conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
        } else if (username != null && !username.isEmpty()) {
            String userPassword = username + ":" + password;
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
        return this.executeHttpMethod(method, url, body, trustStore, authServer);
    }

    private void prepareSSL() {
        TrustManager trustAllCerts[] = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            log.error("prepareSSL error:"+e);
        }
    }

}

