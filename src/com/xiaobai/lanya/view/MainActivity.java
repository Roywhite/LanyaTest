package com.xiaobai.lanya.view;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.xiaobai.lanya.R;
import com.xiaobai.lanya.util.ToastNoLooperUtil;

public class MainActivity extends FragmentActivity {
	// 定义FragmentTabHost对象
	private FragmentTabHost mTabHost;
	private DrawerLayout mDrawer;//整个左侧菜单
	private LinearLayout mLinear;//主界面
	private TextView mPersonMessage;//个人信息
	private TextView mSendBack;//反馈
	private TextView mHelp;//自助服务
	private TextView mVersion;//版本号
	private TextView mLogOut;//退出登录

	// 定义一个布局
	private LayoutInflater layoutInflater;

	// 定义数组来存放Fragment界面
	private Class fragmentArray[] = { LanyaFragment.class, GonggaolanFragment.class, SetFragment.class };

	// 定义数组来存放导航图标
	private int mImageViewArray[] = { R.drawable.one_change_icon_image, R.drawable.two_change_icon_image,
			R.drawable.three_change_icon_image };

	// Tab选项卡的文字
	private String mTextviewArray[] = { "设备", "公告栏", "设置" };

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initListener();
	}

	//设置监听
	private void initListener() {
		mDrawer.setDrawerListener(new DrawerListener() {
			
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

		mPersonMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(MainActivity.this,"修改个人信息");
			}
		});
		mSendBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(MainActivity.this,"反馈");
			}
		});
		mVersion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(MainActivity.this,"Version:2.5.0");
			}
		});
		mLogOut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(MainActivity.this,"退出登录");
			}
		});
		mHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(MainActivity.this,"自助服务");
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
		mLinear = (LinearLayout) findViewById(R.id.main_main);

		mPersonMessage = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_person_message);
		mSendBack = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_sendBack);
		mVersion = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_version);
		mLogOut = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_logout);
		mHelp = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_Help);
		
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
}