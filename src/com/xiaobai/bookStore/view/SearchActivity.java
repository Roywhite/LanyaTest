package com.xiaobai.bookStore.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {
    private TextView mHead;//顶部文字
    private ImageView mNull;//设置为0不可见
    private Intent intent;
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();//与后台数据库做交互的服务类
    private String bookNameFromIntent;
    private List<String> bookName = new ArrayList<>();//书名
    private List<String> locationName = new ArrayList<>();//书店名
    private List<String> locationFullName = new ArrayList<>();//地址
    private List<String> booksNum = new ArrayList<>();//存货
    private ListView listView;   //定义ListView对象，用来获取布局文件中的ListView控件
    private List<Map<String,Object>> list_map = new ArrayList<Map<String,Object>>(); //定义一个适配器对象

    private String responseGetSearchList;//搜索数据
    private Thread thread_getSearchList;//获取搜索数据的线程
    private boolean stopGetSearchListThread;//子线程销毁标记

    private Runnable runGetSearchList = new Runnable() {
        @Override
        public void run() {
            stopGetSearchListThread = false;
            while (!stopGetSearchListThread) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    conn = sendMessageToSql.getSearchList(bookNameFromIntent);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(SearchActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        responseGetSearchList = ResponseUtil.getResponse(is);
                        if(responseGetSearchList != null && !"".equals(responseGetSearchList)) {
                            String[] split = responseGetSearchList.split("\\^");
                            //1.准备好数据源
                            for (int i = 0; i < split.length; i = i + 4) {
                                bookName.add(split[i]);
                                locationName.add(split[i + 1]);
                                locationFullName.add(split[i + 2]);
                                booksNum.add(split[i + 3]);
                            }
                            //循环为listView添加数据
                            for (int i = 0; i < locationName.size(); i++) {
                                Map<String, Object> items = new HashMap<String, Object>(); //创建一个键值对的Map集合，用来存放店铺名、书名、地址、剩余量
                                items.put("bookName", bookName.get(i));      //放入书名， 根据下标获取数组
                                items.put("locationName", locationName.get(i));  //放入店名， 根据下标获取数组
                                items.put("locationFullName", locationFullName.get(i));
                                items.put("booksNum", booksNum.get(i));
                                list_map.add(items);   //把这个存放好数据的Map集合放入到list中，这就完成类数据源的准备工作
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //2、创建适配器（可以使用外部类的方式、内部类方式等均可）
                                    SimpleAdapter simpleAdapter = new SimpleAdapter(SearchActivity.this, list_map, R.layout.search_list, new String[]{"bookName", "locationName", "locationFullName", "booksNum"}, new int[]{R.id.search_list_book_name, R.id.search_list_bookStore, R.id.search_list_bookAddress, R.id.search_list_bookNum});
                                    //3、为listView加入适配器
                                    listView.setAdapter(simpleAdapter);
                                }
                            });
                        }else{
                            ToastUtil.showToast(SearchActivity.this,"无搜索结果！");
                        }
                        stopGetSearchListThread = true;
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
        setContentView(R.layout.activity_search);
        initView();
        initData();
        initListener();
    }

    private void initListener() {

    }

    private void initData() {
        mHead.setText("搜索结果");
        mNull.setImageResource(0);
        thread_getSearchList = new Thread(runGetSearchList);
        thread_getSearchList.start();
    }

    private void initView() {
        mHead = findViewById(R.id.head_name_in_search).findViewById(R.id.head_name);
        mNull = findViewById(R.id.head_name_in_search).findViewById(R.id.head_camaro);
        intent = getIntent();
        bookNameFromIntent = intent.getExtras().getString("bookName");
        listView = findViewById(R.id.search_lv_listView);
    }

    @Override
    protected void onDestroy() {
        stopGetSearchListThread = true;
        super.onDestroy();
    }
}
