package com.example.callplusdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.widget.RemoteViews;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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

//                            try {
//                                channelName = context.getResources().getString(context.getResources().getIdentifier(channelNameResName, "string", context.getPackageName()));
//                            } catch (Resources.NotFoundException var23) {
//                                var23.printStackTrace();
//                            }

                            channelName = "51talk";

                            if (TextUtils.isEmpty(channelName)) {
                                channelName = channelId;
                            }

                            notificationChannel = new NotificationChannel(RongNotificationHelper.getDefaultChannelId(), channelName, NotificationManager.IMPORTANCE_HIGH);
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
        intent.putExtra("isMulti", isMulti);
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
        intent.putExtra("isMulti", isMulti);
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
        if (TextUtils.isEmpty(channelId)) {
            channelId = defaultChannelId;
        }
        return channelId;
    }

    private static Uri getSoundByType(SoundType type) {
        return type.equals(RongNotificationInterface.SoundType.VOIP) ? RingtoneManager.getDefaultUri(1) : RingtoneManager.getDefaultUri(2);
    }

    public static Notification createNotification(Context context, String title, PendingIntent pendingIntent, String content, SoundType soundType, String channelId) {
        String tickerText = context.getResources().getString(context.getResources().getIdentifier("rc_notification_ticker_text", "string", context.getPackageName()));
        if (TextUtils.isEmpty(content)) {
            content = getNotificationContent(context);
        }

        Notification notification;
        if (DeviceUtils.isBuildVersionBelowAndroidH()) {
            try {
                notification = new Notification(context.getApplicationInfo().icon, tickerText, System.currentTimeMillis());
                Class<?> classType = Notification.class;
                Method method = classType.getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
                method.invoke(notification, context, title, content, pendingIntent);
                notification.flags |= 16;
                notification.defaults = -1;
            } catch (Exception var15) {
                var15.printStackTrace();
                return null;
            }
        } else {
            boolean isLollipop = DeviceUtils.isBuildVersionFromAndroidL();
            int smallIcon = context.getResources().getIdentifier("notification_small_icon", "drawable", context.getPackageName());
            if (smallIcon <= 0 || !isLollipop) {
                smallIcon = context.getApplicationInfo().icon;
            }

            int defaults = 1;
            Uri sound = null;
            if (soundType.equals(RongNotificationInterface.SoundType.SILENT)) {
                defaults = 4;
            } else if (soundType.equals(RongNotificationInterface.SoundType.VOIP)) {
                defaults = 6;
                sound = RingtoneManager.getDefaultUri(1);
            } else {
                sound = RingtoneManager.getDefaultUri(2);
            }

            Drawable loadIcon = context.getApplicationInfo().loadIcon(context.getPackageManager());
            Bitmap appIcon = null;

            try {
                if (DeviceUtils.isBuildVersionFromAndroidO() && loadIcon instanceof AdaptiveIconDrawable) {
                    appIcon = Bitmap.createBitmap(loadIcon.getIntrinsicWidth(), loadIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(appIcon);
                    loadIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    loadIcon.draw(canvas);
                } else {
                    appIcon = ((BitmapDrawable) loadIcon).getBitmap();
                }
            } catch (Exception var16) {
                var16.printStackTrace();
            }

            Notification.Builder builder = new Notification.Builder(context);
            builder.setLargeIcon(appIcon);
            if (!soundType.equals(RongNotificationInterface.SoundType.SILENT)) {
                builder.setVibrate(new long[]{0L, 200L, 250L, 200L});
            }

            builder.setSmallIcon(smallIcon);
            builder.setTicker(tickerText);
            builder.setContentTitle(title);
            builder.setContentText(content);
            builder.setContentIntent(pendingIntent);
            builder.setLights(-16711936, 3000, 3000);
            builder.setAutoCancel(true);
            if (DeviceUtils.isBuildVersionFromAndroidO()) {
                if (!TextUtils.isEmpty(channelId)) {
                    builder.setChannelId(channelId);
                } else {
                    builder.setChannelId("rc_notification_id");
                }
            }

            if (mSound != null && !TextUtils.isEmpty(mSound.toString())) {
                builder.setSound(mSound);
            } else {
                builder.setSound(sound);
                builder.setDefaults(defaults);
            }

            setCustomNotificationUI(context, builder, title, content);

            notification = builder.build();
            notification.flags |= 16;
            notification.flags |= 1;
        }

        return notification;
    }

    private static String getNotificationContent(Context context) {
        String rc_notification_new_msg = context.getResources().getString(context.getResources().getIdentifier("rc_notification_new_msg", "string", context.getPackageName()));
        String rc_notification_new_plural_msg = context.getResources().getString(context.getResources().getIdentifier("rc_notification_new_plural_msg", "string", context.getPackageName()));
        String content;
        if (messageCache.size() == 1) {
            Collection<List<PushNotificationMessage>> collection = messageCache.values();
            List<PushNotificationMessage> msg = (List)collection.iterator().next();
            PushNotificationMessage notificationMessage = (PushNotificationMessage)msg.get(0);
            if (msg.size() == 1) {
                content = notificationMessage.getPushContent();
            } else if (((PushNotificationMessage)msg.get(msg.size() - 1)).getObjectName().equals("RC:RcNtf")) {
                notificationMessage = (PushNotificationMessage)msg.get(msg.size() - 1);
                content = notificationMessage.getPushContent();
            } else {
                content = String.format(rc_notification_new_msg, notificationMessage.getTargetUserName(), msg.size());
            }
        } else {
            int count = 0;
            Collection<List<PushNotificationMessage>> collection = messageCache.values();

            List msg;
            for(Iterator var10 = collection.iterator(); var10.hasNext(); count += msg.size()) {
                msg = (List)var10.next();
            }

            content = String.format(rc_notification_new_plural_msg, messageCache.size(), count);
        }

        return content;
    }

    public static final String ACTION_CALL_KEY = "ACTION_CALL_KEY";
    public static final String ACTION_DECLINE_CALL = "ACTION_DECLINE_CALL";
    public static final String ACTION_ACCEPT_CALL = "ACTION_ACCEPT_CALL";
    // 设置自定义通知布局
    private static void setCustomNotificationUI(Context context, Notification.Builder builder, String title, String content) {
        // 自定义通知布局
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_voip_notification);
        // 设置图标
        int iconId = R.mipmap.ic_launcher_round; // 替换为你的图标资源ID
        remoteViews.setImageViewResource(R.id.icon, iconId);

        // 设置标题
        remoteViews.setTextViewText(R.id.title, title);

        // 设置副标题
        remoteViews.setTextViewText(R.id.subtitle, content);

        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        if (DeviceUtils.isBuildVersionFromAndroidM()) {
            flag = PendingIntent.FLAG_IMMUTABLE;
        }

        // 为挂断按钮设置PendingIntent
        Intent declineIntent = new Intent(context, CallPlusActivity.class);
        declineIntent.setAction(ACTION_DECLINE_CALL);
//        declineIntent.putExtra(ACTION_CALL_KEY, ACTION_DECLINE_CALL);
        PendingIntent declinePendingIntent = PendingIntent.getActivity(context, 0, declineIntent, flag);
        remoteViews.setOnClickPendingIntent(R.id.btn_decline, declinePendingIntent);

        // 为接听按钮设置PendingIntent
        Intent acceptIntent = new Intent(context, CallPlusActivity.class);
        acceptIntent.setAction(ACTION_ACCEPT_CALL);
//        declineIntent.putExtra(ACTION_CALL_KEY, ACTION_ACCEPT_CALL);
        PendingIntent acceptPendingIntent = PendingIntent.getActivity(context, 1, acceptIntent, flag);
        remoteViews.setOnClickPendingIntent(R.id.btn_accept, acceptPendingIntent);
        builder.setContent(remoteViews);
    }
}
