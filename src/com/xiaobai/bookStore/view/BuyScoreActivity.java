package com.xiaobai.bookStore.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;

public class BuyScoreActivity extends AppCompatActivity {

    private TextView mMessage;
    private Button lessMoney;
    private Button moreMoney;
    private Button skipButton;
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();
    private String aesPassword;
    private SharedPreferences sp;// 保存数据:键值对
    private String score;
    private Thread threadGetScore;
    private boolean boolGetScore;
    private Double less;
    private Double more;
    private Thread threadBuyScore;
    private boolean boolBuyLessScore;
    private boolean boolBuyMoreScore;
    private String result;//购买结果

    private Runnable runBuyMoreScoreThread = new Runnable() {
        @Override
        public void run() {
            boolBuyMoreScore = false;
            while(!boolBuyMoreScore){
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    aesPassword = sp.getString("passwd", "");
                    conn = sendMessageToSql.updateScore(name,aesPassword,"100");
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(BuyScoreActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        // 获取服务器传来的分数
                        is = conn.getInputStream();
                        result = ResponseUtil.getResponse(is);
                        if("true".equals(result)){
                            Intent intent = new Intent(BuyScoreActivity.this,MainActivity.class);
                            startActivity(intent);
                            ToastUtil.showToast(BuyScoreActivity.this,"充值成功！");
                        }else{
                            ToastUtil.showToast(BuyScoreActivity.this,"充值失败，请检查网络！");
                        }
                        boolBuyMoreScore = true;
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

    private Runnable runBuyLessScoreThread = new Runnable() {
        @Override
        public void run() {
            boolBuyLessScore = false;
            while(!boolBuyLessScore){
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    aesPassword = sp.getString("passwd", "");
                    conn = sendMessageToSql.updateScore(name,aesPassword,"90");
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(BuyScoreActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        // 获取服务器传来的分数
                        is = conn.getInputStream();
                        result = ResponseUtil.getResponse(is);
                        if("true".equals(result)){
                            Intent intent = new Intent(BuyScoreActivity.this,MainActivity.class);
                            startActivity(intent);
                            ToastUtil.showToast(BuyScoreActivity.this,"充值成功！");
                        }else{
                            ToastUtil.showToast(BuyScoreActivity.this,"充值失败，请检查网络！");
                        }
                        boolBuyLessScore = true;
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

    private Runnable runGetScoreFirstCreate = new Runnable() {
        @Override
        public void run() {
            boolGetScore = false;
            while(!boolGetScore){
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    aesPassword = sp.getString("passwd", "");
                    conn = sendMessageToSql.getScores(name, aesPassword);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(BuyScoreActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        // 获取服务器传来的分数
                        is = conn.getInputStream();
                        score = ResponseUtil.getResponse(is);

                        runOnUiThread(new Runnable() {//使用runOnUIThread()方法更新主线程
                            @Override
                            public void run() {
                                if(Integer.parseInt(score)<90) {
                                    less = (90 - Integer.parseInt(score)) / 2.0;
                                    BigDecimal bgLess = new BigDecimal(less).setScale(2, RoundingMode.UP);
                                    more = (100 - Integer.parseInt(score)) / 2.0;
                                    BigDecimal bgMore = new BigDecimal(more).setScale(2, RoundingMode.UP);
                                    mMessage.setText("尊敬的用户，您当前信用分为" + score + "分，为了能更好的服务您，需要充值后才能进行正常的租借业务，90分以上能进行正常的租借业务（2分/元），您当前最少充值" + bgLess.doubleValue() + "元，最多充值" + bgMore.doubleValue() + "元，还请亲在租借的过程中，注意好还书时间，具体扣分细则请见“自助服务”!");
                                    lessMoney.setText("￥"+bgLess.doubleValue());
                                    moreMoney.setText("￥"+bgMore.doubleValue());
                                    skipButton.setText("返回");
                                } else{
                                    lessMoney.setText("");
                                    moreMoney.setText("");
                                    mMessage.setText("尊敬的用户，您当前信用良好，暂不需要进行充值！");
                                    lessMoney.setClickable(false);
                                    lessMoney.setBackgroundColor(0);
                                    moreMoney.setClickable(false);
                                    moreMoney.setBackgroundColor(0);
                                    skipButton.setText("返回");
                                }
                            }
                        });
                        boolGetScore = true;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //继承activity时使用
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //继承AppCompatActivity时使用
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_buyscore);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        lessMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threadBuyScore = new Thread(runBuyLessScoreThread);
                threadBuyScore.start();
            }
        });

        moreMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threadBuyScore = new Thread(runBuyMoreScoreThread);
                threadBuyScore.start();
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BuyScoreActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        sp = this.getSharedPreferences("sp_file", Context.MODE_MULTI_PROCESS);
        threadGetScore = new Thread(runGetScoreFirstCreate);
        threadGetScore.start();
    }

    private void initView() {
        mMessage = findViewById(R.id.buyscore_tv_msg);
        lessMoney = findViewById(R.id.buyscore_bt_lessmoney);
        moreMoney = findViewById(R.id.buyscore_bt_moremoney);
        skipButton = findViewById(R.id.buyscore_skip);
    }

    @Override
    protected void onDestroy() {
        boolGetScore = true;
        boolBuyMoreScore = true;
        boolBuyLessScore = true;
        super.onDestroy();
    }
}
