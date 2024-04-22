package com.example.callplusdemo;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Map;

import io.rong.push.PushEventListener;
import io.rong.push.PushType;
import io.rong.push.RongPushClient;
import io.rong.push.TokenBean;
import io.rong.push.notification.PushNotificationMessage;
import io.rong.push.pushconfig.PushConfig;

public class App extends android.app.Application {

    private static App INSTANCE;

    @Override
    public void onCreate() {
        INSTANCE = this;
        super.onCreate();
        SessionManager.initContext(this);
        setPushEventListener();
    }

    public static App getApplication() {
        return INSTANCE;
    }

    public void setPushEventListener() {
        RongPushClient.setPushEventListener(
                new PushEventListener() {
                    @Override
                    public boolean preNotificationMessageArrived(Context context, PushType pushType, PushNotificationMessage notificationMessage) {
                        // 该回调仅在通知类型为透传消息时生效。返回 true 表示拦截，false 为不拦截
                        Log.i("preNotificationMessageArrived", "notificationMessage="+notificationMessage.toString());
                        RongNotification.sendNotification(INSTANCE, notificationMessage);
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
}
