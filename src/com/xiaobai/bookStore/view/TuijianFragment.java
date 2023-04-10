package com.xiaobai.bookStore.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastNoLooperUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TuijianFragment extends Fragment {
    private ListView listView;   //定义ListView对象，用来获取布局文件中的ListView控件
    private List<Map<String, Object>> list_map; //定义一个适配器对象
    private TextView mHead;
    private ImageView mFlash;//刷新按钮
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();//与后台数据库做交互的服务类
    private int[] index = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};//排名数字
    private List<String> bookName;//书名
    private List<String> bookScore;//评分

    private String responseBookRankingList;//排行榜数据
    private Thread thread_getBookRankingList;//获取排行榜数据的线程
    private boolean stopGetBookRankingListThread;//子线程销毁标记
    private Runnable runGetBookRankingList = new Runnable() {
        @Override
        public void run() {
            stopGetBookRankingListThread = false;
            while (!stopGetBookRankingListThread) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    conn = sendMessageToSql.getBookRankingList();
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(getActivity(), "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        responseBookRankingList = ResponseUtil.getResponse(is);
                        String[] split = responseBookRankingList.split("~");
                        bookName = new ArrayList<>();//书名
                        bookScore = new ArrayList<>();//评分
                        //1.准备好数据源
                        for (int i = 0; i < split.length; i = i + 2) {
                            bookName.add(split[i]);
                            bookScore.add(split[i + 1]);
                        }
                        list_map = new ArrayList<Map<String, Object>>(); //定义一个适配器对象
                        //循环为listView添加数据
                        for (int i = 0; i < 10; i++) {
                            Map<String, Object> items = new HashMap<String, Object>(); //创建一个键值对的Map集合，用来存放序号，书名，分数
                            items.put("index", index[i]);  //放入序号， 根据下标获取数组
                            items.put("bookName", bookName.get(i));      //放入书名， 根据下标获取数组
                            items.put("bookScore", bookScore.get(i));
                            list_map.add(items);   //把这个存放好数据的Map集合放入到list中，这就完成类数据源的准备工作
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //2、创建适配器（可以使用外部类的方式、内部类方式等均可）
                                SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), list_map, R.layout.tuijian_paihang, new String[]{"index", "bookName", "bookScore"}, new int[]{R.id.tuijian_num, R.id.tuijian_tv_bookname, R.id.tuijian_tv_score});
                                //3、为listView加入适配器
                                listView.setAdapter(simpleAdapter);
                            }
                        });
                        stopGetBookRankingListThread = true;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_tuijian_fragment, container, false);
        initView(view);
        initData();
        initListener();
        return view;
    }

    private void initListener() {
        mFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thread_getBookRankingList = new Thread(runGetBookRankingList);
                thread_getBookRankingList.start();
                ToastNoLooperUtil.showToast(getContext(), "刷新成功！");
            }
        });
    }

    private void initData() {
        mHead.setText("好书推荐排行榜");
        mFlash.setImageResource(R.drawable.flash_image);
        thread_getBookRankingList = new Thread(runGetBookRankingList);
        thread_getBookRankingList.start();

    }

    private void initView(View view) {

        mHead = (TextView) view.findViewById(R.id.head_name_in_set).findViewById(R.id.head_name);
        mFlash = (ImageView) view.findViewById(R.id.head_name_in_set).findViewById(R.id.head_camaro);
        listView = view.findViewById(R.id.tuijian_listview);
    }

    @Override
    public void onDestroy() {
        stopGetBookRankingListThread = true;
        super.onDestroy();
    }
}