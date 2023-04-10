package com.xiaobai.bookStore.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResponseUtil {
	public static String getResponse(InputStream is) {
		String str = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			str = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				if(is!=null) is.close();
				if(br!=null) br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return str;
	}
}
