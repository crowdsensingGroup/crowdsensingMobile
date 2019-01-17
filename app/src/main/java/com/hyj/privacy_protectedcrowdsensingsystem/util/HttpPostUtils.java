package com.hyj.privacy_protectedcrowdsensingsystem.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by hyj on 2019/1/14.
 */

public class HttpPostUtils {
    public static String doPostRequest(String path, Object content) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            System.out.println("要发送的信息是：" + content);
            /*拼接url,Android这里需要换上远程地址，因为Android端没办法访问localhost，java的话本地tomcat运行的话倒是无妨*/
            String address = "http://47.100.55.245:8080/crowdsensing" + path;
            URL url = new URL(address);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //这两个参数必须加
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            //设置超时时间
            httpURLConnection.setReadTimeout(10 * 1000);
            httpURLConnection.setConnectTimeout(10 * 1000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.connect();

            out = new PrintWriter(httpURLConnection.getOutputStream());
            //在输出流中写入参数
            out.print(content);
            out.flush();

            if (httpURLConnection.getResponseCode() == 200) {
                in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
                String line;
                while ((line = in.readLine()) != null) {
                    result += line;
                }
            }
            System.out.println("服务器返回的结果是：" + result);
            return result;
        } catch (MalformedURLException e) {
            System.out.println("URL异常");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO异常");
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
