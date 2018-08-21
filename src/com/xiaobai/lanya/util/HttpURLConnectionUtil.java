package com.xiaobai.lanya.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class HttpURLConnectionUtil {
	/**
	 * 这是一个用于连接服务器的工具类，返回一个连接好的对象
	 * @param conn
	 * @param url
	 * @return
	 */
	public HttpURLConnection httpconnection(HttpURLConnection conn,URL url) {
		try {
			conn = (HttpURLConnection) url.openConnection();
			if(conn!=null) {
				conn.setRequestMethod("POST");
				conn.setConnectTimeout(4000);
				conn.setReadTimeout(5000);
				conn.setDoInput(true);
				conn.setDoOutput(true);
			}
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
}
