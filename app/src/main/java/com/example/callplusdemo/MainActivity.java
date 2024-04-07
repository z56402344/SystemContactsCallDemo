package com.example.callplusdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import io.rong.imlib.IRongCoreCallback.ConnectCallback;
import io.rong.imlib.IRongCoreEnum.ConnectionErrorCode;
import io.rong.imlib.IRongCoreEnum.DatabaseOpenStatus;


public class MainActivity extends Base {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAudioVideoPermission();
    }

    public void mainClick(View view) {
        int id = view.getId();
        if (id == R.id.btnLoginUser1) {
            imLogin(USER_1_TOKEN);
        } else if (id == R.id.btnLoginUser2) {
            imLogin(USER_2_TOKEN);
        } else if (id == R.id.btnInsert) {
            SystemContactsManger.getInstance().addAccount(MainActivity.this);
            SystemContactsManger.getInstance().clearAll(MainActivity.this.getApplicationContext());

            String remoteUseName = "王五";
            String remoteUserPhone = "13900000000";
            String userId = "13900000000";
            SystemContactsManger.getInstance()
                .addContact(MainActivity.this.getApplicationContext(), remoteUseName, remoteUserPhone, userId);
            showToast("添加数据成功");
        }
    }

    private void imLogin(String token) {
        connectIM(token, new ConnectCallback() {
            @Override
            public void onSuccess(String t) {
                showToast("IM登录成功，UserId: "+t);
                SessionManager.getInstance().put(CURRENT_USER_TOKEN_KEY, token);
                Intent intent = new Intent(MainActivity.this, CallPlusActivity.class);
                intent.putExtra(FROM_SYSTEM_CONTACTS_KEY, false);
                startActivity(intent);
            }

            @Override
            public void onError(ConnectionErrorCode e) {
                showToast("IM登录失败，ErrorCode: "+ e.name());
            }

            @Override
            public void onDatabaseOpened(DatabaseOpenStatus code) {

            }
        });
    }
}