package com.example.callplusdemo;

import static android.Manifest.permission.READ_PHONE_STATE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class PhoneStateHelper {

    public static int getCurrentCallState(Context context) {
        // 获取TelephonyManager实例
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // 获取当前电话状态
        int callState = telephonyManager.getCallState();
        Log.i("PhoneStateHelper", "callState: " + callState);

        // 返回电话状态
        return callState;
    }

    public static String getCurrentCallPhoneNumber(Context context) {
        // 获取TelephonyManager实例
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // 检查是否已经获取了读取电话状态的权限
        if (ContextCompat.checkSelfPermission(context, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // 没有权限，无法获取电话号码
            return "Permission not granted";
        }

        // 获取当前电话状态
        String phoneNumber = telephonyManager.getLine1Number();

        // 检查是否有正在进行的通话
//        if (isIncomingCall(telephonyManager) || isOutgoingCall(telephonyManager)) {
//            // 获取来电或去电的电话号码
////            String callNumber = telephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING || telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK
////                    ? telephonyManager.getIncomingNumber() : telephonyManager.getOutgoingNumber();
////            return callNumber != null ? callNumber : "Unknown";
//        } else {
            // 没有正在进行的通话
            return "No ongoing call";
//        }
    }

    private static boolean isIncomingCall(TelephonyManager telephonyManager) {
        // 检查是否有来电
        return telephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING;
    }

    private static boolean isOutgoingCall(TelephonyManager telephonyManager) {
        // 检查是否有去电
        return telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK;
    }
}
