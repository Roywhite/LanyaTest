package com.xiaobai.bookStore.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
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

public class AddTalkActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private TextView mHead;
    private ImageView mCamaro;
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();//与后台数据库做交互的服务类
    private SharedPreferences sp;
    private List<String> booksName = new ArrayList<>();//书名
    private List<String> booleanTalk = new ArrayList<>();//是否书评
    private ListView listView;   //定义ListView对象，用来获取布局文件中的ListView控件
    private List<Map<String, Object>> list_map = new ArrayList<Map<String, Object>>(); //定义一个适配器对象
    private String booksNameByClick;
    private String boolTalkByClick;
    private String newTalk;

    private String bookNameAndBooleanTalk;
    private Thread threadGetBookNameAndBooleanTalk;
    private boolean stopGetBookNameAndBooleanTalkThread = false;

    private String sendNewTalk;
    private Thread threadSendNewTalk;
    private boolean stopSendNewTalk = false;

    private Runnable runSendNewTalk = new Runnable() {
        @Override
        public void run() {
            while(!stopSendNewTalk){
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String account = sp.getString("name", "");
                    conn = sendMessageToSql.addTalk(account,booksNameByClick,newTalk);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(AddTalkActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        sendNewTalk = ResponseUtil.getResponse(is);
                        if("true".equals(sendNewTalk)){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    successAddTalkDialog("提示","书评发布成功！");
                                }
                            });
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messageDialog("警告",sendNewTalk);
                                }
                            });
                        }
                        stopSendNewTalk = true;
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

    private Runnable runGetBookNameAndBooleanTalk = new Runnable() {
        @Override
        public void run() {
            while(!stopGetBookNameAndBooleanTalkThread){
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String account = sp.getString("name", "");
                    conn = sendMessageToSql.getBookNameAndBoolTalk(account);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(AddTalkActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        bookNameAndBooleanTalk = ResponseUtil.getResponse(is);
                        if (bookNameAndBooleanTalk != null && !"".equals(bookNameAndBooleanTalk)) {
                            String[] split = bookNameAndBooleanTalk.split("\\^");
                            //1.准备好数据源
                            for (int i = 0; i < split.length; i = i + 2) {
                                booksName.add(split[i]);
                                if ("1".equals(split[i + 1])) {
                                    booleanTalk.add("已书评");
                                } else {
                                    booleanTalk.add("当前未书评");
                                }
                            }
                            //循环为listView添加数据
                            for (int i = 0; i < booksName.size(); i++) {
                                Map<String, Object> items = new HashMap<String, Object>(); //创建一个键值对的Map集合
                                items.put("booksName", booksName.get(i));
                                items.put("booleanTalk", booleanTalk.get(i));
                                list_map.add(items);   //把这个存放好数据的Map集合放入到list中，这就完成类数据源的准备工作
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //2、创建适配器（可以使用外部类的方式、内部类方式等均可）
                                    SimpleAdapter simpleAdapter = new SimpleAdapter(AddTalkActivity.this, list_map, R.layout.bookname_booleantalk, new String[]{"booksName", "booleanTalk"}, new int[]{R.id.bookName_text, R.id.booleanTalk_text});
                                    //3、为listView加入适配器
                                    listView.setAdapter(simpleAdapter);
                                    //设置点击事件
                                    listView.setOnItemClickListener(AddTalkActivity.this);
                                }
                            });
                        } else {
                            ToastUtil.showToast(AddTalkActivity.this, "当前无书籍！");
                        }
                        stopGetBookNameAndBooleanTalkThread = true;
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
        setContentView(R.layout.activity_add_talk);
        initView();
        initData();
    }

    private void initData() {
        mHead.setText("已租借列表/发布书评");
        mCamaro.setImageResource(0);
        sp = this.getSharedPreferences("sp_file", Context.MODE_MULTI_PROCESS);
        threadGetBookNameAndBooleanTalk = new Thread(runGetBookNameAndBooleanTalk);
        threadGetBookNameAndBooleanTalk.start();
    }

    private void initView() {
        listView = findViewById(R.id.add_talk_listview);
        mHead = (TextView) findViewById(R.id.head_name_in_add_talk).findViewById(R.id.head_name);
        mCamaro = (ImageView) findViewById(R.id.head_name_in_add_talk).findViewById(R.id.head_camaro);
    }

    @Override
    protected void onDestroy() {
        stopGetBookNameAndBooleanTalkThread = true;
        stopSendNewTalk = true;
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        HashMap<String, String> map = (HashMap<String, String>) adapterView.getItemAtPosition(position);
        booksNameByClick = map.get("booksName");
        boolTalkByClick = map.get("booleanTalk");
        if("已书评".equals(boolTalkByClick)){
            messageDialog("提示","您已经发布过该书的书评了！");
        }else {
            addTalkDialog("");
        }
    }

    /**
     * 信息的提示框
     */
    private void messageDialog(String title,String text) {
        AlertDialog dialog = new AlertDialog.Builder(AddTalkActivity.this).create();//创建对话框
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
     * 发布书评
     */
    private void addTalkDialog(String text) {
        /*@setView 装入一个EditView
         */
        final EditText editText = new EditText(AddTalkActivity.this);
        editText.setText(text);
        final AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(AddTalkActivity.this);
        inputDialog.setCancelable(false);
        inputDialog.setTitle("发表书评（字数小于等于130）").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newTalk = editText.getText().toString();
                        //预防用户传入切割符进入字符，如“1111^1111”或“^111”
                        String[] split = newTalk.split("\\^");
                        if(split.length>1){
                            addTalkDialog(newTalk);
                            ToastNoLooperUtil.showToast(AddTalkActivity.this,"不允许输入不被允许的特殊符号！");
                        }else{
                            //避免用户输入“1111^”这种
                            newTalk = split[0];
                            int length = newTalk.length();
                            if(length>130){
                                addTalkDialog(newTalk);
                                ToastNoLooperUtil.showToast(AddTalkActivity.this,"字数多余130字！请重新编辑！");
                            }else if(length==0) {
                                ToastNoLooperUtil.showToast(AddTalkActivity.this,"书评不允许为空！请重新编辑！");
                            }else {
                                threadSendNewTalk = new Thread(runSendNewTalk);
                                threadSendNewTalk.start();
                            }
                        }
                    }
                });
        inputDialog.show();
    }

    private void successAddTalkDialog(String title ,String message) {
        AlertDialog dialog = new AlertDialog.Builder(AddTalkActivity.this).create();//创建对话框
        dialog.setTitle(title);//设置对话框标题
        dialog.setMessage(message);//设置文字显示内容
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(AddTalkActivity.this, MainActivity.class);
                startActivity(intent);
                AddTalkActivity.this.finish();
            }
        });
        dialog.show();//显示对话框
    }

}
