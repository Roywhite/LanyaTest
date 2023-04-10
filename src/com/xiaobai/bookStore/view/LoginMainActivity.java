package com.xiaobai.bookStore.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.AesUtils;
import com.xiaobai.bookStore.util.ConnectionUtil;
import com.xiaobai.bookStore.util.GetSecUtil;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastNoLooperUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;

public class LoginMainActivity extends AppCompatActivity {
    private EditText mName;// 账号输入框
    private EditText mPasswd;// 密码输入框
    private CheckBox mRemember;// 记住密码选项
    private Button mLogin;// 登陆按钮
    private TextView mForget;// 忘记密码
    private TextView mZhuce;// 去注册
    private SendMessageToSql send = new SendMessageToSql();
    private SharedPreferences sp;// 保存数据:键值对
    private String aesPassword = "";//加密后的密码
    private Thread thread_login;// 登陆线程
    private boolean stopLoginThread;//子线程销毁标记
    private String sec;
    private Thread getSec;
    private boolean stopGetSecUpdateUIThread;//子线程销毁标记
    private boolean stopGetSecThread;//子线程销毁标记
    private Runnable runGetSecUpdateUI = new Runnable() {
        @Override
        public void run() {
            stopGetSecUpdateUIThread = false;
            while (!stopGetSecUpdateUIThread) {
                final String sec = GetSecUtil.getSec(send);
                if ("".equals(sec)) {
                    ToastUtil.showToast(LoginMainActivity.this, "连接服务器失败！");
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String name = sp.getString("name", "");
                            aesPassword = sp.getString("passwd", "");
                            String passwd = AesUtils.aesDecrypt(aesPassword, sec);
                            mName.setText(name);
                            mPasswd.setText(passwd);
                            mRemember.setChecked(true);
                        }
                    });
                }
                stopGetSecUpdateUIThread = true;
            }
        }
    };

    private Runnable runGetSec = new Runnable() {
        @Override
        public void run() {
            stopGetSecThread = false;
            while (!stopGetSecThread) {
                String name = mName.getText().toString();
                String passwd = mPasswd.getText().toString();
                String sec = GetSecUtil.getSec(send);
                if ("".equals(sec)) {
                    ToastUtil.showToast(LoginMainActivity.this, "连接服务器失败！");
                } else {
                    aesPassword = AesUtils.aesEncrypt(passwd, sec);
                    sp.edit().putString("name", name).commit();
                    sp.edit().putString("passwd", aesPassword).commit();
                    sp.edit().putBoolean("isRemember", true).commit();
                }
                stopGetSecThread = true;
            }
        }
    };

    private Runnable runLogin = new Runnable() {//登录子线程的方法
        @Override
        public void run() {
            //由于该线程在按钮中，通过点击开启子线程，当登录失败时标记为true后再次登录会无法进入到该线程，所以当点击该按钮
            //时应先重新赋为false，这样来控制线程的开关
            stopLoginThread = false;
            while (!stopLoginThread) {
                InputStream is = null;
                HttpURLConnection conn = null;
                try {
                    String name = mName.getText().toString();
                    String passwd = mPasswd.getText().toString();
                    boolean Bname = TextUtils.isEmpty(name);
                    boolean Bpasswd = TextUtils.isEmpty(passwd);
                    if (Bname && !Bpasswd) {
                        ToastUtil.showToast(LoginMainActivity.this, "账号为空");
                    } else if (Bpasswd && !Bname) {
                        ToastUtil.showToast(LoginMainActivity.this, "密码为空");
                    } else if (Bname && Bpasswd) {
                        ToastUtil.showToast(LoginMainActivity.this, "账号和密码为空");
                    } else {
                        // 判断是否有网络连接
                        boolean boolean_conn = ConnectionUtil.isConn(LoginMainActivity.this);
                        // System.out.println(boolean_conn);
                        // 如果没有网络
                        if (!boolean_conn) {
                            ToastUtil.showToast(LoginMainActivity.this, "无法连接到网络，请检查网络连接状况");
                        } else {
                            String sec = GetSecUtil.getSec(send);
                            if ("".equals(sec)) {
                                ToastUtil.showToast(LoginMainActivity.this, "连接服务器失败！");
                            } else {
                                aesPassword = AesUtils.aesEncrypt(passwd, sec);
                                // 连接服务器
                                conn = send.LoginConnection(name, aesPassword);
                                //上面判断有没有连接网络，这里判断有没有连接服务器
                                if (conn == null) {
                                    ToastUtil.showToast(LoginMainActivity.this, "连接服务器失败，请检查网络连接状况");
                                } else {
                                    // 有的话就做自己的操作
                                    // 获取服务器传来的账密状态
                                    is = conn.getInputStream();
                                    String response = ResponseUtil.getResponse(is);

                                    if ("YES".equals(response)) {
                                        if (sp.getBoolean("isRemember", false) == true) {
                                            sp.edit().putBoolean("isLogined", true).commit();
                                        }
                                        aesPassword = AesUtils.aesEncrypt(passwd, sec);
                                        sp.edit().putString("name", name).commit();
                                        sp.edit().putString("passwd", aesPassword).commit();
                                        Intent intent = new Intent(LoginMainActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                        ToastUtil.showToast(LoginMainActivity.this, "登陆成功");
                                    } else if ("NO".equals(response)) {
                                        // 当密码错误，清空勾选框时记住的密码，并且取消勾选，清空密码栏
                                        if (mRemember.isChecked() == true) {
                                            sp.edit().putBoolean("isRemember", false).commit();
                                            sp.edit().putString("name", "").commit();
                                            sp.edit().putString("passwd", "").commit();
                                            mRemember.setChecked(false);
                                            runOnUiThread(new Runnable() {//使用runOnUIThread()方法更新主线程
                                                @Override
                                                public void run() {
                                                    mPasswd.setText("");
                                                }
                                            });
                                        }
                                        stopLoginThread = true;
                                        ToastUtil.showToast(LoginMainActivity.this, "账号或密码错误");

                                    }
                                }
                            }
                        }
                    }
                    Thread.sleep(60000);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
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
        setContentView(R.layout.activity_login_main);
        initView();
        initData();
        initListener();
    }

    public static void closeStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
    }

    private void initListener() {
        mLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                thread_login = new Thread(runLogin);
                thread_login.start();
            }
        });

        mRemember.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String name = mName.getText().toString();
                String passwd = mPasswd.getText().toString();
                boolean Bname = TextUtils.isEmpty(name);
                boolean Bpasswd = TextUtils.isEmpty(passwd);
                if (isChecked == true) {
                    if (Bname || Bpasswd) {
                        ToastNoLooperUtil.showToast(LoginMainActivity.this, "账号或密码为空，无法勾选");
                        mRemember.setChecked(false);
                    } else {
                        getSec = new Thread(runGetSec);
                        getSec.start();
                    }
                } else {
                    sp.edit().putBoolean("isRemember", false).commit();
                    sp.edit().putString("rememberName", "").commit();
                    sp.edit().putString("rememberPasswd", "").commit();
                }
            }
        });
        // 设置功能：注册
        mZhuce.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginMainActivity.this, ZhuceActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // 设置功能：修改框中内容，自动取消勾选记住，并且忘记密码
        mName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mRemember.isChecked() == true) {
                    sp.edit().putBoolean("isRemember", false).commit();
                    sp.edit().putString("name", "").commit();
                    sp.edit().putString("passwd", "").commit();
                    mPasswd.setText("");
                    mRemember.setChecked(false);
                }
            }
        });

        // 设置功能：修改框中内容，自动取消勾选记住，并且忘记密码
        mPasswd.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mRemember.isChecked() == true) {
                    sp.edit().putBoolean("isRemember", false).commit();
                    sp.edit().putString("name", "").commit();
                    sp.edit().putString("passwd", "").commit();
                    mRemember.setChecked(false);
                }
            }
        });
        mForget.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginMainActivity.this, ForgetActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initData() {
        sp = getSharedPreferences("sp_file", MODE_PRIVATE);
        boolean isRemember = sp.getBoolean("isRemember", false);
        boolean isLogined = sp.getBoolean("isLogined", false);
        if (isRemember == true && isLogined == false) {
            getSec = new Thread(runGetSecUpdateUI);
            getSec.start();
        }
        if (isLogined == true) {
            ToastNoLooperUtil.showToast(LoginMainActivity.this,"自动登录成功！");
            Intent intent = new Intent(LoginMainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initView() {
        mName = (EditText) findViewById(R.id.main_et_name);
        mPasswd = (EditText) findViewById(R.id.main_et_passwd);
        mRemember = (CheckBox) findViewById(R.id.main_cb_remember);
        mLogin = (Button) findViewById(R.id.main_bn_login);
        mForget = (TextView) findViewById(R.id.main_tv_forgetpasswd);
        mZhuce = (TextView) findViewById(R.id.main_tv_zhuce);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click(); // 调用双击退出函数
        }
        return false;
    }

    /**
     * 双击退出函数
     */
    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
            System.exit(0);
        }
    }

    @Override
    protected void onDestroy() {
        stopLoginThread = true;
        stopGetSecUpdateUIThread = true;
        stopGetSecThread = true;
        super.onDestroy();
    }
}
