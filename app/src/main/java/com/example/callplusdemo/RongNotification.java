package com.example.callplusdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import io.rong.imlib.common.DeviceUtils;
import io.rong.push.PushType;
import io.rong.push.RongPushClient;
import io.rong.push.common.PushCacheHelper;
import io.rong.push.common.RLog;
import io.rong.push.notification.PushNotificationMessage;
import io.rong.push.notification.RongBridgeActivity;
import io.rong.push.notification.RongNotificationHelper;
import io.rong.push.notification.RongNotificationInterface;

public class RongNotification extends RongNotificationInterface {
    private static final int PUSH_REQUEST_CODE = 200;
    private static final int NEGLECT_TIME = 3000;
    private static HashMap<String, List<PushNotificationMessage>> messageCache = new HashMap();
    private static long lastNotificationTimestamp;
    private static Uri mSound;
    private static boolean recallUpdate = false;

    public static void sendNotification(Context context, PushNotificationMessage message) {
        sendNotification(context, message, PushType.RONG, 0);
    }

    public static void sendNotification(Context context, PushNotificationMessage message, PushType pushType, int left) {
        if (messageCache == null) {
            messageCache = new HashMap();
        }

        RongPushClient.ConversationType conversationType = message.getConversationType();
        String objName = message.getObjectName();
        String title = null;
        String content = "";
        Integer notificationId = null;
        boolean isMulti = false;
        SoundType soundType = RongNotificationInterface.SoundType.DEFAULT;
        RLog.i("RongNotificationInterface", "sendNotification() messageType: " + message.getConversationType() + " messagePushContent: " + message.getPushContent() + " messageObjectName: " + message.getObjectName());
        if (!TextUtils.isEmpty(objName) && conversationType != null) {
            long now = System.currentTimeMillis();
            if (now - lastNotificationTimestamp < 3000L) {
                soundType = RongNotificationInterface.SoundType.SILENT;
            } else {
                lastNotificationTimestamp = now;
            }

            String channelId = RongNotificationHelper.getDefaultChannelId();
            String channelNameResName = RongNotificationHelper.getDefaultChannelName();
            int i;
            if (!conversationType.equals(RongPushClient.ConversationType.SYSTEM) && !conversationType.equals(RongPushClient.ConversationType.PUSH_SERVICE)) {
                if (!objName.equals("RC:VCInvite") && !objName.equals("RC:VCModifyMem") && !objName.equals("RC:VCHangup")) {
                    List<PushNotificationMessage> messages = (List) messageCache.get(message.getTargetId());
                    if (messages == null) {
                        messages = new ArrayList<>();
                        messages.add(message);
                        messageCache.put(message.getTargetId(), messages);
                    } else if (!objName.equals("RC:RcNtf")) {
                        if (((PushNotificationMessage) messages.get(messages.size() - 1)).getObjectName().equals("RC:RcNtf")) {
                            messages.remove(messages.size() - 1);
                        }

                        messages.add(message);
                    } else {
                        for (i = messages.size() - 1; i >= 0; --i) {
                            if (messages.get(i) != null && ((PushNotificationMessage) messages.get(i)).getPushId() != null && ((PushNotificationMessage) messages.get(i)).getPushId().equals(message.getPushId())) {
                                try {
                                    notificationId = Integer.parseInt(((PushNotificationMessage) messages.get(i)).getNotificationId());
                                } catch (Exception var25) {
                                    RLog.d("RongNotificationInterface", "parse notificationId exception:" + var25.toString());
                                }

                                messages.remove(messages.get(i));
                                break;
                            }
                        }

                        if (messages.isEmpty()) {
                            if (messageCache.size() == 1) {
                                messages.add(message);
                            } else {
                                messageCache.remove(message.getTargetId());
                                if (messageCache.size() == 1) {
                                    recallUpdate = true;
                                }
                            }
                        }
                    }

                    if (messageCache.size() > 1) {
                        isMulti = true;
                    }

                    title = message.getPushTitle();
                    if (TextUtils.isEmpty(title) && message.isShowDetail()) {
                        title = message.getTargetUserName();
                    } else if (!message.isShowDetail()) {
                        title = null;
                    }

                    try {
                        notificationId = Integer.parseInt(message.getNotificationId());
                    } catch (Exception var24) {
                        RLog.d("RongNotificationInterface", "parse notificationId exception:" + var24.toString());
                    }

                    content = message.getPushContent();
                    if (notificationId == null) {
                        notificationId = RongNotificationHelper.getNotificationId(message.getPushId());
                        message.setNotificationId(notificationId.toString());
                    }
                } else {
                    if (objName.equals("RC:VCHangup")) {
                        removeNotification(context, RongNotificationHelper.getVoipNotificationId());
                        return;
                    }

                    try {
                        notificationId = Integer.parseInt(message.getNotificationId());
                    } catch (Exception var26) {
                        RLog.d("RongNotificationInterface", "parse notificationId exception:" + var26.toString());
                    }

                    if (notificationId == null) {
                        notificationId = RongNotificationHelper.getVoipNotificationId();
                        message.setNotificationId(notificationId.toString());
                    }

                    soundType = RongNotificationInterface.SoundType.VOIP;
                    channelId = RongNotificationHelper.getDefaultVoipChannelId();
                    channelNameResName = RongNotificationHelper.getDefaultVoipChannelName();
                    title = message.getPushTitle();
                    if (TextUtils.isEmpty(title) && message.isShowDetail()) {
                        title = message.getSenderName();
                    } else if (!message.isShowDetail()) {
                        title = null;
                    }

                    content = message.getPushContent();
                }
            } else {
                title = message.getPushTitle();
                if (TextUtils.isEmpty(title)) {
                    title = (String) context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
                }

                content = message.getPushContent();

                try {
                    notificationId = Integer.parseInt(message.getNotificationId());
                } catch (Exception var27) {
                    RLog.d("RongNotificationInterface", "parse notificationId exception:" + var27.toString());
                }

                if (notificationId == null) {
                    notificationId = RongNotificationHelper.getPushServiceNotificationId();
                    message.setNotificationId(notificationId.toString());
                }
            }

            if (left <= 0) {
                i = notificationId;
                PendingIntent intent;
                if (recallUpdate) {
                    intent = updateRecallPendingIntent(context, i, isMulti);
                } else {
                    intent = createPendingIntent(context, message, pushType, i, isMulti);
                }

                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null) {
                    Notification notification;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        channelId = getChannelId(context, message, channelId);
                        NotificationChannel notificationChannel = nm.getNotificationChannel(channelId);
                        if (notificationChannel == null) {
                            String channelName = null;

                            try {
                                channelName = context.getResources().getString(context.getResources().getIdentifier(channelNameResName, "string", context.getPackageName()));
                            } catch (Resources.NotFoundException var23) {
                                var23.printStackTrace();
                            }

                            if (TextUtils.isEmpty(channelName)) {
                                channelName = channelId;
                            }

                            notificationChannel = new NotificationChannel(RongNotificationHelper.getDefaultChannelId(), channelName, 4);
                            notificationChannel.enableLights(true);
                            notificationChannel.setLightColor(-16711936);
                            notificationChannel.setLockscreenVisibility(1);
                            notificationChannel.setSound(getSoundByType(soundType), (AudioAttributes) null);
                        }

                        notification = createNotification(context, title, intent, content, soundType, notificationChannel.getId());
                        nm.createNotificationChannel(notificationChannel);
                    } else {
                        notification = createNotification(context, title, intent, content, soundType, "");
                    }

                    if (notification != null) {
                        RLog.i("RongNotificationInterface", "sendNotification() real notify! notificationId: " + notificationId + " notification: " + notification.toString());

                        try {
                            nm.notify(notificationId, notification);
                        } catch (Exception var22) {
                            RLog.i("RongNotificationInterface", "notify for local notification Exception e:" + var22);
                        }
                    }

                }
            }
        }
    }

    private static PendingIntent updateRecallPendingIntent(Context context, int requestCode, boolean isMulti) {
        Collection<List<PushNotificationMessage>> collection = messageCache.values();
        List<PushNotificationMessage> msg = (List) collection.iterator().next();
        PushNotificationMessage notificationMessage = (PushNotificationMessage) msg.get(0);
        Intent intent = new Intent(context, RongBridgeActivity.class);
        intent.putExtra("message", notificationMessage);
//        intent.putExtra("isMulti", isMulti);
        intent.setPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        if (DeviceUtils.isBuildVersionFromAndroidM()) {
            flag = PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getActivity(context, requestCode, intent, flag);
    }

    private static PendingIntent createPendingIntent(Context context, PushNotificationMessage message, PushType pushType, int requestCode, boolean isMulti) {
        Intent intent = new Intent(context, RongBridgeActivity.class);
        intent.putExtra("message", message);
        intent.putExtra("pushType", pushType.getName());
//        intent.putExtra("isMulti", isMulti);
        intent.setPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        if (DeviceUtils.isBuildVersionFromAndroidM()) {
            flag = PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getActivity(context, requestCode, intent, flag);
    }

    private static String getChannelId(Context context, PushNotificationMessage message, String defaultChannelId) {
        String channelId = defaultChannelId;
        if (DeviceUtils.isBuildVersionFromAndroidO()) {
            PushType configPushType = PushCacheHelper.getInstance().getConfigPushType(context);
            if (PushType.XIAOMI.equals(configPushType)) {
                channelId = message.getChannelIdMi();
            } else if (PushType.HUAWEI.equals(configPushType)) {
                channelId = message.getChannelIdHW();
            } else if (PushType.OPPO.equals(configPushType)) {
                channelId = message.getChannelIdOPPO();
            } else if (PushType.GOOGLE_FCM.equals(configPushType)) {
                channelId = message.getChannelIdFCM();
            }
        }
        if (TextUtils.isEmpty(channelId)){
            channelId = defaultChannelId;
        }
        return channelId;
    }

    private static Uri getSoundByType(SoundType type) {
        return type.equals(RongNotificationInterface.SoundType.VOIP) ? RingtoneManager.getDefaultUri(1) : RingtoneManager.getDefaultUri(2);
    }
}
