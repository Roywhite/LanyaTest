package com.xiaobai.bookStore.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.xiaobai.bookStore.R;

public class EditPersonActivity extends AppCompatActivity {
    private TextView mEditName;//修改昵称
    private TextView mEditPasswd;//修改密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //继承activity时使用
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //继承AppCompatActivity时使用
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_editperson);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        mEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditPersonActivity.this,EditNameActivity.class);
                startActivity(intent);
            }
        });

        mEditPasswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditPersonActivity.this,EditPasswdActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initData() {

    }

    private void initView() {
        mEditName = findViewById(R.id.editPerson_name);
        mEditPasswd = findViewById(R.id.editPerson_passwd);
    }
}
