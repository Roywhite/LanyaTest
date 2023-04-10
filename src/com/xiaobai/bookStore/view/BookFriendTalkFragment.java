package com.xiaobai.bookStore.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

public class BookFriendTalkFragment extends Fragment implements AdapterView.OnItemClickListener{
    private TextView mHead;
    private ImageView mPublicText;//发布书评
    private SharedPreferences sp;// 保存数据:键值对
    private List<String> title;//标题
    private List<String> talk;//书评
    private List<String> accountList;
    private String name;
    private String talkByClick;
    private String titleByClick;
    private ListView listView;   //定义ListView对象，用来获取布局文件中的ListView控件
    private List<Map<String,Object>> list_map; //定义一个适配器对象
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();
    private Thread threadGetFriendTalk;
    private boolean stopGetFriendTalkThread;
    private String getFriendTalk;

    private Thread threadDeleteTalk;
    private boolean stopDeleteTalkThread;
    private String deleteTalk;

    private Runnable runDeleteTalk = new Runnable() {
        @Override
        public void run() {
            stopDeleteTalkThread = false;
            while(!stopDeleteTalkThread){
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    name = sp.getString("name", "");
                    String[] bookNameSplit = titleByClick.split("了");
                    String bookNameReal = "";
                    for(int i=0;i<bookNameSplit.length-1;i++){
                        bookNameReal = bookNameSplit[i+1]+bookNameReal;
                    }
                    String[] talkSplit = talkByClick.split("[：]");
                    String talkReal = "";
                    for(int i = 0;i<talkSplit.length-1;i++){
                        talkReal = talkReal + talkSplit[i+1];
                    }
                    conn = sendMessageToSql.deleteTalk(name,bookNameReal,talkReal);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(getActivity(), "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        deleteTalk = ResponseUtil.getResponse(is);
                        if("true".equals(deleteTalk)){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    successMessageDialog("提示","成功删除书评！");
                                }
                            });
                        }else{
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messageDialog("提示",deleteTalk);
                                }
                            });
                        }
                        stopDeleteTalkThread = true;
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

    private Runnable runGetFriendTalk = new Runnable() {
        @Override
        public void run() {
            stopGetFriendTalkThread = false;
            while(!stopGetFriendTalkThread){
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    name = sp.getString("name", "");
                    conn = sendMessageToSql.getFriendTalk(name);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(getActivity(), "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        getFriendTalk = ResponseUtil.getResponse(is);
                        if(getFriendTalk!=null&&!"false".equals(getFriendTalk)&&!"empty".equals(getFriendTalk)){
                            String[] split = getFriendTalk.split("\\^");
                            title = new ArrayList<>();
                            talk = new ArrayList<>();
                            accountList = new ArrayList<>();
                            //1.准备好数据源
                            for (int i = 0; i < split.length; i = i + 4) {
                                accountList.add("NO."+split[i]);
                                title.add(split[i+1]+"：推荐了"+split[i+2]);
                                talk.add("评语："+split[i + 3]);
                            }
                            list_map = new ArrayList<Map<String, Object>>(); //定义一个适配器对象
                            //循环为listView添加数据
                            for (int i = 0; i < title.size(); i++) {
                                Map<String, Object> items = new HashMap<String, Object>(); //创建一个键值对的Map集合
                                items.put("account",accountList.get(i));
                                items.put("title", title.get(i));
                                items.put("talk", talk.get(i));
                                list_map.add(items);   //把这个存放好数据的Map集合放入到list中，这就完成类数据源的准备工作
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //2、创建适配器（可以使用外部类的方式、内部类方式等均可）
                                    SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), list_map, R.layout.book_friend_talk, new String[]{"account","title", "talk"}, new int[]{R.id.book_friend_account,R.id.book_friend_title, R.id.book_friend_neirong});
                                    //3、为listView加入适配器
                                    listView.setAdapter(simpleAdapter);
                                    //设置点击事件
                                    listView.setOnItemClickListener(BookFriendTalkFragment.this);
                                    simpleAdapter.notifyDataSetChanged();
                                }
                            });
                        }else if(getFriendTalk==null||"false".equals(getFriendTalk)){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messageDialog("提示","必须进行租借后才能看见书友的评论！");
                                }
                            });
                        }else if("empty".equals(getFriendTalk)){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messageDialog("提示","当前书籍圈无人发表评论！");
                                }
                            });
                        }
                        stopGetFriendTalkThread = true;
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
        View view = inflater.inflate(R.layout.activity_book_friend_talk, container, false);
        //初始化组件
        initView(view);
        //初始化组件数据
        initData();
        //设置监听
        initListner(getActivity());
        return view;
    }

    private void initListner(FragmentActivity activity) {
        mPublicText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),AddTalkActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        sp = this.getActivity().getSharedPreferences("sp_file", Context.MODE_MULTI_PROCESS);
        mHead.setText("书评圈");
        mPublicText.setImageResource(R.drawable.add_image);
        threadGetFriendTalk = new Thread(runGetFriendTalk);
        threadGetFriendTalk.start();
    }

    private void initView(View view) {
        mHead = (TextView) view.findViewById(R.id.head_name_in_book_friend).findViewById(R.id.head_name);
        mPublicText = (ImageView) view.findViewById(R.id.head_name_in_book_friend).findViewById(R.id.head_camaro);
        listView = view.findViewById(R.id.book_friend_listview);
    }

    @Override
    public void onDestroy() {
        stopGetFriendTalkThread = true;
        stopDeleteTalkThread = true;
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        HashMap<String, String> map = (HashMap<String, String>) adapterView.getItemAtPosition(position);
        talkByClick = map.get("talk");
        titleByClick = map.get("title");
        String accountByClick = map.get("account");
        String[] split = accountByClick.split("[.]");
        if(name.equals(split[1])){
            deleteMessageDialog(titleByClick,talkByClick);
        }else {
            messageDialog(titleByClick, talkByClick);
        }
    }

    /**
     * 信息的提示框
     */
    private void messageDialog(String title,String text) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();//创建对话框
        dialog.setTitle(title);//设置对话框标题
        dialog.setMessage(text);//设置文字显示内容
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();//显示对话框
    }

    /**
     * 信息的提示框
     */
    private void successMessageDialog(String title,String text) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();//创建对话框
        dialog.setTitle(title);//设置对话框标题
        dialog.setMessage(text);//设置文字显示内容
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                threadGetFriendTalk = new Thread(runGetFriendTalk);
                threadGetFriendTalk.start();
                dialog.dismiss();
            }
        });
        dialog.show();//显示对话框
    }

    private void deleteMessageDialog(String title,String text) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();//创建对话框
        dialog.setTitle(title);//设置对话框标题
        dialog.setMessage(text);//设置文字显示内容
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "删除书评", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                threadDeleteTalk = new Thread(runDeleteTalk);
                threadDeleteTalk.start();
            }
        });
        dialog.show();//显示对话框
    }
}
