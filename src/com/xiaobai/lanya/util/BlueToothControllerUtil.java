package com.xiaobai.lanya.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙适配器工具类
 */
public class BlueToothControllerUtil {
    private BluetoothAdapter mAdapter;

    public BlueToothControllerUtil(){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    /**
     * 是否支持蓝牙
     * @return true 支持，false  不支持
     */
    public boolean isSupportBlueTooth(){
        if(mAdapter != null){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 判断当前蓝牙状态
     * @return true 打开，false  关闭
     */
    public boolean getBlueToothStatus(){
        assert (mAdapter!=null);
        return mAdapter.isEnabled();
    }

    /**
     * 打开蓝牙
     * @param activity
     * @param requestCode
     */
    public void turnOnBlueTooth(Activity activity,int requestCode){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent,requestCode);
        //mAdapter.enable();
    }

    /**
     * 开启蓝牙（无提示框）
     */
    public void getDevies(){
        mAdapter.enable();
    }

    /**
     * 关闭蓝牙
     */
    public void turnOffBlueTooth() {
        mAdapter.disable();
    }

    /**
     * 打开蓝牙可见性
     * @param context
     */
    public void enableVisibly(Context context){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        context.startActivity(discoverableIntent);
    }

    /**
     * 查找设备
     */
    public void findDevice(){
        assert (mAdapter!=null);
        mAdapter.startDiscovery();
    }

    /**
     * 关闭查找功能
     */
    public void closeFindDevice(){
        assert (mAdapter!=null);
        mAdapter.cancelDiscovery();
    }

    /**
     * 获取绑定设备
     * @return
     */
    public List<BluetoothDevice> getBondedDeviceList(){
        return new ArrayList<>(mAdapter.getBondedDevices());
    }


}
