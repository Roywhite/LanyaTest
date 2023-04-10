package com.xiaobai.bookStore.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v4.widget.ViewDragHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastNoLooperUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;

public class MainActivity extends FragmentActivity {
	// 定义FragmentTabHost对象
	private FragmentTabHost mTabHost;
	private DrawerLayout mDrawer;//整个左侧菜单
	private LinearLayout mLinear;//主界面
	private TextView mName;//昵称
	private TextView mPersonMessage;//个人信息
	private TextView mRentBook;//租书
	private TextView mBuyScore;//信用分充值
	private TextView mHelp;//自助服务
	private TextView mVersion;//版本号
	private TextView mLogOut;//退出登录
	private Intent intent;
	private SharedPreferences sp;

	private SendMessageToSql sendMessageToSql = new SendMessageToSql();
	private String name;
	private Thread getNameThread;
	private boolean boolGetNameThread;

	private String aesPassword;
	private String score;//分数
	private String boolRent;//有没有租借
	private Thread threadGetScoreAndBoolRent;//主界面刚加载时的分数线程
	private boolean stopScoreThread = false;//子线程销毁标记

	private Runnable runGetScoreAndBoolRent = new Runnable(){//获取信用分的runnable
		@Override
		public void run() {
			stopScoreThread = false;
			while(!stopScoreThread) {
				HttpURLConnection connScore = null;
				HttpURLConnection connBoolRent = null;
				InputStream is = null;
				try {
					String name = sp.getString("name", "");
					aesPassword = sp.getString("passwd", "");
					connScore = sendMessageToSql.getScores(name, aesPassword);
					connBoolRent = sendMessageToSql.getBooleanRent(name,aesPassword);
					//判断有没有连接服务器
					if (connScore == null||connBoolRent == null) {
						ToastUtil.showToast(MainActivity.this, "连接服务器失败，请检查网络连接状况");
					} else {
						// 有的话就做自己的操作
						// 获取服务器传来的分数
						is = connScore.getInputStream();
						score = ResponseUtil.getResponse(is);
						is = connBoolRent.getInputStream();
						boolRent = ResponseUtil.getResponse(is);
						if(Integer.parseInt(score)>=90&&"false".equals(boolRent)){
							Intent intent = new Intent(MainActivity.this,ChooseStoreDialogActivity.class);
							startActivity(intent);
						}else{
							ToastUtil.showToast(MainActivity.this,"信用分不足或当前有未归还书籍！请核实！");
						}
						stopScoreThread = true;
					}
					Thread.sleep(60000);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (connScore != null) {
						connScore.disconnect();
					}
					if (connBoolRent != null) {
						connBoolRent.disconnect();
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

	private Runnable runGetName = new Runnable() {
		@Override
		public void run() {
			boolGetNameThread = false;
			while(!boolGetNameThread){
				HttpURLConnection conn = null;
				InputStream is = null;
				try {
					String account = sp.getString("name", "");
					conn = sendMessageToSql.getName(account);
					//判断有没有连接服务器
					if (conn == null) {
						ToastUtil.showToast(MainActivity.this, "连接服务器失败，请检查网络连接状况");
					} else {
						// 有的话就做自己的操作
						is = conn.getInputStream();
						name = ResponseUtil.getResponse(is);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mName.setText(name);
							}
						});
						boolGetNameThread = true;
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

	// 定义一个布局
	private LayoutInflater layoutInflater;

	// 定义数组来存放Fragment界面
	private Class fragmentArray[] = { HomeFragment.class, ReturnBookFragment.class,BookFriendTalkFragment.class, TuijianFragment.class };

	// 定义数组来存放导航图标
	private int mImageViewArray[] = { R.drawable.one_change_icon_image, R.drawable.two_change_icon_image,R.drawable.four_change_icon_image,
			R.drawable.three_change_icon_image };

	// Tab选项卡的文字
	private String mTextviewArray[] = { "主界面", "归还/续租","书评圈", "好书推荐" };

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initDate();
		initListener();
	}

	private void initDate() {
		sp = getSharedPreferences("sp_file", MODE_PRIVATE);
		getNameThread = new Thread(runGetName);
		getNameThread.start();
	}


	//设置监听
	private void initListener() {
		mDrawer.addDrawerListener(new DrawerListener() {
			
			@Override
			public void onDrawerStateChanged(int arg0) {
			}
			
			@Override
			public void onDrawerSlide(View drawView, float slideOffset) {
				//设置主布局随菜单滑动而滑动
				int drawViewWidth = drawView.getWidth();
				mLinear.setTranslationX(drawViewWidth*slideOffset);
			}
			
			@Override
			public void onDrawerOpened(View arg0) {
			}
			
			@Override
			public void onDrawerClosed(View arg0) {
			}
		});

		mBuyScore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,BuyScoreActivity.class);
				startActivity(intent);
			}
		});

		mPersonMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,EditPersonActivity.class);
				startActivity(intent);
			}
		});
		mRentBook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				threadGetScoreAndBoolRent = new Thread(runGetScoreAndBoolRent);
				threadGetScoreAndBoolRent.start();
			}
		});
		mVersion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(MainActivity.this,"Version:3.1.0");
			}
		});
		mLogOut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//清除自动登录状态
				sp.edit().putBoolean("isLogined", false).commit();
				intent = new Intent(MainActivity.this,LoginMainActivity.class);
				startActivity(intent);
				ToastNoLooperUtil.showToast(MainActivity.this,"退出成功");
				finish();
			}
		});
		mHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(MainActivity.this,ServerYourelfActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 初始化组件
	 */
	private void initView() {
		// 实例化布局对象
		layoutInflater = LayoutInflater.from(this);
		mDrawer = (DrawerLayout) findViewById(R.id.drawer_main);
		setDrawerLeftEdgeSize(this, mDrawer, 0.08f);
		mLinear = (LinearLayout) findViewById(R.id.main_main);

		mName = findViewById(R.id.youhua).findViewById(R.id.youhua_name);
		mPersonMessage = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_person_message);
		mRentBook = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_sendBack);
		mVersion = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_version);
		mLogOut = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_logout);
		mHelp = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_Help);
		mBuyScore = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_buyscore);


		// 实例化TabHost对象，得到TabHost
		mTabHost = (FragmentTabHost) findViewById(R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.qq_tabcontent);
		//去除底部导航栏分割线
		mTabHost.getTabWidget().setDividerDrawable(null);

		// 得到fragment的个数
		int count = fragmentArray.length;

		for (int i = 0; i < count; i++) {
			// 为每一个Tab按钮设置图标、文字和内容
			TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
			// 将Tab按钮添加进Tab选项卡中
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
		}
	}

	/**
	 * 给Tab按钮设置图标和文字
	 */
	private View getTabItemView(int index) {
		View view = layoutInflater.inflate(R.layout.nav_item, null);

		ImageView imageView = (ImageView) view.findViewById(R.id.nav_icon_iv);
		imageView.setImageResource(mImageViewArray[index]);

		TextView textView = (TextView) view.findViewById(R.id.nav_text_tv);
		textView.setText(mTextviewArray[index]);

		return view;
	}
	
	//点击返回键返回桌面而不是退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
 
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

	/**
	 * 抽屉滑动范围控制
	 *
	 * @param activity
	 * @param drawerLayout
	 * @param displayWidthPercentage 占全屏的份额0~1
	 */
	@SuppressLint("LongLogTag")
	private void setDrawerLeftEdgeSize(Activity activity, DrawerLayout drawerLayout, float displayWidthPercentage) {
		if (activity == null || drawerLayout == null)
			return;
		try {
			// find ViewDragHelper and set it accessible
			Field leftDraggerField = drawerLayout.getClass().getDeclaredField("mLeftDragger");
			leftDraggerField.setAccessible(true);
			ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);
			// find edgesize and set is accessible
			Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
			edgeSizeField.setAccessible(true);
			int edgeSize = edgeSizeField.getInt(leftDragger);
			// set new edgesize
			// Point displaySize = new Point();
			DisplayMetrics dm = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) (dm.widthPixels * displayWidthPercentage)));
		} catch (NoSuchFieldException e) {
			Log.e("NoSuchFieldException", e.getMessage().toString());
		} catch (IllegalArgumentException e) {
			Log.e("IllegalArgumentException", e.getMessage().toString());
		} catch (IllegalAccessException e) {
			Log.e("IllegalAccessException", e.getMessage().toString());
		}
	}

	@Override
	protected void onDestroy() {
		boolGetNameThread = true;
		stopScoreThread = true;
		super.onDestroy();
	}
}