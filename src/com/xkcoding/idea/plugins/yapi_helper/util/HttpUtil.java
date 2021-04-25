package com.xkcoding.idea.plugins.yapi_helper.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class HttpUtil {

    private static final String CHARSET = "UTF-8";

    public static String doGet(String url, Map<String, String> params) {
        return doHttpRequst(buildGetRequest(url, params));
    }

    public static String doPost(String url, String body) {
        return doHttpRequst(buildPostRequestWithJsonType(url, body));
    }

    private static HttpGet buildGetRequest(String url, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() != 0) {
                sb.append("&");
            }
            sb.append(entry.getKey());
            sb.append("=");
            try {
                sb.append(URLEncoder.encode(entry.getValue(), CHARSET));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return new HttpGet(url + "?" + sb.toString());
    }

    private static HttpPost buildPostRequestWithJsonType(String url, String body) {
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/json;charset=utf-8");
            HttpEntity reqEntity = new StringEntity(body == null ? "" : body, CHARSET);
            httpPost.setEntity(reqEntity);
        } catch (Exception e) {
        }
        return httpPost;
    }

    private static String doHttpRequst(HttpUriRequest httpUriRequest) {

        SSLContext sslcontext = custom();
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(connManager).build();


        String responseStr = "";
        try {
            CloseableHttpResponse response = httpclient.execute(httpUriRequest);
            try {
                responseStr = getStreamAsString(response.getEntity().getContent());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseStr;
    }

    private static String getStreamAsString(InputStream stream) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, CHARSET));
            StringWriter writer = new StringWriter();

            char[] chars = new char[256];
            int count = 0;
            while ((count = reader.read(chars)) > 0) {
                writer.write(chars, 0, count);
            }

            return writer.toString();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    /**
     * 设置信任自签名证书
     */
    public static SSLContext custom() {
        SSLContext sc = null;
        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // 相信自己的CA和所有自签名的证书
            sc = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return sc;
    }

}
