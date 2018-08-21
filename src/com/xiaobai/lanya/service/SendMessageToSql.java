package com.xiaobai.lanya.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.xiaobai.lanya.R;
import com.xiaobai.lanya.util.HttpURLConnectionUtil;
import com.xiaobai.lanya.util.PropertiesUtil;
import com.xiaobai.lanya.util.ResponseUtil;
import com.xiaobai.lanya.view.LoginMainActivity;

import android.view.View;
import android.widget.EditText;

public class SendMessageToSql {
	HttpURLConnection conn = null;
	URL url = null;
	PropertiesUtil propUtil;
	HttpURLConnectionUtil httpconnect = new HttpURLConnectionUtil();
	//登录连接
	public HttpURLConnection LoginConnection(EditText mName,EditText mPasswd) {
		try {
			// post
			url = new URL("http://"+propUtil.getProperties("url")+":"+propUtil.getProperties("post")+"/"+propUtil.getProperties("hostName")+"/LoginServlet");
			conn = httpconnect.httpconnection(conn,url);
			OutputStream os = conn.getOutputStream();
			PrintWriter pw = new PrintWriter(os);
			pw.print("name=" + mName.getText().toString() + "&passwd=" + mPasswd.getText().toString());
			pw.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	
	//注册连接
	public HttpURLConnection RegisterConnection(EditText mName,EditText mPasswd,EditText mAgainPasswd,EditText mPhone) {
		HttpURLConnection conn = null;
		URL url = null;
		try {
			// post
			url = new URL("http://"+propUtil.getProperties("url")+":"+propUtil.getProperties("post")+"/"+propUtil.getProperties("hostName")+"/RegistServlet");
			conn = httpconnect.httpconnection(conn,url);
			OutputStream os = conn.getOutputStream();
			PrintWriter pw = new PrintWriter(os);
			pw.print("name=" + mName.getText().toString() + "&passwd=" + mPasswd.getText().toString()+"&phone="+mPhone.getText().toString());
			pw.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	
	//忘记密码
		public HttpURLConnection ForgetConnection(EditText mName,EditText mPhone) {
			HttpURLConnection conn = null;
			URL url = null;
			try {
				// post
				url = new URL("http://"+propUtil.getProperties("url")+":"+propUtil.getProperties("post")+"/"+propUtil.getProperties("hostName")+"/ForgetServlet");
				conn = httpconnect.httpconnection(conn,url);
				OutputStream os = conn.getOutputStream();
				PrintWriter pw = new PrintWriter(os);
				pw.print("name=" + mName.getText().toString() + "&phone="+mPhone.getText().toString());
				pw.flush();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return conn;
		}
}
