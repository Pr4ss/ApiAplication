package com.vinic.miboaux;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.security.SecureRandom;

public class ApiRequester {
    private String IP;
    private String username;
    private String password;
    private String url;
    private String metodo;

    public ApiRequester(String IP, String username, String password, String url, String metodo) {
        this.IP = IP;
        this.username = username;
        this.password = password;
        this.url = url;
        this.metodo = metodo;
    }

    public String makeRequest() {
        try {
            HttpURLConnection connection = createConnection(url);

            int responseCode = connection.getResponseCode();

            if (responseCode == 401) {
                Map<String, String> authParams = extractAuthParams(connection);

                if (authParams != null) {
                    String authorizationHeader = createAuthorizationHeader(authParams, url);
                    connection = createConnection(url);
                    connection.setRequestProperty("Authorization", authorizationHeader);
                    responseCode = connection.getResponseCode();
                }
            }

            if (responseCode == 200) {
                return readResponse(connection);
            } else {
                return "Requisição sem sucesso. Código de erro: " + responseCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Erro de E/S ao acessar a API: " + e.getMessage();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "Erro ao calcular o hash MD5: " + e.getMessage();
        }
    }

    private HttpURLConnection createConnection(String url) throws IOException {
        URL endpointUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) endpointUrl.openConnection();
        connection.setRequestMethod(this.metodo);
        return connection;
    }

    private Map<String, String> extractAuthParams(HttpURLConnection connection) {
        String authHeader = connection.getHeaderField("WWW-Authenticate");
        if (authHeader != null && authHeader.startsWith("Digest ")) {
            String[] params = authHeader.substring(7).split(", ");
            Map<String, String> authParams = new HashMap<>();
            for (String param : params) {
                String[] parts = param.split("=");
                authParams.put(parts[0], parts[1].replace("\"", ""));
            }
            return authParams;
        }
        return null;
    }

    private String createAuthorizationHeader(Map<String, String> authParams, String url)
            throws NoSuchAlgorithmException {
        String realm = authParams.get("realm");
        String nonce = authParams.get("nonce");
        String qop = authParams.get("qop");
        String opaque = authParams.get("opaque");

        String cnonce = generateCnonce();
        String nc = "00000002";

        String hash1 = calculateMD5(username + ":" + realm + ":" + password);
        String hash2 = calculateMD5("GET:" + url);

        String response = calculateMD5(hash1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + hash2);

        String authorizationHeader = "Digest username=\"" + username + "\", realm=\"" + realm + "\", nonce=\"" + nonce
                + "\", uri=\"" + url + "\", response=\"" + response + "\", qop=" + qop + ", nc=" + nc + ", cnonce=\"" + cnonce
                + "\", opaque=\"" + opaque + "\"";

        return authorizationHeader;
    }

    private String calculateMD5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            hexString.append(String.format("%02x", b & 0xff));
        }
        return hexString.toString();
    }

    private String generateCnonce() {
        SecureRandom random = new SecureRandom();
        byte[] cnonceBytes = new byte[8];
        random.nextBytes(cnonceBytes);

        StringBuilder cnonceHex = new StringBuilder(16);
        for (byte b : cnonceBytes) {
            cnonceHex.append(String.format("%02x", b & 0xFF));
        }

        return cnonceHex.toString();
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();

        return response.toString();
    }
}
