package com.xiaobai.bookStore.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.AesUtils;
import com.xiaobai.bookStore.util.GetSecUtil;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastNoLooperUtil;
import com.xiaobai.bookStore.util.ToastUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RentBookActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private Intent intent;
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();//与后台数据库做交互的服务类
    private SharedPreferences sp;
    private String bookNameFromIntent;//从前一个页面传来的书名
    private String bookNameByClick;//点击租借的书名
    private List<String> bookName = new ArrayList<>();//书名
    private List<String> locationName = new ArrayList<>();//书店名
    private List<String> booksLocation = new ArrayList<>();//货架
    private List<String> booksNum = new ArrayList<>();//存货
    private ListView listView;   //定义ListView对象，用来获取布局文件中的ListView控件
    private List<Map<String, Object>> list_map = new ArrayList<Map<String, Object>>(); //定义一个适配器对象

    private String responseGetRentBookSearchList;//搜索数据
    private Thread thread_getRentBookSearchList;//获取搜索数据的线程
    private boolean stopGetRentBookSearchListThread;//子线程销毁标记

    private String responseGetReceiverAddress;//收货地址
    private Thread thread_getReceiverAddress;//获取搜索数据的线程
    private boolean stopGetReceiverAddressThread;//子线程销毁标记

    private String responseUpdateReceiverAddress;//更新收货地址操作的状态
    private Thread thread_updateReceiverAddress;//获取数据的线程
    private boolean stopUpdateReceiverAddressThread;//子线程销毁标记

    private String responseRentBook;//借书操作的状态
    private Thread thread_RentBook;//借书的线程
    private boolean stopRentBookThread;//子线程销毁标记

    private Thread thread_createTwoWeiPic;//创建二维码线程
    private boolean stopCreateTwoWeiPic;

    private Runnable runCreateTwoWeiPic = new Runnable() {
        @Override
        public void run() {
            stopCreateTwoWeiPic = false;
            while (!stopCreateTwoWeiPic) {
                String sec = GetSecUtil.getSec(sendMessageToSql);
                if ("".equals(sec)) {
                    ToastNoLooperUtil.showToast(RentBookActivity.this, "连接服务器失败！");
                } else {
                    String account = sp.getString("name", "");
                    String aesPassword = sp.getString("passwd", "");
                    String bookStore = sp.getString("rentBookStore", "");
                    String text = account + "^" + aesPassword + "^" + bookNameByClick + "^" + bookStore;
                    //加密传输
                    String aesText = AesUtils.aesEncrypt(text, sec);
                    stopCreateTwoWeiPic = true;
                    Intent intent = new Intent(RentBookActivity.this, RentBookTwoWeiPicActivity.class);
                    intent.putExtra("aesText", aesText);
                    startActivity(intent);
                    finish();
                }
            }
        }
    };

    private Runnable runRentBook = new Runnable() {
        @Override
        public void run() {
            stopRentBookThread = false;
            while (!stopRentBookThread) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String sec = GetSecUtil.getSec(sendMessageToSql);
                    if ("".equals(sec)) {
                        ToastNoLooperUtil.showToast(RentBookActivity.this, "连接服务器失败！");
                    } else {
                        String account = sp.getString("name", "");
                        String aesPassword = sp.getString("passwd", "");
                        String bookStore = sp.getString("rentBookStore", "");
                        String text = account + "^" + aesPassword + "^" + bookNameByClick + "^" + bookStore;
                        //加密传输
                        String aesText = AesUtils.aesEncrypt(text, sec);
                        conn = sendMessageToSql.rentBook(aesText);
                        //判断有没有连接服务器
                        if (conn == null) {
                            ToastUtil.showToast(RentBookActivity.this, "连接服务器失败，请检查网络连接状况");
                        } else {
                            // 有的话就做自己的操作
                            is = conn.getInputStream();
                            responseRentBook = ResponseUtil.getResponse(is);
                            if ("true".equals(responseRentBook)) {
                                //租借成功，此时弹窗成功
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        successDialog();
                                    }
                                });
                            } else {
                                ToastUtil.showToast(RentBookActivity.this, "租借失败，请检查网络连接状况");
                            }
                            stopRentBookThread = true;
                        }
                    }
                    stopRentBookThread = true;
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

    private Runnable runUpdateReceiverAddress = new Runnable() {
        @Override
        public void run() {
            stopUpdateReceiverAddressThread = false;
            while (!stopUpdateReceiverAddressThread) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String account = sp.getString("name", "");
                    conn = sendMessageToSql.updateReceiverAddress(account, responseGetReceiverAddress);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(RentBookActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        responseUpdateReceiverAddress = ResponseUtil.getResponse(is);
                        if ("true".equals(responseUpdateReceiverAddress)) {
                            //更新成功，此时弹窗是否进行租借
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    rentBookDialog();
                                }
                            });
                        } else {
                            ToastUtil.showToast(RentBookActivity.this, "更新失败，请检查网络连接状况");
                        }
                        stopUpdateReceiverAddressThread = true;
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

    private Runnable runGetReceiverAddress = new Runnable() {
        @Override
        public void run() {
            stopGetReceiverAddressThread = false;
            while (!stopGetReceiverAddressThread) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String account = sp.getString("name", "");
                    conn = sendMessageToSql.getReceiverAddress(account);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(RentBookActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        responseGetReceiverAddress = ResponseUtil.getResponse(is);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!"".equals(responseGetReceiverAddress) && responseGetReceiverAddress != null) {
                                    //有地址,弹出地址
                                    showAddressDialog();
                                } else {
                                    //新地址
                                    setAddressDialog();
                                }
                            }
                        });
                        stopGetReceiverAddressThread = true;
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

    private Runnable runGetRentBookSearch = new Runnable() {
        @Override
        public void run() {
            stopGetRentBookSearchListThread = false;
            while (!stopGetRentBookSearchListThread) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String rentBookStore = sp.getString("rentBookStore", "");
                    conn = sendMessageToSql.getBookRentSearchList(bookNameFromIntent, rentBookStore);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(RentBookActivity.this, "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        responseGetRentBookSearchList = ResponseUtil.getResponse(is);
                        if (responseGetRentBookSearchList != null && !"".equals(responseGetRentBookSearchList)) {
                            String[] split = responseGetRentBookSearchList.split("\\^");
                            //1.准备好数据源
                            for (int i = 0; i < split.length; i = i + 4) {
                                bookName.add(split[i]);
                                locationName.add(split[i + 1]);
                                if ("NET".equals(split[i + 2])) {
                                    booksLocation.add("网络书架");
                                } else {
                                    booksLocation.add(split[i + 2]);
                                }
                                booksNum.add(split[i + 3]);
                            }
                            //循环为listView添加数据
                            for (int i = 0; i < locationName.size(); i++) {
                                Map<String, Object> items = new HashMap<String, Object>(); //创建一个键值对的Map集合，用来存放书名、店铺名、货架、剩余量
                                items.put("bookName", bookName.get(i));      //放入书名， 根据下标获取数组
                                items.put("locationName", locationName.get(i));  //放入店名， 根据下标获取数组
                                items.put("booksLocation", booksLocation.get(i));
                                items.put("booksNum", booksNum.get(i));
                                list_map.add(items);   //把这个存放好数据的Map集合放入到list中，这就完成类数据源的准备工作
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //2、创建适配器（可以使用外部类的方式、内部类方式等均可）
                                    SimpleAdapter simpleAdapter = new SimpleAdapter(RentBookActivity.this, list_map, R.layout.rent_book_list, new String[]{"bookName", "locationName", "booksLocation", "booksNum"}, new int[]{R.id.rent_book_name, R.id.rent_bookStore, R.id.rent_bookAddress, R.id.rent_bookNum});
                                    //3、为listView加入适配器
                                    listView.setAdapter(simpleAdapter);
                                    //设置点击事件
                                    listView.setOnItemClickListener(RentBookActivity.this);
                                    showNormalDialog();
                                }
                            });
                        } else {
                            ToastUtil.showToast(RentBookActivity.this, "无搜索结果！");
                        }
                        stopGetRentBookSearchListThread = true;
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
        setContentView(R.layout.activity_rentbook);
        initView();
        initData();
        //initListener();
    }

    private void initData() {
        intent = getIntent();
        sp = this.getSharedPreferences("sp_file", Context.MODE_MULTI_PROCESS);
        bookNameFromIntent = intent.getExtras().getString("bookName");
        thread_getRentBookSearchList = new Thread(runGetRentBookSearch);
        thread_getRentBookSearchList.start();
    }

    private void initView() {
        listView = findViewById(R.id.rent_lv_listView);
    }

    @Override
    protected void onDestroy() {
        stopGetRentBookSearchListThread = true;
        stopGetReceiverAddressThread = true;
        stopUpdateReceiverAddressThread = true;
        stopRentBookThread = true;
        stopCreateTwoWeiPic = true;
        super.onDestroy();
    }

    //点击返回键返回MainActivity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(RentBookActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 每一个item的点击事件
     * 先判断地址是否为空
     * 如果为空，弹出一个输入框输入地址（需要判空），两个按钮，确定和取消
     * 如果不为空，弹出地址，三个按钮（取消，确定，输入新地址）
     * 如果点击输入新地址，弹出输入框（需要判空），两个按钮，确定和返回上一步
     * 点确定后，弹出“是否租借”提示框，两个按钮（确定，取消）
     * 确定后直接生成订单，弹出“租借成功”，让返回主页
     *
     * @param adapterView
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        /*adapterView是指当前的listview；
         *view是当前listview中的item的view的布局,就是可用这个view获取里面控件id后操作控件
         * position是当前item在listview中适配器的位置
         * id是当前item在listview里第几行的位置
         */
        //获得选中项中的HashMap对象
        HashMap<String, String> map = (HashMap<String, String>) adapterView.getItemAtPosition(position);
        bookNameByClick = map.get("bookName");
        String booksNum = map.get("booksNum");
        if (Integer.parseInt(booksNum) == 0) {
            ToastNoLooperUtil.showToast(RentBookActivity.this, "该书籍数量不足！");
        } else {
            //先判断是线上店还是线下店，如果是线下店，直接生成二维码，开启一个线程一直请求后台，当请求上的时候弹窗“租借成功”，如果是线上店
            String rentBookStore = sp.getString("rentBookStore", "");
            if ("NET".equals(rentBookStore)) {
                //获取地址,线程里进行后续操作
                thread_getReceiverAddress = new Thread(runGetReceiverAddress);
                thread_getReceiverAddress.start();
            } else {
                //线下店
                rentBookUnWebDialog();
            }
        }
    }

    /**
     * 提示租借如何操作的提示框
     */
    private void showNormalDialog() {
        AlertDialog dialog = new AlertDialog.Builder(RentBookActivity.this).create();//创建对话框
        dialog.setTitle("提示");//设置对话框标题
        dialog.setMessage("点击书籍即可进行租借操作！");//设置文字显示内容
        dialog.show();//显示对话框
    }

    /**
     * 成功租借时弹出的提示框
     */
    private void successDialog() {
        AlertDialog dialog = new AlertDialog.Builder(RentBookActivity.this).create();//创建对话框
        dialog.setTitle("提示");//设置对话框标题
        dialog.setMessage("租借成功");//设置文字显示内容
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(RentBookActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        dialog.show();//显示对话框
    }

    //线下租借对话框
    private void rentBookUnWebDialog() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(RentBookActivity.this);
        normalDialog.setTitle("租借提示");
        normalDialog.setCancelable(false);
        normalDialog.setMessage("是否租借" + bookNameByClick);
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //生成二维码（account,password、bookName、bookStore）使用AES加密
                        thread_createTwoWeiPic = new Thread(runCreateTwoWeiPic);
                        thread_createTwoWeiPic.start();
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        normalDialog.show();
    }


    //租借对话框
    private void rentBookDialog() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(RentBookActivity.this);
        normalDialog.setTitle("租借提示");
        normalDialog.setCancelable(false);
        normalDialog.setMessage("是否租借" + bookNameByClick);
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //租借的线程
                        thread_RentBook = new Thread(runRentBook);
                        thread_RentBook.start();
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        normalDialog.show();
    }


    //显示地址的提示框
    private void showAddressDialog() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(RentBookActivity.this);
        normalDialog.setTitle("租借提示");
        normalDialog.setCancelable(false);
        normalDialog.setMessage("您当前的收货地址是:" + responseGetReceiverAddress + "，是否启用该地址？");
        normalDialog.setPositiveButton("新地址",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //新地址框
                        setNewAddressDialog("");
                    }
                });
        normalDialog.setNegativeButton("启用", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rentBookDialog();
            }
        });
        normalDialog.setNeutralButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        normalDialog.show();
    }

    /**
     * 当有地址时调用,创建新地址
     */
    private void setNewAddressDialog(String text) {
        /*@setView 装入一个EditView
         */
        final EditText editText = new EditText(RentBookActivity.this);
        editText.setText(text);
        final AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(RentBookActivity.this);
        inputDialog.setCancelable(false);
        inputDialog.setTitle("租借提示：请输入新的收货地址").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        responseGetReceiverAddress = editText.getText().toString();
                        if ("".equals(responseGetReceiverAddress) || responseGetReceiverAddress == null) {
                            ToastNoLooperUtil.showToast(RentBookActivity.this, "您输入的地址为空！");
                        } else {
                            String[] split = responseGetReceiverAddress.split("\\^");
                            if(split.length>1){
                                setNewAddressDialog(responseGetReceiverAddress);
                                ToastNoLooperUtil.showToast(RentBookActivity.this,"不允许输入不被允许的特殊符号！");
                            }else {
                                //避免用户输入“1111^”这种
                                responseGetReceiverAddress = split[0];
                                //更新收货地址的线程
                                thread_updateReceiverAddress = new Thread(runUpdateReceiverAddress);
                                thread_updateReceiverAddress.start();
                            }
                        }

                    }
                });
        inputDialog.setNegativeButton("上一步", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showAddressDialog();
            }
        });
        inputDialog.show();
    }


    /**
     * 当没有地址时调用
     */
    private void setAddressDialog() {
        /*@setView 装入一个EditView
         */
        final EditText editText = new EditText(RentBookActivity.this);
        final AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(RentBookActivity.this);
        inputDialog.setCancelable(false);
        inputDialog.setTitle("租借提示：您当前无收货地址，请输入新的收货地址").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        responseGetReceiverAddress = editText.getText().toString();
                        if ("".equals(responseGetReceiverAddress) || responseGetReceiverAddress == null) {
                            ToastNoLooperUtil.showToast(RentBookActivity.this, "您输入的地址为空！");
                        } else {
                            //更新收货地址的线程
                            thread_updateReceiverAddress = new Thread(runUpdateReceiverAddress);
                            thread_updateReceiverAddress.start();
                        }
                    }
                });
        inputDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        inputDialog.show();
    }
}
