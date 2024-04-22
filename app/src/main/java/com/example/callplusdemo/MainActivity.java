package com.example.callplusdemo;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.Map;

import io.rong.imlib.IRongCoreCallback.ConnectCallback;
import io.rong.imlib.IRongCoreEnum.ConnectionErrorCode;
import io.rong.imlib.IRongCoreEnum.DatabaseOpenStatus;
import io.rong.push.PushEventListener;
import io.rong.push.PushType;
import io.rong.push.RongPushClient;
import io.rong.push.RongPushPlugin;
import io.rong.push.TokenBean;
import io.rong.push.notification.PushNotificationMessage;
import io.rong.push.notification.RongNotificationInterface;
import io.rong.push.pushconfig.PushConfig;


public class MainActivity extends Base {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAudioVideoPermission();
        RongPushPlugin.init(this);
//        FirebaseAnalytics.setAnalyticsCollectionEnabled(true);
        requestIgnoreBatteryOptimizations();
//        setPushEventListener();

    }

    public void mainClick(View view) {
        int id = view.getId();
        if (id == R.id.btnLoginUser1) {
            imLogin(USER_1_TOKEN);
        } else if (id == R.id.btnLoginUser2) {
            imLogin(USER_2_TOKEN);
        } else if (id == R.id.btnLoginUser4) {
            imLogin(USER_4_TOKEN);
        } else if (id == R.id.btnLoginUser5) {
            imLogin(USER_5_TOKEN);
        } else if (id == R.id.btnLoginUser6) {
            imLogin(USER_6_TOKEN);
        } else if (id == R.id.btnTest) {
            clickNotification(this);
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

    public static void clickNotification(Context context) {
        PushNotificationMessage message = new PushNotificationMessage();
        message.setPushId("CESN-Q7TU-37C0-I2TH");
        message.setConversationType(RongPushClient.ConversationType.PRIVATE);
        message.setReceivedTime(0);
        message.setObjectName("RC:VCInvite");
        message.setSenderId("10002");
        message.setSenderName("小丑");
        message.setSenderPortrait(null);
        message.setTargetId("10002");
        message.setTargetUserName("null");
        message.setToId("10000");
        message.setPushTitle("test");
        message.setPushContent("邀请您进行视频聊天");
        message.setPushData("");
        message.setExtra(null);
        message.setPushFlag("null");
        message.setPid("null");
        message.setSourceType(PushNotificationMessage.PushSourceType.FROM_OFFLINE_MESSAGE);
        message.setCollapseKeyFCM("null");
        message.setImageUrlFCM("");
        message.setChannelIdFCM("null");
        message.setImageUrlHW("null");
        message.setChannelIdHW("null");
        message.setImageUrlMi("null");
        message.setChannelIdMi("null");
        message.setChannelIdOPPO("null");
        message.setBusChannel("");
        message.setVoip(1);
        message.setNotificationId("null");
        message.setIntent("null");
        message.setShowDetail(true);
        RongNotificationInterface.sendNotification(context,message);
    }

    private void imLogin(String token) {
        connectIM(token, new ConnectCallback() {
            @Override
            public void onSuccess(String t) {
                showToast("IM登录成功，UserId: " + t);
                SessionManager.getInstance().put(CURRENT_USER_TOKEN_KEY, token);
                Intent intent = new Intent(MainActivity.this, CallPlusActivity.class);
                intent.putExtra(FROM_SYSTEM_CONTACTS_KEY, false);
                startActivity(intent);
            }

            @Override
            public void onError(ConnectionErrorCode e) {
                showToast("IM登录失败，ErrorCode: " + e.name());
            }

            @Override
            public void onDatabaseOpened(DatabaseOpenStatus code) {

            }
        });
    }

    public void setPushEventListener() {
        RongPushClient.setPushEventListener(
                new PushEventListener() {
                    @Override
                    public boolean preNotificationMessageArrived(Context context, PushType pushType, PushNotificationMessage notificationMessage) {
                        // 该回调仅在通知类型为透传消息时生效。返回 true 表示拦截，false 为不拦截
                        RongNotification.sendNotification(MainActivity.this, notificationMessage);
//                        RongNotificationInterface.sendNotification(MainActivity.this, notificationMessage);
//                        return false;
                        return true;
                    }


                    @Override
                    public void afterNotificationMessageArrived(Context context, PushType pushType, PushNotificationMessage notificationMessage) {
                        // 该回调仅在通知类型为透传消息时生效
                    }

                    @Override
                    public boolean onNotificationMessageClicked(Context context, PushType pushType, PushNotificationMessage notificationMessage) {
                        // 用户可以在此定义自己的通知的点击事件业务，返回 true 表示拦截，false 为不拦截
                        return false;
                    }

                    @Override
                    public void onThirdPartyPushState(PushType pushType, String action, long resultCode) {
                    }

                    @Override
                    public void onTokenReceived(PushType pushType, String token) {

                    }

                    @Override
                    public void onTokenReportResult(PushType reportType, int code, PushType finalType, String finalToken) {

                    }

                    @Override
                    public void onTokenReportResult(PushType reportType, int code, PushType finalType, String finalToken, String message, Map<String, String> failMap) {
                        PushEventListener.super.onTokenReportResult(reportType, code, finalType, finalToken, message, failMap);
                    }

                    @Override
                    public void onTokenReportResult(PushType reportType, int code, PushType finalType, String finalToken, String message) {
                        PushEventListener.super.onTokenReportResult(reportType, code, finalType, finalToken, message);
                    }

                    @Override
                    public void onStartRegister(List<PushType> preferTypes) {
                        PushEventListener.super.onStartRegister(preferTypes);
                    }

                    @Override
                    public void onPreferPushType(List<PushType> preferType) {
                        PushEventListener.super.onPreferPushType(preferType);
                    }

                    @Override
                    public void onGetTokenServer(boolean isSuccess) {
                        PushEventListener.super.onGetTokenServer(isSuccess);
                    }

                    @Override
                    public void onPushConfigInitFinish(PushConfig.Builder pushConfig) {
                        PushEventListener.super.onPushConfigInitFinish(pushConfig);
                    }

                    @Override
                    public void onGetTokenError(PushType pushType, long code, String errorMessage) {
                        PushEventListener.super.onGetTokenError(pushType, code, errorMessage);
                    }

                    @Override
                    public List<TokenBean> onStartTokenReport(List<TokenBean> tokenList) {
                        return PushEventListener.super.onStartTokenReport(tokenList);
                    }

                });
    }

    private int REQUEST_CODE = 20000;
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestIgnoreBatteryOptimizations() {
        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE);
    }
}