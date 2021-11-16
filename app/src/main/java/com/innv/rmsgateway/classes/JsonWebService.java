package com.innv.rmsgateway.classes;


import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class JsonWebService {

    public static String getJSON(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setRequestProperty("Authorization",(Globals.appid.equals("")?Globals.appid_temp:Globals.appid));
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();
            setLastWsReturnCode(status);
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    //System.out.println(sb.toString());
                    return sb.toString();
                case 401:

            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (final java.net.SocketTimeoutException e) {
            // connection timed out...let's try again
            return null;
        } catch (ConnectException e) {
            // host and port combination not valid
            return null;
        }
        catch (IOException ex) {
            ex.printStackTrace();
            setLastWsReturnCode(0);
            setLastConnectionError(ex.getMessage());
        }  finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }


    public static  String getJSONPOST(String url1, HashMap<String,String> apiParams) throws IOException, JSONException {
        URL url = new URL(url1);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        String getParams=getQuery(apiParams);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(getParams);
        writer.flush();
        writer.close();
        os.close();
        conn.connect();

        int status = conn.getResponseCode();
        setLastWsReturnCode(status);
        switch (status) {
            case 200:
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                System.out.println(sb.toString());
                return sb.toString();
        }


        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Exception ex) {
            }
        }
        return null;
    }
    public static  String postJSON(String url1, String  jsonData,int timeOut) throws IOException, JSONException {
        URL url = new URL(url1);
        String charset = "UTF-8";
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization",Globals.appid);
        conn.setConnectTimeout(timeOut);
        conn.setReadTimeout(timeOut);
        //String getParams=getQuery(apiParams);

        conn.setDoOutput(true); // Triggers POST.
        conn.setRequestProperty("Accept-Charset", charset);
        conn.setRequestProperty("Content-Type", "application/json;charset=" + charset);

        String getParams=jsonData;

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonData);
        writer.flush();
        writer.close();
        os.close();
        conn.connect();

        int status = conn.getResponseCode();
        setLastWsReturnCode(status);
        switch (status) {
            case 200:
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                System.out.println(sb.toString());
                return sb.toString();
            case 400:
            case 401:
            case 403:

        }


        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Exception ex) {
            }
        }
        return null;
    }

    public static  String putJSON(String url1, String  jsonData,int timeOut) throws IOException, JSONException {
        URL url = new URL(url1);
        String charset = "UTF-8";
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization",Globals.appid);
        conn.setConnectTimeout(timeOut);
        conn.setReadTimeout(timeOut);
        //String getParams=getQuery(apiParams);

        conn.setDoOutput(true); // Triggers POST.
        conn.setRequestProperty("Accept-Charset", charset);
        conn.setRequestProperty("Content-Type", "application/json;charset=" + charset);

        String getParams=jsonData;

        OutputStreamWriter  writer = new OutputStreamWriter(conn.getOutputStream());
        /*BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));*/
        writer.write(jsonData);
        writer.flush();
        // writer.close();
        //os.close();
        conn.connect();

        int status = conn.getResponseCode();
        setLastWsReturnCode(status);
        switch (status) {
            case 200:
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                System.out.println(sb.toString());
                return sb.toString();
            case 400:
            case 401:
                BufferedReader brr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sbb = new StringBuilder();
                String line1;
                while ((line1 = brr.readLine()) != null) {
                    sbb.append(line1);
                }
                brr.close();
                System.out.println(sbb.toString());
                setLastWsReturnCode(status);
                return sbb.toString();

            case 403:

        }


        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Exception ex) {
            }
        }
        return null;
    }

    private static void setLastWsReturnCode(int status) {
        Globals.lastWsReturnCode=status;
    }
    private  static void setLastConnectionError(String message){
        Globals.lastConnectionError=message;
    }

    public static  String postJSON(String url1, JSONArray ja, int timeOut) throws IOException, JSONException {
        URL url = new URL(url1);
        String charset = "UTF-8";
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);


        conn.setConnectTimeout(timeOut);
        conn.setReadTimeout(timeOut);
        //String getParams=getQuery(apiParams);

        conn.setDoOutput(true); // Triggers POST.
        conn.setRequestProperty("Accept-Charset", charset);
        conn.setRequestProperty("Content-Type", "application/json;charset=" + charset);

        String getParams=ja.toString();

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(getParams);
        writer.flush();
        writer.close();
        os.close();
        conn.connect();

        int status = conn.getResponseCode();
        setLastWsReturnCode(status);
        switch (status) {
            case 200:
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                System.out.println(sb.toString());
                return sb.toString();
        }


        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Exception ex) {
            }
        }
        return null;
    }

    private static String getQuery(HashMap<String,String> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;


        for(Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    public static String makePostRequest(String uri, String json,int timeOut) {
        HttpURLConnection urlConnection;
        String url;
        String data = json;
        String result = null;
        try {
            //Connect
            urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Authorization",Globals.appid);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            //urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(timeOut);
            urlConnection.connect();

            int status = urlConnection.getResponseCode();
            setLastWsReturnCode(status);

            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(data);
            writer.close();
            outputStream.close();

            //Read
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

            String line = null;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            result = sb.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}


