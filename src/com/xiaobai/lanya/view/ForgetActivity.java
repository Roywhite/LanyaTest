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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ForgetActivity extends Activity {
	private EditText mName;
	private EditText mPhone;
	private Button mGoForget;
	private SharedPreferences sp;
	Thread thread_forget;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_forget);
		initView();
		initData();
		initListener();
	}

	private void initListener() {
		mGoForget.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				thread_forget = new Thread(new Runnable() {

					@Override
					public void run() {
						String name = mName.getText().toString();
						String phone = mPhone.getText().toString();
						boolean Bname = TextUtils.isEmpty(name);
						boolean Bphone = TextUtils.isEmpty(phone);
						if (Bname && !Bphone) {
							ToastUtil.showToast(ForgetActivity.this, "账号为空");
						} else if (Bphone && !Bname) {
							ToastUtil.showToast(ForgetActivity.this, "密保手机号为空");
						} else if (Bname && Bphone) {
							ToastUtil.showToast(ForgetActivity.this, "账号和密保手机号为空");
						} else {
							// 连接服务器
							SendMessageToSql send = new SendMessageToSql();
							HttpURLConnection conn = send.ForgetConnection(mName, mPhone);
							// 判断是否有网络连接
							boolean boolean_conn = ConnectionUtil.isConn(ForgetActivity.this);
							// System.out.println(boolean_conn);
							// 如果没有网络
							if (!boolean_conn) {
								ToastUtil.showToast(ForgetActivity.this, "无法连接到服务器，请检查网络连接状况");
							} else {
								// 获取服务器传来的账密状态
								try {
									InputStream is = conn.getInputStream();
									String response = ResponseUtil.getResponse(is);
									// 判断是否注册
									if ("false".equals(response)) {
										Looper.prepare();
										new AlertDialog.Builder(ForgetActivity.this).setTitle("密码提示")
												.setMessage("请确认输入的用户名已注册，或者手机号与用户名匹配").show();
										Looper.loop();
									} else {
										Looper.prepare();
										new AlertDialog.Builder(ForgetActivity.this).setTitle("密码提示")
												.setMessage("密码为:" + response)
												.setPositiveButton("确定", new DialogInterface.OnClickListener() {

													@Override
													public void onClick(DialogInterface dialog, int which) {
														Intent intent = new Intent(ForgetActivity.this,
																LoginMainActivity.class);
														startActivity(intent);
														finish();
													}
												}).show();
										Looper.loop();
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				});
				thread_forget.start();
			}
		});	
	}

	private void initData() {
		sp = getSharedPreferences("sp_file", MODE_PRIVATE);
	}

	private void initView() {
		mName = (EditText) findViewById(R.id.forget_et_name);
		mGoForget = (Button) findViewById(R.id.forget_bn_forget);
		mPhone = (EditText) findViewById(R.id.forget_et_phone);
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
