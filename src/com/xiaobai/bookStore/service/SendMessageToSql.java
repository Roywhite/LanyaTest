package com.xiaobai.bookStore.service;

import com.xiaobai.bookStore.util.HttpURLConnectionUtil;
import com.xiaobai.bookStore.util.PropertiesUtil;

import org.json.JSONObject;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SendMessageToSql {
    HttpURLConnection conn = null;
    URL url = null;
    PropertiesUtil propUtil;
    HttpURLConnectionUtil httpconnect = new HttpURLConnectionUtil();
    List<String> a;
    List<String> aJsonName;

    //登录连接
    public HttpURLConnection LoginConnection(String mName, String mPasswd) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(mName);
        a.add(mPasswd);
        aJsonName.add("account");
        aJsonName.add("password");
        conn = fix(conn, url, "bookuser", "login", a, aJsonName);
        return conn;
    }

    //注册连接
    public HttpURLConnection RegisterConnection(String mName, String mNameTwo, String mPasswd, String mPhone) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(mName);
        a.add(mNameTwo);
        a.add(mPasswd);
        a.add(mPhone);
        aJsonName.add("account");
        aJsonName.add("name");
        aJsonName.add("password");
        aJsonName.add("backKey");
        conn = fix(conn, url, "bookuser", "register", a, aJsonName);
        return conn;
    }

    //忘记密码
    public HttpURLConnection ForgetConnection(String mName, String mPhone) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(mName);
        a.add(mPhone);
        aJsonName.add("account");
        aJsonName.add("backKey");
        conn = fix(conn, url, "bookuser", "forget", a, aJsonName);
        return conn;
    }


    //获取分数
    public HttpURLConnection getScores(String name, String passwd) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(name);
        a.add(passwd);
        aJsonName.add("account");
        aJsonName.add("password");
        conn = fix(conn, url, "bookuser", "getScore", a, aJsonName);
        return conn;
    }

    //获取租借状态
    public HttpURLConnection getBooleanRent(String name, String passwd) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(name);
        a.add(passwd);
        aJsonName.add("account");
        aJsonName.add("password");
        conn = fix(conn, url, "bookuser", "getBooleanRent", a, aJsonName);
        return conn;
    }

    //获取书籍名
    public HttpURLConnection getBookName(String name) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(name);
        aJsonName.add("account");
        conn = fix(conn, url, "bookuser", "getBookName", a, aJsonName);
        return conn;
    }

    //获取总租借天数
    public HttpURLConnection getResidueDegree(String name,String password) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(name);
        a.add(password);
        aJsonName.add("account");
        aJsonName.add("password");
        conn = fix(conn, url, "bookuser", "getResidueDegree", a, aJsonName);
        return conn;
    }

    //获取剩余租借天数
    public HttpURLConnection getRestDay(String name) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(name);
        aJsonName.add("account");
        conn = fix(conn, url, "bookuser", "getRestDay", a, aJsonName);
        return conn;
    }

    //更新总租借天数
    public HttpURLConnection updateResidueDay(String name) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(name);
        aJsonName.add("account");
        conn = fix(conn, url, "bookuser", "updateResidueDay", a, aJsonName);
        return conn;
    }

    //获取排行榜数据
    public HttpURLConnection getBookRankingList() {
        conn = fix(conn, url, "bookbooks", "getBookRankingList");
        return conn;
    }

    //获取搜索结果
    public HttpURLConnection getSearchList(String bookName) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(bookName);
        aJsonName.add("bookName");
        conn = fix(conn, url, "bookbooks", "getSearchList",a,aJsonName);
        return conn;
    }

    //获取租借时搜索结果
    public HttpURLConnection getBookRentSearchList(String bookName,String bookStore) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(bookName);
        a.add(bookStore);
        aJsonName.add("bookName");
        aJsonName.add("bookStore");
        conn = fix(conn, url, "bookbooks", "getBookRentSearchList",a,aJsonName);
        return conn;
    }

    //修改昵称
    public HttpURLConnection editPersonName(String name,String account) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(name);
        a.add(account);
        aJsonName.add("newName");
        aJsonName.add("account");
        conn = fix(conn, url, "bookuser", "editPersonName",a,aJsonName);
        return conn;
    }

    //修改密码
    public HttpURLConnection editPersonPasswd(String account,String oldPasswd,String newPasswd) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(account);
        a.add(oldPasswd);
        a.add(newPasswd);
        aJsonName.add("account");
        aJsonName.add("oldPasswd");
        aJsonName.add("newPasswd");
        conn = fix(conn, url, "bookuser", "editPersonPasswd",a,aJsonName);
        return conn;
    }

    //获取昵称
    public HttpURLConnection getName(String account) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(account);
        aJsonName.add("account");
        conn = fix(conn, url, "bookuser", "getName",a,aJsonName);
        return conn;
    }

    //获取密码的加密解密秘钥
    public HttpURLConnection getSec() {
        conn = fix(conn, url, "booksec", "getSec");
        return conn;
    }

    //更新分数
    public HttpURLConnection updateScore(String account,String passwd,String score) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(account);
        a.add(passwd);
        a.add(score);
        aJsonName.add("account");
        aJsonName.add("password");
        aJsonName.add("score");
        conn = fix(conn, url, "bookuser", "updateScore",a,aJsonName);
        return conn;
    }

    //获取在借书籍
    public HttpURLConnection getNowBook(String account) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(account);
        aJsonName.add("account");
        conn = fix(conn, url, "bookuser", "getNowBook",a,aJsonName);
        return conn;
    }

    //获取历史订单
    public HttpURLConnection getHistoryBook(String account) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(account);
        aJsonName.add("account");
        conn = fix(conn, url, "bookuser", "getHistoryBook",a,aJsonName);
        return conn;
    }

    //获取除了NET外的所有线下店列表
    public HttpURLConnection getBookLocationListExceptNet() {
        conn = fix(conn, url, "booklocation", "getBookLocationListExceptNet");
        return conn;
    }

    //获取收件地址
    public HttpURLConnection getReceiverAddress(String account) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(account);
        aJsonName.add("account");
        conn = fix(conn, url, "bookuser", "getReceiverAddress",a,aJsonName);
        return conn;
    }

    //更新用户的收件地址
    public HttpURLConnection updateReceiverAddress(String account,String address) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(account);
        a.add(address);
        aJsonName.add("account");
        aJsonName.add("receiverAddress");
        conn = fix(conn, url, "bookuser", "updateReceiverAddress",a,aJsonName);
        return conn;
    }

    //借书操作
    public HttpURLConnection rentBook(String aesText) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(aesText);
        aJsonName.add("aesText");
        conn = fix(conn, url, "bookuser", "rentBook",a,aJsonName);
        return conn;
    }

    //更新用户的收件地址
    public HttpURLConnection updateBookScore(String bookName,String score) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(bookName);
        a.add(score);
        aJsonName.add("bookName");
        aJsonName.add("score");
        conn = fix(conn, url, "bookuser", "updateBookScore",a,aJsonName);
        return conn;
    }

    //获取书友圈数据
    public HttpURLConnection getFriendTalk(String account) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(account);
        aJsonName.add("account");
        conn = fix(conn, url, "bookFriend", "getFriendTalk",a,aJsonName);
        return conn;
    }

    //获取租借书名名和评价状态
    public HttpURLConnection getBookNameAndBoolTalk(String account) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(account);
        aJsonName.add("account");
        conn = fix(conn, url, "bookFriend", "getBookNameAndBoolTalk",a,aJsonName);
        return conn;
    }

    //发布新的书评
    public HttpURLConnection addTalk(String account,String bookName,String talk) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(account);
        a.add(bookName);
        a.add(talk);
        aJsonName.add("account");
        aJsonName.add("bookName");
        aJsonName.add("talk");
        conn = fix(conn, url, "bookFriend", "addTalk",a,aJsonName);
        return conn;
    }

    //发布新的书评
    public HttpURLConnection deleteTalk(String account,String bookName,String talk) {
        a = new ArrayList<>();
        aJsonName = new ArrayList<>();
        a.add(account);
        a.add(bookName);
        a.add(talk);
        aJsonName.add("account");
        aJsonName.add("bookName");
        aJsonName.add("talk");
        conn = fix(conn, url, "bookFriend", "deleteTalk",a,aJsonName);
        return conn;
    }

    /**
     * 解决代码复用
     * @param conn
     * @param url
     * @param serverName
     * @param serverFunc
     * @return
     */
    public HttpURLConnection fix(HttpURLConnection conn, URL url, String serverName, String serverFunc) {
        try {
            url = new URL("http://" + propUtil.getProperties("url") + ":" + propUtil.getProperties("post") + "/" + serverName + "/" + serverFunc);
            conn = httpconnect.httpconnection(conn, url);
            OutputStream os = null;
            //判断有没有连接上服务器，如果连接上继续，没有连接上赋空
            try {
                os = conn.getOutputStream();
            } catch (Exception e) {
                conn = null;
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }


    /**
     * 解决代码复用问题，完成代码封装(有传值到后台的情况)
     *
     * @param conn       连接
     * @param url        地址
     * @param serverName 服务器名
     * @param serverFunc 服务方法
     * @param a          需要传的值
     * @param aJsonName  Json取值标志
     * @return
     */
    public HttpURLConnection fix(HttpURLConnection conn, URL url, String serverName, String serverFunc, List<String> a, List<String> aJsonName) {
        try {
            url = new URL("http://" + propUtil.getProperties("url") + ":" + propUtil.getProperties("post") + "/" + serverName + "/" + serverFunc);
            conn = httpconnect.httpconnection(conn, url);
            OutputStream os = null;
            //判断有没有连接上服务器，如果连接上继续，没有连接上赋空
            try {
                os = conn.getOutputStream();
            } catch (Exception e) {
                conn = null;
                e.printStackTrace();
            }
            if (conn != null) {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "utf-8"));

                /*封装子对象*/
                JSONObject ClientKey = new JSONObject();
                for (int i = 0; i < a.size(); i++) {
                    ClientKey.put(aJsonName.get(i), a.get(i));
                }

                /*把JSON数据转换成String类型使用输出流向服务器写*/
                String content = String.valueOf(ClientKey);
                pw.print(content);
                pw.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}
