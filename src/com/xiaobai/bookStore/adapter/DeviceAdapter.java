package com.xiaobai.bookStore.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * 设备适配器
 */
public class DeviceAdapter extends BaseAdapter {

    private List<BluetoothDevice> mData;
    private Context mContext;
    private TextView line1;
    private TextView line2;
    private String rssi = null;
    private String address=null;
    private SharedPreferences sp;;

    public DeviceAdapter(List<BluetoothDevice> data, Context context) {
        mData = data;
        mContext = context.getApplicationContext();
        sp =  mContext.getSharedPreferences("lanya_device", Context.MODE_MULTI_PROCESS);
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View itemView = view;
        //复用View，优化性能
        if( itemView == null) {
            itemView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2,viewGroup,false);
        }

        line1 = (TextView) itemView.findViewById(android.R.id.text1);
        line2 = (TextView) itemView.findViewById(android.R.id.text2);
        line1.setTextColor(Color.BLACK);
        line2.setTextColor(Color.BLACK);

        //获取对应的蓝牙设备
        BluetoothDevice device = (BluetoothDevice) getItem(i);

        if(device.getName()==null){
            line1.setText("NULL NAME"+" (未绑定)");
        }else if(device.getBondState()==device.BOND_NONE ){
            //显示名称
            line1.setText(device.getName()+" (未绑定)");
        }else if(device.getBondState()==device.BOND_BONDED){
            String rssi = sp.getString(device.getAddress(), null);
            if(rssi!=null&&Double.parseDouble(rssi)<=10){
                line1.setText(device.getName()+" (已连接 距离:"+rssi+"m)");
            }else if(rssi!=null&&Double.parseDouble(rssi)>10){
                line1.setText(device.getName()+" (已绑定 丢失设备)");
            }else if(rssi==null){
                line1.setText(device.getName() + " (已绑定)");
            }
        }else if(device.getBondState()==device.BOND_BONDING){
            line1.setText(device.getName()+" (正在绑定)");
        }
        //显示地址
        line2.setText(device.getAddress());

        return itemView;
    }

    public void refresh(List<BluetoothDevice> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public  void setName(BluetoothDevice device,String name){
        line1.setText(device.getName()+" ("+name+")");
    }

    public void setRssi(String address,String rssi){
        this.address = address;
        this.rssi = rssi;
    }

}
