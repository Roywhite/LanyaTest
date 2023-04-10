package com.xiaobai.bookStore.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastNoLooperUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;


//Fragment不能通过在按钮上加onclick方法设置监听
public class HomeFragment extends Fragment {
	private TextView mHead;
	private EditText mSearchText;//搜索框
	private Button mSearchButton;//搜索按钮
	private ImageButton mGetScore;//获取分数按钮
	private TextView mScores;//分数显示
	private Button mNowBook;//查看在借书籍简介
	private Button mHistoryBook;//查看历史订单信息
	private ImageView mCamaro;//扫码
	private Context mContext;//定义上下文
	private String aesPassword;
	private SharedPreferences sp;// 保存数据:键值对
	private String response;//分数
    private Thread threadGetScore_main;//主界面刚加载时的分数线程
    private Thread threadGetScore_click;//获取分数的子线程
    private boolean stopScoreThread = false;//子线程销毁标记
	private SendMessageToSql sendMessageToSql = new SendMessageToSql();
	private Thread threadGetBooleanRent;
	private boolean stopGetBooleanRentThread;
	private String getBooleanRent;


	private Runnable runGetScore = new Runnable(){//获取信用分的runnable
		@Override
		public void run() {
            stopScoreThread = false;
		    while(!stopScoreThread) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    aesPassword = sp.getString("passwd", "");
                    conn = sendMessageToSql.getScores(name, aesPassword);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(mContext, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        // 获取服务器传来的分数
                        is = conn.getInputStream();
                        response = ResponseUtil.getResponse(is);
                        getActivity().runOnUiThread(new Runnable() {//使用runOnUIThread()方法更新主线程
                            @Override
                            public void run() {
                                mScores.setGravity(Gravity.CENTER);
                                mScores.setText(response);
                            }
                        });
                        stopScoreThread = true;
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


	private Runnable runGetBooleanRent= new Runnable() {
		@Override
		public void run() {
			stopGetBooleanRentThread = false;
			while(!stopGetBooleanRentThread) {
				HttpURLConnection conn = null;
				InputStream is = null;
				try {
					String name = sp.getString("name", "");
					aesPassword = sp.getString("passwd", "");
					conn = sendMessageToSql.getBooleanRent(name,aesPassword);
					//判断有没有连接服务器
					if (conn == null) {
						ToastUtil.showToast(getActivity(), "连接服务器失败，请检查网络连接状况");
					} else {
						// 有的话就做自己的操作
						// 获取是否还在租借
						is = conn.getInputStream();
						getBooleanRent = ResponseUtil.getResponse(is);
						stopGetBooleanRentThread = true;
						//判断有没有租借
						if("true".equals(getBooleanRent)){
							Intent intent = new Intent(getActivity(),NowBookActivity.class);
							startActivity(intent);
						}else{
							ToastUtil.showToast(getActivity(),"您当前没有租借书籍！");
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_home_fragment, container, false);
		//获取上下文
		this.mContext = getActivity();
		//初始化组件
		initView(view);
		//初始化组件数据
		initData();
		//设置监听
		initListner(getActivity());
		return view;
	}



	private void initListner(final Activity activity) {
		mGetScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threadGetScore_click = new Thread(runGetScore);
                threadGetScore_click.start();
            }
        });
		mSearchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String searchText = mSearchText.getText().toString();
                boolean empty_search = TextUtils.isEmpty(searchText);
                if(!empty_search){
					Intent intent = new Intent(getActivity(),SearchActivity.class);
					intent.putExtra("bookName",searchText);
					startActivity(intent);
				}else{
					ToastNoLooperUtil.showToast(getActivity(),"搜索内容为空！");
				}
			}
		});

		mNowBook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				threadGetBooleanRent = new Thread(runGetBooleanRent);
				threadGetBooleanRent.start();
			}
		});

		mHistoryBook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),HistoryBookActivity.class);
				startActivity(intent);
			}
		});
	}



	/**
	 * 在fragment中使用getSharedPreferences需要先获取上下文
	 */
	private void initData() {
		sp = this.getActivity().getSharedPreferences("sp_file", Context.MODE_MULTI_PROCESS);
		mHead.setText("主界面");
		mCamaro.setImageResource(0);
		//加载界面时打开线程获取分数
        threadGetScore_main = new Thread(runGetScore);
        threadGetScore_main.start();
	}

	private void initView(View view) {

		mHead = (TextView) view.findViewById(R.id.head_name_in_lanya).findViewById(R.id.head_name);
		mCamaro = (ImageView) view.findViewById(R.id.head_name_in_lanya).findViewById(R.id.head_camaro);
		mSearchText = view.findViewById(R.id.lanya_et_search);
		mSearchButton = view.findViewById(R.id.lanya_bt_search);
		mGetScore = view.findViewById(R.id.lanya_ib_score);
		mScores = view.findViewById(R.id.lanya_tv_score);
		mNowBook = view.findViewById(R.id.lanya_bt_book_now);
		mHistoryBook = view.findViewById(R.id.lanya_bt_book_history);
	}

	@Override
	public void onDestroy() {
        stopScoreThread = true;
        stopGetBooleanRentThread = true;
		super.onDestroy();
	}
}
