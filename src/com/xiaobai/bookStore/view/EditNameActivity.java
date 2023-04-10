package com.xiaobai.bookStore.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastNoLooperUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;

public class EditNameActivity extends AppCompatActivity {

    private EditText mNewName;//新昵称
    private Button mPushNewName;
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();
    private SharedPreferences sp;// 保存数据:键值对
    private String result;//结果
    private Thread pushNameThread;
    private boolean boolPushThread;//结束的标志
    private Runnable runPushName = new Runnable() {
        @Override
        public void run() {
            boolPushThread = false;
            while(!boolPushThread){
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    conn = sendMessageToSql.editPersonName(mNewName.getText().toString(),name);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(EditNameActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        result = ResponseUtil.getResponse(is);
                        if("true".equals(result)){
                            Intent intent = new Intent(EditNameActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                            ToastUtil.showToast(EditNameActivity.this,"更新昵称成功！");
                        }else{
                            ToastUtil.showToast(EditNameActivity.this,"更新昵称失败，请重试！");
                        }
                        boolPushThread = true;
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
        setContentView(R.layout.activity_editname);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        mPushNewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean emptyName = TextUtils.isEmpty(mNewName.getText());
                if(emptyName){
                    ToastNoLooperUtil.showToast(EditNameActivity.this,"新昵称不允许为空！");
                }else {
                    pushNameThread = new Thread(runPushName);
                    pushNameThread.start();
                }
            }
        });
    }


    private void initData() {
        sp = this.getSharedPreferences("sp_file", Context.MODE_MULTI_PROCESS);
    }


    private void initView() {
        mNewName = findViewById(R.id.editName);
        mPushNewName = findViewById(R.id.editName_bt);
    }

    @Override
    protected void onDestroy() {
        boolPushThread = true;
        super.onDestroy();
    }
}
