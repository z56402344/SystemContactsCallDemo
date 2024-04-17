package com.example.callplusdemo;

import static com.example.callplusdemo.Base.FROM_SYSTEM_CONTACTS_KEY;

import android.content.Context;
import android.content.Intent;

import io.rong.push.PushType;
import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;

public class YourCustomMessageReceiver extends PushMessageReceiver {

    @Override
    public boolean onNotificationMessageClicked(Context context, PushType pushType, PushNotificationMessage message) {
        // 返回 true 表示拦截，false 为不拦截
        if (pushType.equals(PushType.GOOGLE_FCM)){
            Intent intent2 = new Intent(context, CallPlusActivity.class);
            intent2.putExtra(FROM_SYSTEM_CONTACTS_KEY, true);
            context.startActivity(intent2);
            return true;
        }
        return false;
    }
}
