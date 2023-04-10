package com.xiaobai.bookStore.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.xiaobai.bookStore.R;
import com.xiaobai.bookStore.service.SendMessageToSql;
import com.xiaobai.bookStore.util.AesUtils;
import com.xiaobai.bookStore.util.GetSecUtil;
import com.xiaobai.bookStore.util.ResponseUtil;
import com.xiaobai.bookStore.util.ToastNoLooperUtil;
import com.xiaobai.bookStore.util.ToastUtil;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.io.InputStream;
import java.net.HttpURLConnection;

public class ReturnBookFragment extends Fragment {

    private TextView mHead;
    private ImageView mCamaro;
    private ImageView mTwoPic;
    private String aesPassword;
    private SendMessageToSql sendMessageToSql = new SendMessageToSql();
    private SharedPreferences sp;// 保存数据:键值对
    private String aesText;//加密文本

    private String response;//有没有租借
    private Thread thread_getBooleanRent;//获取租借状态的线程，false为未租借，true表示还有书籍未还
    private boolean stopGetBooleanRentThread;//子线程销毁标记

    private String bookName;//书籍名
    private Thread thread_getBookName;
    private boolean stopGetBookNameThread;//子线程销毁标记

    private String residueDegree;//剩余续租次数
    private Thread thread_getResidueDegree;//获取剩余续租次数的线程
    private boolean stopGetResidueDegree;//子线程销毁标记

    private Long restDay;//剩余天数
    private Thread thread_getRestDay;
    private boolean stopGetRestDayThread;//子线程销毁标记

    private String booleanUpdateResidueDay;//续租
    private Thread thread_updateResidueDay;
    private boolean stopUpdateResidueDayThread;//子线程销毁标记

    private Thread thread_createTwoWeiPic;//创建二维码线程
    private boolean stopCreateTwoWeiPic;

    private String boolRent;//判断是否归还
    private boolean stopBoolRent;
    private Thread threadBoolRent;

    private String boolUpdateBookScore;//判断是否评分成功
    private boolean stopUpdateBookScore;
    private Thread threadUpdateBookScore;
    private String scoreByClick;//评分栏获取

