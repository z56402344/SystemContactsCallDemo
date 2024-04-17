package com.example.callplusdemo;

import static com.example.callplusdemo.Base.FROM_SYSTEM_CONTACTS_KEY;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取TelephonyManager实例
        Log.d("CallStateReceiver", "Call in progress");
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // 检查Intent的action是否是电话状态变化的action
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
            // 获取电话状态
            int callState = PhoneStateHelper.getCurrentCallState(context);

            // 判断电话是否处于通话状态
            if (callState == TelephonyManager.CALL_STATE_OFFHOOK) {
                // 电话正在通话中，执行你的逻辑操作
                // 例如，可以在这里显示一个通知，记录通话信息，或者执行其他操作
                // 这里仅作为示例，打印日志信息
//                String phoneNumber = telephonyManager.extractPackageInfo(intent.getExtras(), TelephonyManager.EXTRA_PHONE_NUMBER);
                String phoneNumber = "123";
//                if (phoneNumber != null) {
                    // 假设你想要记录电话号码
                    Log.d("CallStateReceiver", "Call in progress with number: " + phoneNumber);
                    Intent intent2 = new Intent(context, CallPlusActivity.class);
                    intent2.putExtra(FROM_SYSTEM_CONTACTS_KEY, true);
                    context.startActivity(intent2);
//                }
            }
        }
    }
}
