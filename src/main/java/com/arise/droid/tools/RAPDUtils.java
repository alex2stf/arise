package com.arise.droid.tools;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.core.app.NotificationCompat;
import com.arise.droid.MainActivity;

import java.util.Map;

public class RAPDUtils {






    public static void hideKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static NotificationManager getNotificationManager(Context context){
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void createNotificationChannel(Context context, NotificationOps ops){
        NotificationManager notificationManager = getNotificationManager(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(ops.channelId, "channel_name", NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            notificationChannel.setDescription("SOME CHANNEL DESCRIPTION");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
//            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
//            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public static Notification createNotification(Context ctx, NotificationOps ops){
        createNotificationChannel(ctx, ops);
        return createNotification(ctx, ops.title, ops.text, ops.channelId,
                        ops.extra, ops.flags, ops.smallIcon);
    }


    static Notification createNotification(Context ctx,
                                           String title,
                                           String text,
                                           String channelId,
                                           Map<String, String> extra,
                                           int flags,
                                           int smallIcon){


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx, channelId)
                        .setSmallIcon(smallIcon)
                        .setContentTitle(title)
                        .setContentText(text);

        //ptr poza
//        .setStyle(new NotificationCompat.BigTextStyle()
//                .bigText(emailObject.getSubjectAndSnippet()))

//        if (playSound) {
//            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            mBuilder.setSound(soundUri);
//        }


        Intent intent = new Intent(ctx, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        for (Map.Entry<String, String> entry: extra.entrySet()){
            intent.putExtra(entry.getKey(), entry.getValue());
        }
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        ctx, 0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


//        mBuilder.setAutoCancel()
        mBuilder.setContentIntent(resultPendingIntent);

        if (flags == Notification.FLAG_ONGOING_EVENT) {
            mBuilder.setOngoing(true); //make it uncloseable
        }

        Notification notification = mBuilder.build();
        notification.flags = flags;
        notification.contentIntent = resultPendingIntent;

//        if (vibrate) {
//            vibrate(vibratePattern, -1);
//        }

        // Builds the notification and issues it.
//        startForeground(notId, notification);
//        getNotificationManager(context, channelId).notify(notId, notification);
        return notification;
    }
}
