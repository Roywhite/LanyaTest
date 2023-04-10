package com.xiaobai.bookStore.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ImageView;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastUtil;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.io.InputStream;
import java.net.HttpURLConnection;

public class RentBookTwoWeiPicActivity extends AppCompatActivity {
    private ImageView mTwoWeiPic;
    private Intent intent;
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();//与后台数据库做交互的服务类
    private SharedPreferences sp;

    private String boolRent;
    private boolean stopBoolRent;
    private Thread threadBoolRent;

    private Runnable runBoolRent = new Runnable() {
        @Override
        public void run() {
            stopBoolRent = false;
            int num = 0;
            while(!stopBoolRent){
                HttpURLConnection connBoolRent = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    String aesPassword = sp.getString("passwd", "");
                    connBoolRent = sendMessageToSql.getBooleanRent(name, aesPassword);
                    //判断有没有连接服务器
                    if (connBoolRent == null) {
                        ToastUtil.showToast(RentBookTwoWeiPicActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        is = connBoolRent.getInputStream();
                        boolRent = ResponseUtil.getResponse(is);
                        //false无租借，true有租借
                        if("true".equals(boolRent)){
                            Looper.prepare();
                            successDialog();
                            stopBoolRent = true;
                            Looper.loop();
                        }else{
                            num = num+2;
                            //时间控制
                            if(num==62){
                                stopBoolRent = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        outTimeDialog();
                                    }
                                });
                            }
                            Thread.sleep(2000);
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                } finally {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //继承activity时使用
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //继承AppCompatActivity时使用
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_rentbook_twoweipic);
        initView();
        initData();
    }

    private void initData() {
        intent = getIntent();
        String aesText = intent.getExtras().getString("aesText");
        sp = this.getSharedPreferences("sp_file", Context.MODE_MULTI_PROCESS);
        Bitmap bitmap = CodeCreator.createQRCode(aesText, 400, 400, null);
        if (bitmap != null) {
            mTwoWeiPic.setImageBitmap(bitmap);
            threadBoolRent = new Thread(runBoolRent);
            threadBoolRent.start();
        }
    }

    private void initView() {
        mTwoWeiPic = findViewById(R.id.rent_iv_twopic);
    }

    /**
     * 成功租借时弹出的提示框
     */
    private void successDialog() {
        AlertDialog dialog = new AlertDialog.Builder(RentBookTwoWeiPicActivity.this).create();//创建对话框
        dialog.setTitle("提示");//设置对话框标题
        dialog.setMessage("租借成功");//设置文字显示内容
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(RentBookTwoWeiPicActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        dialog.show();//显示对话框
    }

    /**
     * 超时提示框
     */
    private void outTimeDialog() {
        AlertDialog dialog = new AlertDialog.Builder(RentBookTwoWeiPicActivity.this).create();//创建对话框
        dialog.setTitle("提示");//设置对话框标题
        dialog.setMessage("二维码验证超时，请重新进行租借生成！");//设置文字显示内容
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(RentBookTwoWeiPicActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        dialog.show();//显示对话框
    }

    //点击返回键返回MainActivity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(RentBookTwoWeiPicActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
