package com.xiaobai.bookStore.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.EditText;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;

public class ChooseStoreDialogActivity extends AppCompatActivity {

    private SharedPreferences sp;
    private int yourChoice;
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();
    private String responseGetBookLocationExceptNetList;//获取除了NET外的所有线下店
    private Thread thread_getBookLocationExceptNetList;//获取搜索数据的线程
    private boolean stopGetBookLocationListExceptNetThread;//子线程销毁标记

    private Runnable runGetBookLocationExceptNet = new Runnable() {
        @Override
        public void run() {
            stopGetBookLocationListExceptNetThread = false;
            while(!stopGetBookLocationListExceptNetThread){
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    conn = sendMessageToSql.getBookLocationListExceptNet();
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(ChooseStoreDialogActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        responseGetBookLocationExceptNetList = ResponseUtil.getResponse(is);
                        if (responseGetBookLocationExceptNetList != null && !"".equals(responseGetBookLocationExceptNetList)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String items = responseGetBookLocationExceptNetList;
                                    chooseBookStoreFromLocal(items);
                                }
                            });
                        } else {
                            ToastUtil.showToast(ChooseStoreDialogActivity.this, "暂无线下店！");
                        }
                        stopGetBookLocationListExceptNetThread = true;
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
        setContentView(R.layout.activity_choosestore);
        initData();
    }

    private void initData() {
        sp = this.getSharedPreferences("sp_file", Context.MODE_MULTI_PROCESS);
        showListDialog();
    }



    private void showListDialog() {
        final String[] items = { "线上店","线下店" };
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(ChooseStoreDialogActivity.this);
        listDialog.setTitle("请选择线上租借或者是线下店租借");
        listDialog.setCancelable(false);
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                if("线上店".equals(items[which])){
                    sp.edit().putString("rentBookStore", "NET").commit();
                    showSearchDialog();
                }else{
                    thread_getBookLocationExceptNetList = new Thread(runGetBookLocationExceptNet);
                    thread_getBookLocationExceptNetList.start();
                }
            }
        });
        listDialog.show();
    }


    private void chooseBookStoreFromLocal(String itemString){
        String[] split = itemString.split("\\^");
        final String[] items = split;
        yourChoice = -1;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(ChooseStoreDialogActivity.this);
        singleChoiceDialog.setTitle("线下店列表");
        singleChoiceDialog.setCancelable(false);
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (yourChoice != -1) {
                            sp.edit().putString("rentBookStore", items[yourChoice]).commit();
                            showSearchDialog();
                        }else{
                            sp.edit().putString("rentBookStore", items[0]).commit();
                            showSearchDialog();
                        }
                    }
                });
        singleChoiceDialog.show();
    }

    private void showSearchDialog() {
        /*@setView 装入一个EditView
         */
        final EditText editText = new EditText(ChooseStoreDialogActivity.this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(ChooseStoreDialogActivity.this);
        inputDialog.setCancelable(false);
        inputDialog.setTitle("请输入需要租借的书籍（支持模糊查询，不输入查询该店所有书籍！）").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String searchText = editText.getText().toString();
                        Intent intent = new Intent(ChooseStoreDialogActivity.this,RentBookActivity.class);
                        intent.putExtra("bookName",searchText);
                        startActivity(intent);
                    }
                }).show();
    }

    @Override
    protected void onDestroy() {
        stopGetBookLocationListExceptNetThread = true;
        super.onDestroy();
    }
}
