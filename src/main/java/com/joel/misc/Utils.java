package com.joel.misc;

import com.google.gson.Gson;
import com.joel.PristineClient;
import com.joel.model.PristineRequest;
import com.joel.servlets.PristineServlet;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by rakjavik on 9/23/2017.
 */
public class Utils {

    private static Gson gson = new Gson();


    private static boolean verbose = false;
    private static boolean debug = false;

    public static boolean writePristineRequest(PristineRequest request,OutputStream out,Logger logger) throws IOException {
        String json = gson.toJson(request);
        BufferedReader reader = new BufferedReader(new StringReader(json));
        PrintWriter writer = new PrintWriter(out);
        debug("Starting write sequence",Utils.class.getName(),logger);
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                writer.write(line);
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        log("Request sent",logger);
        return true;
    }
    public static PristineRequest readPristineRequest(InputStream in,Logger logger) throws IOException {

        StringBuilder builder = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        debug("Starting read sequence",Utils.class.getName(),logger);
        try {
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            debug(e.getMessage(),Utils.class.getName(),logger);
        }
        PristineRequest request = gson.fromJson(builder.toString(),PristineRequest.class);
        debug("Request received", Utils.class.getName(),logger);
        return request;
    }

    public static byte[] decode(String base64) {

        return Base64.getDecoder().decode(base64);
    }

    public static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static void log(String message,Logger logger) {
        if(verbose && message != null) {
            logger.info(message);
        }
    }
    public static void debug(String message,String className,Logger logger) {
        if(debug && message != null) {
            logger.info("DEBUG--" + className + "-" + message);
        }
    }

    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    public static String getHTTP(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        String newString = urlToRead.substring(0,urlToRead.indexOf("8080/")+5) + PristineServlet.CONTEXT_ROOT + urlToRead.substring(urlToRead.indexOf("8080/")+5);
        URL url = new URL(newString);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
        conn.setRequestProperty("Accept","*/*");

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    public static String sendNotify(String hostname, String status,Logger logger) {
        Utils.log("notify from " + hostname + ", status - " + status, logger);
        try {
            return Utils.getHTTP("http://" + PristineClient.serverIP + ":8080/pristine?requestType=notify&hostname="
                    + hostname + "&update=" + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        Utils.getHTTP("http://10.0.0.100:8080/this/part");
    }
}