    private Runnable runUpdateBookScore = new Runnable() {
        @Override
        public void run() {
            stopUpdateBookScore = false;
            while (!stopUpdateBookScore) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    conn = sendMessageToSql.updateBookScore(bookName, scoreByClick);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(getActivity(), "连接服务器失败，请检查网络连接状况");
                    } else {
                        is = conn.getInputStream();
                        boolUpdateBookScore = ResponseUtil.getResponse(is);
                        if ("true".equals(boolUpdateBookScore)) {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                            ToastUtil.showToast(getActivity(), "评分成功");
                        } else {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                            ToastUtil.showToast(getActivity(), "评分失败");
                        }
                    }
                    stopUpdateBookScore = true;
                    Thread.sleep(120000);
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

    private Runnable runBoolRent = new Runnable() {
        @Override
        public void run() {
            stopBoolRent = false;
            int num = 0;
            while (!stopBoolRent) {
                HttpURLConnection connBoolRent = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    String aesPassword = sp.getString("passwd", "");
                    connBoolRent = sendMessageToSql.getBooleanRent(name, aesPassword);
                    //判断有没有连接服务器
                    if (connBoolRent == null) {
                        ToastUtil.showToast(getActivity(), "连接服务器失败，请检查网络连接状况");
                    } else {
                        is = connBoolRent.getInputStream();
                        boolRent = ResponseUtil.getResponse(is);
                        //false无租借，true有租借
                        if ("false".equals(boolRent)) {
                            Looper.prepare();
                            successReturnDialog();
                            stopBoolRent = true;
                            Looper.loop();
                        } else {
                            num = num + 2;
                            //时间控制
                            if (num == 62) {
                                stopBoolRent = true;
                                stopCreateTwoWeiPic = true;
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        outTimeDialog();
                                    }
                                });
                            }
                            Thread.sleep(2000);
                        }
                    }
                } catch (Exception e) {
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

    private Runnable runCreateTwoWeiPic = new Runnable() {
        @Override
        public void run() {
            stopCreateTwoWeiPic = false;
            while (!stopCreateTwoWeiPic) {
                String sec = GetSecUtil.getSec(sendMessageToSql);
                if ("".equals(sec)) {
                    ToastNoLooperUtil.showToast(getActivity(), "连接服务器失败！");
                } else {
                    try {
                        String account = sp.getString("name", "");
                        final String aesPassword = sp.getString("passwd", "");
                        String text = account + "^" + aesPassword + "^" + bookName + "^" + "补位" + "^" + "returnBook";
                        //加密传输
                        aesText = AesUtils.aesEncrypt(text, sec);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap = CodeCreator.createQRCode(aesText, 400, 400, null);
                                if (bitmap != null) {
                                    mTwoPic.setImageBitmap(bitmap);
                                }
                            }
                        });
                        threadBoolRent = new Thread(runBoolRent);
                        threadBoolRent.start();
                        Thread.sleep(60000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private Runnable runUpdateResidueDay = new Runnable() {
        @Override
        public void run() {
            stopUpdateResidueDayThread = false;
            while (!stopUpdateResidueDayThread) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    conn = sendMessageToSql.updateResidueDay(name);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(getActivity(), "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        is = conn.getInputStream();
                        booleanUpdateResidueDay = ResponseUtil.getResponse(is);
                        stopUpdateResidueDayThread = true;
                        //续租操作
                        if ("true".equals(booleanUpdateResidueDay)) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    successDialog();
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    falseDialog();
                                }
                            });
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

    //获取剩余天数
    private Runnable runGetRestDay = new Runnable() {
        @Override
        public void run() {
            stopGetRestDayThread = false;
            while (!stopGetRestDayThread) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    conn = sendMessageToSql.getRestDay(name);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(getActivity(), "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        // 获取剩余天数
                        is = conn.getInputStream();
                        restDay = Long.parseLong(ResponseUtil.getResponse(is));
                        stopGetRestDayThread = true;
                        //如果剩余天数小于0，则不可进行续租
                        if (restDay < 0) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myTrueDialogLowZero();
                                }
                            });
                        } else {
                            //判断租借次数，如果租借次数为0，不显示租借按钮，如果为1，显示
                            thread_getResidueDegree = new Thread(runGetResidueDegree);
                            thread_getResidueDegree.start();
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

    //获取续租次数
    private Runnable runGetResidueDegree = new Runnable() {
        @Override
        public void run() {
            stopGetResidueDegree = false;
            while (!stopGetResidueDegree) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    aesPassword = sp.getString("passwd", "");
                    conn = sendMessageToSql.getResidueDegree(name, aesPassword);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(getActivity(), "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        // 获取剩余续租次数
                        is = conn.getInputStream();
                        residueDegree = ResponseUtil.getResponse(is);
                        stopGetResidueDegree = true;
                        //获取续租次数,为0时调用直接归还的窗口，为1是显示出续租按钮
                        if (Integer.parseInt(residueDegree) == 1) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myTrueDialogBigZeroAndNOne();
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myTrueDialogBigZeroAndNZero();
                                }
                            });
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
    //获取书名
    private Runnable runGetBookName = new Runnable() {
        @Override
        public void run() {
            stopGetBookNameThread = false;
            while (!stopGetBookNameThread) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    conn = sendMessageToSql.getBookName(name);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(getActivity(), "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        // 获取书籍名
                        is = conn.getInputStream();
                        bookName = ResponseUtil.getResponse(is);
                        stopGetBookNameThread = true;
                        //判断剩余租借天数，如果小于0，不显示续租按钮，大于等于0继续
                        thread_getRestDay = new Thread(runGetRestDay);
                        thread_getRestDay.start();
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
    private Runnable runGetBooleanRent = new Runnable() {
        @Override
        public void run() {
            stopGetBooleanRentThread = false;
            while (!stopGetBooleanRentThread) {
                HttpURLConnection conn = null;
                InputStream is = null;
                try {
                    String name = sp.getString("name", "");
                    aesPassword = sp.getString("passwd", "");
                    conn = sendMessageToSql.getBooleanRent(name, aesPassword);
                    //判断有没有连接服务器
                    if (conn == null) {
                        ToastUtil.showToast(getActivity(), "连接服务器失败，请检查网络连接状况");
                    } else {
                        // 有的话就做自己的操作
                        // 获取是否还在租借
                        is = conn.getInputStream();
                        response = ResponseUtil.getResponse(is);
                        stopGetBooleanRentThread = true;

                        /**
                         * 逻辑梳理：
                         * 先获取有没有租借中的书籍，没有则跳转到主页，有，继续下一步
                         * 获取书籍名，剩余天数
                         * 如果天数<0，弹窗为“尊敬的用户，您当前租借的书籍:《xxx》尚未归还，已经超过租借最大期限，归还时将扣取相应信用分，具体扣分细则请查看自助服务！”，只显示归还和取消按钮
                         * 如果天数>0，继续下面的操作，查询剩余租借次数
                         * 如果租借次数=1，弹窗为“尊敬的用户，您当前租借的书籍:《xxx》尚未归还，尚可续租1次，续租or归还？”，显示取消、续租、归还
                         * 如果租借次数=0，弹窗为“尊敬的用户，您当前租借的书籍:《xxx》尚未归还，已达最大续租次数！”，显示取消、归还
                         */
                        //判断有没有租借，如果有，进行归还/续租操作
                        if ("true".equals(response)) {
                            //先获取书籍名+剩余时间+剩余续租次数
                            thread_getBookName = new Thread(runGetBookName);
                            thread_getBookName.start();
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTwoPic.setImageResource(R.drawable.twoweima);
                                    ToastNoLooperUtil.showToast(getActivity(), "当前未租借书籍");
                                }
                            });
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
        View view = inflater.inflate(R.layout.activity_returnbook_fragment, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initData() {
        mHead.setText("归还/续租");
        mCamaro.setImageResource(0);
        sp = this.getActivity().getSharedPreferences("sp_file", Context.MODE_MULTI_PROCESS);
        thread_getBooleanRent = new Thread(runGetBooleanRent);
        thread_getBooleanRent.start();
    }

    private void setBookScoreDialog() {
        final AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(getActivity());
        final View dialogView = LayoutInflater.from(getActivity())
                .inflate(R.layout.set_book_score, null);
        customizeDialog.setTitle("给书评个分数吧！高分书籍将进入推荐榜哟！");
        customizeDialog.setView(dialogView);
        final AlertDialog dia = customizeDialog.show();
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                rating = rating * 2;
                scoreByClick = String.valueOf(rating);
                threadUpdateBookScore = new Thread(runUpdateBookScore);
                threadUpdateBookScore.start();
                dia.dismiss();
            }
        });
        dia.show();
    }

    /**
     * 超时提示框
     */
    private void outTimeDialog() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();//创建对话框
        dialog.setTitle("提示");//设置对话框标题
        dialog.setMessage("二维码验证超时，请重新进行归还生成！");//设置文字显示内容
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });
        dialog.show();//显示对话框
    }

    /**
     * 成功归还时弹出的提示框
     */
    private void successReturnDialog() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();//创建对话框
        dialog.setTitle("提示");//设置对话框标题
        dialog.setMessage("归还成功");//设置文字显示内容
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setBookScoreDialog();
            }
        });
        dialog.show();//显示对话框
    }

    public void successDialog() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();//创建对话框
        dialog.setTitle("提示");//设置对话框标题
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage("尊敬的用户，您已续租成功！");//设置文字显示内容
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTwoPic.setImageResource(R.drawable.twoweima);
                dialog.dismiss();//关闭对话框
            }
        });
        dialog.show();//显示对话框
    }

    public void falseDialog() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();//创建对话框
        dialog.setTitle("提示");//设置对话框标题
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage("尊敬的用户，发生异常续租失败！");//设置文字显示内容
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTwoPic.setImageResource(R.drawable.twoweima);
                dialog.dismiss();//关闭对话框
            }
        });
        dialog.show();//显示对话框
    }

    //租借书时租借剩余天数大于0并且续租次数为0的对话框
    public void myTrueDialogBigZeroAndNZero() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();//创建对话框
        dialog.setTitle("提示");//设置对话框标题
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage("尊敬的用户，您当前租借的书籍:" + bookName + "尚未归还，还有" + restDay + "天到期，已达最大续租次数！");//设置文字显示内容
        //分别设置2个button
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "归还", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thread_createTwoWeiPic = new Thread(runCreateTwoWeiPic);
                thread_createTwoWeiPic.start();
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTwoPic.setImageResource(R.drawable.twoweima);
                dialog.dismiss();//关闭对话框
            }
        });
        dialog.show();//显示对话框
    }

    //租借书时租借剩余天数大于0且续租次数为1的对话框
    public void myTrueDialogBigZeroAndNOne() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();//创建对话框
        dialog.setTitle("提示");//设置对话框标题
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage("尊敬的用户，您当前租借的书籍:" + bookName + "尚未归还，还有" + restDay + "天到期，尚可续租1次，续租or归还？");//设置文字显示内容
        //分别设置2个button
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "归还", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thread_createTwoWeiPic = new Thread(runCreateTwoWeiPic);
                thread_createTwoWeiPic.start();
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "续租", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thread_updateResidueDay = new Thread(runUpdateResidueDay);
                thread_updateResidueDay.start();
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTwoPic.setImageResource(R.drawable.twoweima);
                dialog.dismiss();//关闭对话框
            }
        });
        dialog.show();//显示对话框
    }

    //租借书时租借剩余天数小于0的对话框
    public void myTrueDialogLowZero() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();//创建对话框
        dialog.setTitle("提示");//设置对话框标题
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage("尊敬的用户，您当前租借的书籍:" + bookName + "尚未归还，已经超过租借最大期限，归还时将扣取相应信用分，具体扣分细则请查看自助服务！");//设置文字显示内容
        //分别设置2个button
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "归还", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thread_createTwoWeiPic = new Thread(runCreateTwoWeiPic);
                thread_createTwoWeiPic.start();
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTwoPic.setImageResource(R.drawable.twoweima);
                dialog.dismiss();//关闭对话框
            }
        });
        dialog.show();//显示对话框
    }

    private void initView(View view) {
        mHead = (TextView) view.findViewById(R.id.head_name_in_gonggao).findViewById(R.id.head_name);
        mCamaro = (ImageView) view.findViewById(R.id.head_name_in_gonggao).findViewById(R.id.head_camaro);
        mTwoPic = view.findViewById(R.id.returnbook_iv_twopic);
    }

    @Override
    public void onDestroy() {
        stopGetBooleanRentThread = true;
        stopGetBookNameThread = true;
        stopGetResidueDegree = true;
        stopGetRestDayThread = true;
        stopUpdateResidueDayThread = true;
        stopCreateTwoWeiPic = true;
        stopBoolRent = true;
        stopUpdateBookScore = true;
        super.onDestroy();
    }
}
