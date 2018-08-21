package com.xiaobai.lanya.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.xiaobai.lanya.R;
import com.xiaobai.lanya.adapter.DeviceAdapter;
import com.xiaobai.lanya.util.BlueToothControllerUtil;
import com.xiaobai.lanya.util.ToastNoLooperUtil;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

//Fragment不能通过在按钮上加onclick方法设置监听
public class LanyaFragment extends Fragment {
	private BlueToothControllerUtil mController = new BlueToothControllerUtil();
	private TextView mHead;
	private TextView mText;//文字
	private Switch mSwitch;//开关
	private ListView mListView;//显示设备
	private DeviceAdapter mAdapter;//设备布局适配器
	private ImageView mCamaro;
	private Context mContext;//定义上下文
	private Thread thread_open;//打开蓝牙的线程
	private List<BluetoothDevice> mDeviceList = new ArrayList<>();//查找到的设备
	private List<BluetoothDevice> mBondedDeviceList = new ArrayList<>();//绑定过的设备
	private View view;//缓存Fragment view

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(view==null){
			view = inflater.inflate(R.layout.activity_lanya_fragment, container, false);
		}
		//缓存的view需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个view已经有parent的错误。
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null) {
			parent.removeView(view);
		}

		//注册广播,Fragment中需要使用getActivity()获取到Activity对象
		IntentFilter filter = new IntentFilter();
		//开始查找
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		//结束查找
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		//查找设备
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		//设备扫描模式改变
		filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		//绑定状态
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

		getActivity().registerReceiver(receiver, filter);

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



    //定义一个广播
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action) ) {
                getActivity().setProgressBarIndeterminateVisibility(true);
                //初始化数据列表
                mDeviceList.clear();
                mAdapter.notifyDataSetChanged();
            }
            else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                getActivity().setProgressBarIndeterminateVisibility(false);
            }
            else if( BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //找到一个，添加一个
                mDeviceList.add(device);
                mAdapter.notifyDataSetChanged();
            }
            else if( BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,0);
                if( scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                	//顶部进度条
                    getActivity().setProgressBarIndeterminateVisibility(true);
					mText.setText("正在查找设备");
                }
                else {
                    getActivity().setProgressBarIndeterminateVisibility(false);
					mText.setText("查找到的设备");
                }
            }
            else if( BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action) ) {
                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if( remoteDevice == null ) {
                    ToastNoLooperUtil.showToast(getActivity(),"没有设备");
                    return;
                }
                int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,0);
                if( status == BluetoothDevice.BOND_BONDED) {
                    ToastNoLooperUtil.showToast(getActivity(),"已绑定"+remoteDevice.getName());
                    mAdapter.setName(remoteDevice,"已绑定");
					mAdapter.refresh(mDeviceList);
                }
                else if( status == BluetoothDevice.BOND_BONDING){
                    ToastNoLooperUtil.showToast(getActivity(),"绑定中"+remoteDevice.getName());
					mAdapter.setName(remoteDevice,"绑定中");
					mAdapter.refresh(mDeviceList);
                }
                else if(status == BluetoothDevice.BOND_NONE){
                    ToastNoLooperUtil.showToast(getActivity(),"未绑定"+remoteDevice.getName());
                    mAdapter.setName(remoteDevice,"未绑定");
                }
            }
        }
    };


	private void initListner(final Activity activity) {
		mSwitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Switch sw = (Switch)v;
				boolean isChecked = sw.isChecked();
				if(isChecked){
					open(activity);
				}else {
					close();
				}
			}
		});
	}

	/**
	 * 打开蓝牙式的功能
	 * @param activity
	 */
	public void open(final Activity activity){
		//将自己的设备设为可被查找状态
		mController.enableVisibly(mContext);
		//查找设备
		mText.setText("正在查找设备");
		mController.turnOnBlueTooth(activity,0);
		mAdapter.refresh(mDeviceList);
		mController.findDevice();
		mListView.setOnItemClickListener(bindDeviceClick);
	}

	/**
	 * 关闭蓝牙的功能
	 */
	public void close(){

		mController.closeFindDevice();
		mText.setText("绑定设备历史");
		mController.turnOffBlueTooth();
		mBondedDeviceList = mController.getBondedDeviceList();
		mAdapter.refresh(mBondedDeviceList);
		mListView.setOnItemClickListener(null);

	}

	private void initData() {
		mHead.setText("设备");
		//判断是否支持蓝牙
		boolean supportBlueTooth = mController.isSupportBlueTooth();
		if(supportBlueTooth==false) ToastNoLooperUtil.showToast(getActivity(),"该设备不支持蓝牙功能");
		else {
			//判断蓝牙状态，初始化滑动开关
			boolean blueToothStatus = mController.getBlueToothStatus();
			if (blueToothStatus == true) {
				mSwitch.setChecked(true);
				open(getActivity());
			} else {
				mSwitch.setChecked(false);
				close();
				mText.setText("绑定设备历史（第一次打开蓝牙后显示）");
			}
		}
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				if()
//			}
//		});
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(bindDeviceClick);
	}

	private void initView(View view) {
		
		mHead = (TextView) view.findViewById(R.id.head_name_in_lanya).findViewById(R.id.head_name);
		mCamaro = (ImageView) view.findViewById(R.id.head_name_in_lanya).findViewById(R.id.head_camaro);
		mSwitch = (Switch) view.findViewById(R.id.turn_blue_tooth);
		mText = (TextView) view.findViewById(R.id.lanya_text);
		mListView = (ListView) view.findViewById(R.id.device_list);
		mAdapter = new DeviceAdapter(mDeviceList, mContext);

		//
		View list_view = View.inflate(mContext, android.R.layout.simple_list_item_2, null);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			ToastNoLooperUtil.showToast(getActivity(),"打开成功");
		}else{
			ToastNoLooperUtil.showToast(getActivity(),"打开失败");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//销毁广播
		getActivity().unregisterReceiver(receiver);
	}

	/**
	 * 绑定的方法
	 */
	private AdapterView.OnItemClickListener bindDeviceClick = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
			BluetoothDevice device = mDeviceList.get(i);
			if(device.getBondState()==device.BOND_NONE){
				device.createBond();
			}else if(device.getBondState()==device.BOND_BONDED){
				device.createBond();
				ToastNoLooperUtil.showToast(getActivity(),"已绑定"+device.getName());
			}

		}
	};
}