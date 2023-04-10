package com.xiaobai.bookStore.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.AesUtils;
import com.xiaobai.bookStore.util.ConnectionUtil;
import com.xiaobai.bookStore.util.GetSecUtil;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;

public class ForgetActivity extends Activity {
	private EditText mName;//账号
	private EditText mPhone;//密保手机
	private Button mGoForget;
	private TextView mZhuce;
	private TextView mBackLogin;
	private Thread thread_forget;
	private SendMessageToSql send = new SendMessageToSql();
	private boolean stopForgetThread;//子线程销毁标记
	private Runnable runForget = new Runnable() {
		@Override
		public void run() {
			stopForgetThread = false;
			while(!stopForgetThread) {
				String name = mName.getText().toString();
				String phone = mPhone.getText().toString();
				boolean Bname = TextUtils.isEmpty(name);
				boolean Bphone = TextUtils.isEmpty(phone);
				if (Bname && !Bphone) {
					ToastUtil.showToast(ForgetActivity.this, "账号为空");
				} else if (Bphone && !Bname) {
					ToastUtil.showToast(ForgetActivity.this, "密保标记为空");
				} else if (Bname && Bphone) {
					ToastUtil.showToast(ForgetActivity.this, "账号和密保标记为空");
				} else {
					// 判断是否有网络连接
					boolean boolean_conn = ConnectionUtil.isConn(ForgetActivity.this);
					// 如果没有网络
					if (!boolean_conn) {
						ToastUtil.showToast(ForgetActivity.this, "无法连接到网络，请检查网络连接状况");
					} else {
						// 连接服务器
						HttpURLConnection conn = send.ForgetConnection(name, phone);
						InputStream is = null;

						//上面判断有没有连接网络，这里判断有没有连接服务器
						if (conn == null) {
							ToastUtil.showToast(ForgetActivity.this, "连接服务器失败，请检查网络连接状况");
						} else {
							// 获取服务器传来的账密状态
							try {
								is = conn.getInputStream();
								String response = ResponseUtil.getResponse(is);
								// 判断是否注册
								if ("false".equals(response)) {
									Looper.prepare();
									new AlertDialog.Builder(ForgetActivity.this).setTitle("密码提示")
											.setMessage("请确认输入的用户名已注册，或者密保标记与用户名匹配").show();
									Looper.loop();
									stopForgetThread = true;
								} else {
									Looper.prepare();
									String sec = GetSecUtil.getSec(send);
									if("".equals(sec)){
										ToastUtil.showToast(ForgetActivity.this,"连接服务器失败！");
									}else{
										response = AesUtils.aesDecrypt(response, sec);
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
									stopForgetThread = true;
								}
								Thread.sleep(60000);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}finally {
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
				}
			}
		}
	};

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
		mZhuce.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ForgetActivity.this, ZhuceActivity.class);
				startActivity(intent);
				finish();
			}
		});
		mBackLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ForgetActivity.this, LoginMainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		mGoForget.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				thread_forget = new Thread(runForget);
				thread_forget.start();
			}
		});	
	}

	private void initData() {
	}

	private void initView() {
		mName = (EditText) findViewById(R.id.forget_et_name);
		mGoForget = (Button) findViewById(R.id.forget_bn_forget);
		mPhone = (EditText) findViewById(R.id.forget_et_phone);
		mBackLogin = findViewById(R.id.forget_tv_backlogin);
		mZhuce = findViewById(R.id.forget_tv_zhuce);
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
		stopForgetThread = true;
		super.onDestroy();
	}
}
