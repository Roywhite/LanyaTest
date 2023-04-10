package com.xiaobai.bookStore.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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

public class HistoryBookActivity extends AppCompatActivity {
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();//与后台数据库做交互的服务类
    private List<String> dingDan = new ArrayList<>();//订单号
    private List<String> bookName = new ArrayList<>();//书名
    private List<String> bookStore = new ArrayList<>();//书店名
    private List<String> rentDay = new ArrayList<>();//租借天数
    private ListView listView;   //定义ListView对象，用来获取布局文件中的ListView控件
    private List<Map<String,Object>> list_map = new ArrayList<Map<String,Object>>(); //定义一个适配器对象
    private SharedPreferences sp;// 保存数据:键值对
    private String responseGetHistoryList;
    private Thread thread_getHistoryList;
    private boolean stopGetHistoryListThread;

    private Runnable runGetHistoryList = new Runnable() {
        @Override
        public void run() {
            stopGetHistoryListThread = false;
            while (!stopGetHistoryListThread) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    conn = sendMessageToSql.getHistoryBook(name);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(HistoryBookActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        responseGetHistoryList = ResponseUtil.getResponse(is);
                        if(responseGetHistoryList != null) {
                            String[] split = responseGetHistoryList.split("\\^");
                            //1.准备好数据源
                            for (int i = 0; i < split.length; i = i + 4) {
                                dingDan.add(split[i]);
                                bookName.add(split[i + 1]);
                                bookStore.add(split[i + 2]);
                                rentDay.add(split[i + 3]);
                            }
                            //循环为listView添加数据
                            for (int i = 0; i < dingDan.size(); i++) {
                                Map<String, Object> items = new HashMap<String, Object>(); //创建一个键值对的Map集合
                                items.put("dingDan", dingDan.get(i));      //放入订单， 根据下标获取数组
                                items.put("bookName", bookName.get(i));  //放入店名， 根据下标获取数组
                                items.put("bookStore", bookStore.get(i));
                                if(Integer.parseInt(rentDay.get(i))<0){
                                    items.put("rentDay", "超时");
                                }else{
                                    items.put("rentDay", "正常");
                                }
                                list_map.add(items);   //把这个存放好数据的Map集合放入到list中，这就完成类数据源的准备工作
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //2、创建适配器（可以使用外部类的方式、内部类方式等均可）
                                    SimpleAdapter simpleAdapter = new SimpleAdapter(HistoryBookActivity.this, list_map, R.layout.history_book_list, new String[]{"dingDan", "bookName", "bookStore", "rentDay"}, new int[]{R.id.history_book_dingdan, R.id.history_book_bookname, R.id.history_book_bookstore, R.id.history_book_day});
                                    //3、为listView加入适配器
                                    listView.setAdapter(simpleAdapter);
                                }
                            });
                        }else{
                            ToastUtil.showToast(HistoryBookActivity.this,"当前无订单信息！");
                        }
                        stopGetHistoryListThread = true;
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
        setContentView(R.layout.activity_history_book);
        initView();
        initData();
    }

    private void initData() {
        sp = this.getSharedPreferences("sp_file", Context.MODE_MULTI_PROCESS);
        thread_getHistoryList = new Thread(runGetHistoryList);
        thread_getHistoryList.start();
    }


    private void initView() {
        listView = findViewById(R.id.historybook_listview);
    }

    @Override
    protected void onDestroy() {
        stopGetHistoryListThread = true;
        super.onDestroy();
    }
}
