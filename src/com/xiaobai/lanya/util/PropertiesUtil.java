package com.xiaobai.lanya.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

public class PropertiesUtil {
	private static Properties properties = new Properties();
	
	public static String getProperties(String name) {
		String value = null;
		//读取属性文件a.properties
		try {
		InputStream in = PropertiesUtil.class.getResourceAsStream("url.properties");
			properties.load(in);     ///加载属性列表
			value = properties.getProperty(name);
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
}
