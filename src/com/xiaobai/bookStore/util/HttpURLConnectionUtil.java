package com.xiaobai.bookStore.util;

import java.net.HttpURLConnection;
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
				conn.setRequestProperty("ser-Agent", "Fiddler");
				conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");
				conn.setDoInput(true);
				conn.setDoOutput(true);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
}
