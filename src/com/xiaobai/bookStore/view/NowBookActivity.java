package com.xiaobai.bookStore.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.TextView;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;

public class NowBookActivity extends AppCompatActivity {
    private TextView mBookName;
    private TextView mBookDay;//剩余时间
    private TextView mBookStore;
    private TextView mBookLocation;
    private TextView mBookProfile;//简介
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();
    private SharedPreferences sp;// 保存数据:键值对
    private Thread getNowBookThread;
    private boolean boolNowBook;
    private String nowbook;
    private Runnable runGetNowBook = new Runnable() {
        @Override
        public void run() {
            boolNowBook = false;
            while(!boolNowBook){
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    conn = sendMessageToSql.getNowBook(name);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(NowBookActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        // 获取服务器传来的分数
                        is = conn.getInputStream();
                        nowbook = ResponseUtil.getResponse(is);
                        if(nowbook != null) {
                            runOnUiThread(new Runnable() {//使用runOnUIThread()方法更新主线程
                                @Override
                                public void run() {
                                    String[] split = nowbook.split("\\^");
                                    mBookName.setText(split[0]);
                                    int day = Integer.parseInt(split[1]);
                                    if(day<0){
                                        int backDay = 0-day;
                                        mBookDay.setText("超时"+backDay+"天");
                                    }else{
                                        mBookDay.setText("剩余"+day+"天");
                                    }
                                    mBookStore.setText(split[2]);
                                    mBookLocation.setText(split[3]);
                                    mBookProfile.setText(split[4]);
                                }
                            });
                        }
                        boolNowBook = true;
                    }
                    Thread.sleep(60000);
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
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //继承activity时使用
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //继承AppCompatActivity时使用
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_nowbook);
        initView();
        initData();
    }



    private void initData() {
        sp = this.getSharedPreferences("sp_file", Context.MODE_MULTI_PROCESS);
        getNowBookThread = new Thread(runGetNowBook);
        getNowBookThread.start();
    }

    private void initView() {
        mBookName = findViewById(R.id.nowbook_nameText);
        mBookDay = findViewById(R.id.nowbook_timeText);
        mBookStore = findViewById(R.id.nowbook_nowbookstoreText);
        mBookLocation = findViewById(R.id.nowbook_locationText);
        mBookProfile = findViewById(R.id.nowbook_bookText);
    }

    @Override
    protected void onDestroy() {
        boolNowBook = true;
        super.onDestroy();
    }
}
