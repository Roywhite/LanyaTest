package com.xiaobai.lanya.view;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;

import com.xiaobai.lanya.R;
import com.xiaobai.lanya.service.SendMessageToSql;
import com.xiaobai.lanya.util.ConnectionUtil;
import com.xiaobai.lanya.util.ResponseUtil;
import com.xiaobai.lanya.util.ToastUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ZhuceActivity extends Activity {
	private EditText mName;
	private EditText mPasswd;
	private EditText mAgainPasswd;
	private EditText mPhone;
	private Button mZhuce;
	private SharedPreferences sp;// 保存数据:键值对
	Thread thread_zhuce;// 登陆线程

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_zhuce);
		initView();
		initListener();
	}

	private void initListener() {
		mZhuce.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				thread_zhuce = new Thread(new Runnable() {

					@Override
					public void run() {

						String name = mName.getText().toString();
						String password = mPasswd.getText().toString();
						String againPasswd = mAgainPasswd.getText().toString();
						String phone = mPhone.getText().toString();
						boolean empty_name = TextUtils.isEmpty(name);
						boolean empty_passwd = TextUtils.isEmpty(password);
						boolean empty_againPasswd = TextUtils.isEmpty(againPasswd);
						boolean empty_phone = TextUtils.isEmpty(phone);
						if (empty_name && !empty_againPasswd && !empty_passwd && !empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "用户名为空");
						} else if (!empty_name && !empty_againPasswd && empty_passwd && !empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "密码为空");
						} else if (!empty_name && empty_againPasswd && !empty_passwd && !empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "请再次输入密码");
						} else if (!empty_name && !empty_againPasswd && !empty_passwd && empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "手机号不能为空");
						} else if (!empty_name && !againPasswd.equals(password)) {
							ToastUtil.showToast(ZhuceActivity.this, "两次输入的密码不相同");
						} else if (!empty_name && !empty_againPasswd && !empty_passwd) {
							// 连接服务器
							SendMessageToSql send = new SendMessageToSql();
							HttpURLConnection conn = send.RegisterConnection(mName, mPasswd, mAgainPasswd,
									mPhone);
							// 判断是否有网络连接
							boolean boolean_conn = ConnectionUtil.isConn(ZhuceActivity.this);
							// System.out.println(boolean_conn);
							// 如果没有网络
							if (!boolean_conn) {
								ToastUtil.showToast(ZhuceActivity.this, "无法连接到服务器，请检查网络连接状况");
							} else {
								try {
									// 获取服务器传来的账密状态
									InputStream is = conn.getInputStream();
									String response = ResponseUtil.getResponse(is);
									//判断重名
									if ("true".equals(response)) {
										Intent intent = new Intent(ZhuceActivity.this, LoginMainActivity.class);
										startActivity(intent);
										ToastUtil.showToast(ZhuceActivity.this, "注册成功");
										finish();
									} else {
										ToastUtil.showToast(ZhuceActivity.this, "已经有重名用户，请重新输入");
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						} else if (empty_name && empty_againPasswd && empty_passwd && empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "全部不能为空");
						} else if (!empty_name && empty_againPasswd && empty_passwd && !empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "两次密码不能为空");
						} else if (empty_name && !empty_againPasswd && empty_passwd && !empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "用户名和再次输入框不能为空");
						} else if (empty_name && empty_againPasswd && !empty_passwd && !empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "用户名和密码框不能为空");
						} else if (empty_name && !empty_againPasswd && !empty_passwd && empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "用户名和手机号不能为空");
						} else if (!empty_name && empty_againPasswd && !empty_passwd && empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "再次输入框和手机号不能为空");
						} else if (!empty_name && !empty_againPasswd && empty_passwd && empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "密码框和手机号不能为空");
						} else if (!empty_name && empty_againPasswd && empty_passwd && empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "密码框、再次输入框和手机号不能为空");
						} else if (empty_name && !empty_againPasswd && empty_passwd && empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "用户名、密码框和手机号不能为空");
						} else if (empty_name && empty_againPasswd && !empty_passwd && empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "用户名、再次输入框和手机号不能为空");
						} else if (empty_name && empty_againPasswd && empty_passwd && !empty_phone) {
							ToastUtil.showToast(ZhuceActivity.this, "用户名、密码框和再次输入框不能为空");
						}
					}
				});
				thread_zhuce.start();
			}

		});
	}

	private void initView() {
		mName = (EditText) findViewById(R.id.zhuce_et_name);
		mPasswd = (EditText) findViewById(R.id.zhuce_et_passwd);
		mAgainPasswd = (EditText) findViewById(R.id.zhuce_et_againPasswd);
		mZhuce = (Button) findViewById(R.id.zhuce_bn_zhuce);
		mPhone = (EditText) findViewById(R.id.zhuce_et_phone);
		sp = getSharedPreferences("sp_file", MODE_PRIVATE);
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
}
