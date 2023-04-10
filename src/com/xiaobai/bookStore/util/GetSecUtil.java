package com.xiaobai.bookStore.util;


import com.xiaobai.bookStore.service.SendMessageToSql;

import java.io.InputStream;
import java.net.HttpURLConnection;

//获取密码的加密解密秘钥
public class GetSecUtil {

    public static String getSec(SendMessageToSql sendMessageToSql) {
        String result = "";
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            conn = sendMessageToSql.getSec();
            // 有的话就做自己的操作
            is = conn.getInputStream();
            result = ResponseUtil.getResponse(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
