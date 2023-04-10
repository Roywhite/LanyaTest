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
import com.xiaobai.bookStore.util.AesUtils;
import com.xiaobai.bookStore.util.GetSecUtil;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastNoLooperUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;

public class EditPasswdActivity extends AppCompatActivity {
    private EditText mOldPasswd;
    private EditText mNewPasswd;
    private EditText mNewPasswdAgain;
    private Button mPushPasswd;
    private SharedPreferences sp;// 保存数据:键值对
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();
    private String oldPasswd ;
    private String newPasswd;
    private String newPasswdAgain;
    private String result;//结果
    private Thread pushPasswdThread;
    private boolean boolPushThread;//结束的标志

    private Runnable runPushPasswd = new Runnable() {
        @Override
        public void run() {
            boolPushThread = false;
            while(!boolPushThread){
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String account = sp.getString("name", "");
                    String sec = GetSecUtil.getSec(sendMessageToSql);
                    if("".equals(sec)){
                        ToastUtil.showToast(EditPasswdActivity.this,"无法连接服务器！");
                    }else{
                        String aesOldPassword = AesUtils.aesEncrypt(oldPasswd,sec);
                        String aesNewPassword = AesUtils.aesEncrypt(newPasswd,sec);
                        conn = sendMessageToSql.editPersonPasswd(account,aesOldPassword,aesNewPassword);
                        //判断有没有连接服务器
                        if (conn == null) {
                            ToastUtil.showToast(EditPasswdActivity.this, "连接服务器失败，请检查网络连接状况");
                        } else {
                            // 有的话就做自己的操作
                            is = conn.getInputStream();
                            result = ResponseUtil.getResponse(is);
                            if("true".equals(result)){
                                //因为更新了密码，所以用户必须重新进行登录
                                //清除自动登录和记住密码状态
                                sp.edit().putBoolean("isLogined", false).commit();
                                sp.edit().putBoolean("isRemember", false).commit();
                                Intent intent = new Intent(EditPasswdActivity.this,LoginMainActivity.class);
                                startActivity(intent);
                                finish();
                                ToastUtil.showToast(EditPasswdActivity.this,"更新密码成功，请使用新密码重新进行登录！");
                            }else{
                                ToastUtil.showToast(EditPasswdActivity.this,"更新密码失败，请检查密码后重试！");
                            }
                            boolPushThread = true;
                        }
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
        setContentView(R.layout.activity_editpasswd);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        mPushPasswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldPasswd = mOldPasswd.getText().toString();
                newPasswd = mNewPasswd.getText().toString();
                newPasswdAgain = mNewPasswdAgain.getText().toString();
                boolean emptyOldPasswd = TextUtils.isEmpty(oldPasswd);
                boolean emptyNewPasswd = TextUtils.isEmpty(newPasswd);
                boolean emptyNewPasswdAgain = TextUtils.isEmpty(newPasswdAgain);
                if(emptyOldPasswd && !emptyNewPasswd && !emptyNewPasswdAgain){
                    ToastNoLooperUtil.showToast(EditPasswdActivity.this,"当前密码不能为空！");
                }else if(!emptyOldPasswd && emptyNewPasswd && !emptyNewPasswdAgain){
                    ToastNoLooperUtil.showToast(EditPasswdActivity.this,"新密码不能为空！");
                }else if(!emptyOldPasswd && !emptyNewPasswd && emptyNewPasswdAgain){
                    ToastNoLooperUtil.showToast(EditPasswdActivity.this,"必须再次输入一遍新密码！");
                }else if(emptyOldPasswd && emptyNewPasswd && !emptyNewPasswdAgain){
                    ToastNoLooperUtil.showToast(EditPasswdActivity.this,"当前密码和新密码不能为空！");
                }else if(emptyOldPasswd && !emptyNewPasswd && emptyNewPasswdAgain){
                    ToastNoLooperUtil.showToast(EditPasswdActivity.this,"当前密码和新密码验证不能为空！");
                }else if(!emptyOldPasswd && emptyNewPasswd && emptyNewPasswdAgain){
                    ToastNoLooperUtil.showToast(EditPasswdActivity.this,"新密码和新密码验证不能为空！");
                }else if(emptyOldPasswd && emptyNewPasswd && emptyNewPasswdAgain){
                    ToastNoLooperUtil.showToast(EditPasswdActivity.this,"全部不能为空！");
                }else{
                    if(!newPasswd.equals(newPasswdAgain)){
                        ToastNoLooperUtil.showToast(EditPasswdActivity.this,"两次输入的密码前后不一致，请检查！");
                    }else{
                        pushPasswdThread = new Thread(runPushPasswd);
                        pushPasswdThread.start();
                    }
                }
            }
        });
    }

    private void initData() {
        sp = this.getSharedPreferences("sp_file", Context.MODE_MULTI_PROCESS);
    }

    private void initView() {
        mOldPasswd = findViewById(R.id.editOldPasswd);
        mNewPasswd = findViewById(R.id.editNewPasswd);
        mNewPasswdAgain = findViewById(R.id.editNewPasswdAgain);
        mPushPasswd = findViewById(R.id.editPushNewPasswd);
    }

    @Override
    protected void onDestroy() {
        boolPushThread = true;
        super.onDestroy();
    }
}
