package com.xiaobai.bookStore.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.xiaobai.bookStore.util.BooleanNumber;
import com.xiaobai.bookStore.util.ConnectionUtil;
import com.xiaobai.bookStore.util.GetSecUtil;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;

public class ZhuceActivity extends Activity {
	private EditText mName;
	private EditText mNameTwo;
	private EditText mPasswd;
	private EditText mAgainPasswd;
	private EditText mPhone;
	private Button mZhuce;
	private TextView mBackLogin;
	private TextView mForget;
	private SendMessageToSql send = new SendMessageToSql();
	private String aesPassword= "";//加密后的密码
	private Thread thread_zhuce;// 注册线程
	private boolean stopZhuceThread;//子线程销毁标记
	private Runnable runZhuce = new Runnable() {
		@Override
		public void run() {
			stopZhuceThread = false;
			while(!stopZhuceThread) {
				String name = mName.getText().toString();
				String nameTwo = mNameTwo.getText().toString();
				String password = mPasswd.getText().toString();
				String againPasswd = mAgainPasswd.getText().toString();
				String phone = mPhone.getText().toString();
				//判断账号是不是纯数字
				boolean booleanNumberName = BooleanNumber.isNumeric(name);
				boolean empty_name = TextUtils.isEmpty(name);
				boolean empty_name_two = TextUtils.isEmpty(nameTwo);
				boolean empty_passwd = TextUtils.isEmpty(password);
				boolean empty_againPasswd = TextUtils.isEmpty(againPasswd);
				boolean empty_phone = TextUtils.isEmpty(phone);
				if(name.length()>7){
					ToastUtil.showToast(ZhuceActivity.this, "账号只允许少于等于7位");
				}
				if (!booleanNumberName) {
					ToastUtil.showToast(ZhuceActivity.this, "账号只允许纯数字");
				} else if (empty_name && !empty_name_two && !empty_againPasswd && !empty_passwd && !empty_phone) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名为空");
				} else if (!empty_name && empty_name_two && !empty_againPasswd && !empty_passwd && !empty_phone) {
					ToastUtil.showToast(ZhuceActivity.this, "昵称为空");
				} else if (!empty_name && !empty_name_two && !empty_againPasswd && empty_passwd && !empty_phone) {
					ToastUtil.showToast(ZhuceActivity.this, "密码为空");
				} else if (!empty_name && !empty_name_two && empty_againPasswd && !empty_passwd && !empty_phone) {
					ToastUtil.showToast(ZhuceActivity.this, "请再次输入密码");
				} else if (!empty_name && !empty_name_two && !empty_againPasswd && !empty_passwd && empty_phone) {
					ToastUtil.showToast(ZhuceActivity.this, "手机号不能为空");
				} else if (!empty_name && !empty_name_two && !againPasswd.equals(password) && !empty_phone) {
					ToastUtil.showToast(ZhuceActivity.this, "两次输入的密码不相同");
				} else if (!empty_name && !empty_name_two && !empty_againPasswd && !empty_passwd && !empty_phone) {

					// 判断是否有网络连接
					boolean boolean_conn = ConnectionUtil.isConn(ZhuceActivity.this);
					// 如果没有网络
					if (!boolean_conn) {
						ToastUtil.showToast(ZhuceActivity.this, "无法连接到网络，请检查网络连接状况");
					} else {
						//获取秘钥
						String sec = GetSecUtil.getSec(send);
						if("".equals(sec)){
							ToastUtil.showToast(ZhuceActivity.this, "无法连接服务器！");
						}else{
							// 连接服务器
							aesPassword = AesUtils.aesEncrypt(password, sec);
							HttpURLConnection conn = send.RegisterConnection(name, nameTwo, aesPassword, phone);
							InputStream is = null;
							//上面判断有没有连接网络，这里判断有没有连接服务器
							if (conn == null) {
								ToastUtil.showToast(ZhuceActivity.this, "连接服务器失败，请检查网络连接状况");
							} else {
								try {
									// 获取服务器传来的账密状态
									is = conn.getInputStream();
									String response = ResponseUtil.getResponse(is);
									//判断重名
									if ("true".equals(response)) {
										Intent intent = new Intent(ZhuceActivity.this, LoginMainActivity.class);
										startActivity(intent);
										ToastUtil.showToast(ZhuceActivity.this, "注册成功");
										finish();
									} else {
										ToastUtil.showToast(ZhuceActivity.this, "已经有重名用户，请重新输入");
										stopZhuceThread = true;
									}
								} catch (IOException e) {
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
				} else if (empty_name && empty_againPasswd && empty_passwd && empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "全部不能为空");
				} else if (!empty_name && empty_againPasswd && empty_passwd && !empty_phone && !empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "两次密码不能为空");
				} else if (empty_name && !empty_againPasswd && !empty_passwd && !empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名和昵称不能为空");
				} else if (empty_name && !empty_againPasswd && empty_passwd && !empty_phone && !empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名和密码框不能为空");
				} else if (empty_name && empty_againPasswd && !empty_passwd && !empty_phone && !empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名和再次输入框不能为空");
				} else if (empty_name && !empty_againPasswd && !empty_passwd && empty_phone && !empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名和手机号不能为空");
				} else if (!empty_name && !empty_againPasswd && empty_passwd && !empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "昵称和密码框不能为空");
				} else if (!empty_name && empty_againPasswd && !empty_passwd && !empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "昵称和再次输入框不能为空");
				} else if (!empty_name && !empty_againPasswd && !empty_passwd && empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "昵称和密保标记不能为空");
				} else if (!empty_name && empty_againPasswd && !empty_passwd && empty_phone && !empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "再次输入框和密保标记不能为空");
				} else if (!empty_name && !empty_againPasswd && empty_passwd && empty_phone && !empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "密码框和密保标记不能为空");
				} else if (!empty_name && empty_againPasswd && empty_passwd && empty_phone && !empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "密码框、再次输入框和密保标记不能为空");
				} else if (empty_name && !empty_againPasswd && empty_passwd && empty_phone && !empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名、密码框和密保标记不能为空");
				} else if (empty_name && empty_againPasswd && !empty_passwd && empty_phone && !empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名、再次输入框和密保标记不能为空");
				} else if (empty_name && empty_againPasswd && empty_passwd && !empty_phone && !empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名、密码框和再次输入框不能为空");
				} else if (empty_name && !empty_againPasswd && empty_passwd && !empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名、昵称和密码框不能为空");
				} else if (empty_name && empty_againPasswd && !empty_passwd && !empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名、昵称和再次输入框不能为空");
				} else if (empty_name && !empty_againPasswd && !empty_passwd && empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名、昵称和密保标记不能为空");
				} else if (!empty_name && empty_againPasswd && empty_passwd && !empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "昵称、密码框和再次输入框不能为空");
				} else if (!empty_name && !empty_againPasswd && empty_passwd && empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "昵称、密码框和密保标记不能为空");
				} else if (!empty_name && empty_againPasswd && !empty_passwd && empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "昵称、再次输入框和密保标记不能为空");
				} else if (empty_name && empty_againPasswd && empty_passwd && !empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名、昵称、密码和再次输入框不能为空");
				} else if (empty_name && !empty_againPasswd && empty_passwd && empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名、昵称、密码和密保标记不能为空");
				} else if (empty_name && empty_againPasswd && empty_passwd && empty_phone && !empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "用户名、密码、再次输入框和密保标记不能为空");
				} else if (!empty_name && empty_againPasswd && empty_passwd && empty_phone && empty_name_two) {
					ToastUtil.showToast(ZhuceActivity.this, "昵称、密码、再次输入框和密保标记不能为空");
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_zhuce);
		initView();
		initListener();
	}

	private void initListener() {
		mForget.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ZhuceActivity.this, ForgetActivity.class);
				startActivity(intent);
				finish();
			}
		});
		mBackLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ZhuceActivity.this, LoginMainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		mZhuce.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				thread_zhuce = new Thread(runZhuce);
				thread_zhuce.start();
			}

		});
	}

	private void initView() {
		mName = (EditText) findViewById(R.id.zhuce_et_name);
		mNameTwo = (EditText) findViewById(R.id.zhuce_et_name_two);
		mPasswd = (EditText) findViewById(R.id.zhuce_et_passwd);
		mAgainPasswd = (EditText) findViewById(R.id.zhuce_et_againPasswd);
		mZhuce = (Button) findViewById(R.id.zhuce_bn_zhuce);
		mPhone = (EditText) findViewById(R.id.zhuce_et_phone);
		mBackLogin = findViewById(R.id.zhuce_tv_backlogin);
		mForget = findViewById(R.id.zhuce_tv_forgetpasswd);
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
		stopZhuceThread = true;
		super.onDestroy();
	}
}
