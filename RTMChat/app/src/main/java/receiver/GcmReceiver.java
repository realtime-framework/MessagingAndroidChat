package receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rtmchat.realtime.co.rtmchat.R;
import rtmchat.realtime.co.rtmchat.activities.MessageActivity;
import rtmchat.realtime.co.rtmchat.activities.NotificationActivity;
import ibt.ortc.extensibility.GcmOrtcBroadcastReceiver;

public class GcmReceiver extends GcmOrtcBroadcastReceiver {

    private static final String TAG = "GcmReceiver";
    private static final String MESSAGE_PATTERN = "^(.[^_]*)_(.[^-]*)-(.[^_]*)_([\\s\\S]*?)$";
    private static final Pattern messagePattern = Pattern.compile(MESSAGE_PATTERN);

    public GcmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received message");
        Bundle extras = intent.getExtras();
        if (extras != null && !MessageActivity.isInForeground() && !NotificationActivity.isInForeground()) {
            createNotification(context, extras);
        }
    }

    private String parseOrtcMessage(String ortcMessage) {

        // Automatic ORTC push messages have the following format:
        // <msg_id>_1-1_<message sent by user>

        Matcher parsedMessageMatcher = messagePattern.matcher(ortcMessage);
        String parsedMessage = "";

        try{
            if (parsedMessageMatcher.matches()) {
                parsedMessage = parsedMessageMatcher.group(4);
            }
        } catch (Exception parseException){
            // probably a custom push message, use the received string with no parsing
            parsedMessage = ortcMessage;
        }

        if(parsedMessage == "") {
            // there's something wrong with the message format. Use the unparsed format.
            parsedMessage = ortcMessage;
        }

        return parsedMessage;
    }


    public void createNotification(Context context, Bundle extras)
    {
        String message = extras.getString("M");
        String channel = extras.getString("C");
        String payload = extras.getString("P"); // this is only used by custom notifications

        if (message != "") {

            String parsedMessage = parseOrtcMessage(message);

            if(payload != null) {
                Log.i(TAG, String.format("Custom push notification on channel: %s message: %s payload: %s", channel, parsedMessage, payload));
            }else{
                Log.i(TAG, String.format("Automatic push notification on channel: %s message: %s ", channel, parsedMessage));
            }

            try {

                // parsed message format: <user>:<chat message>

                String parts [] = parsedMessage.split(":");
                String user = parts[0].split("_")[parts[0].split("_").length - 1];
                String chatMessage = parts[1];

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent notificationIntent = new Intent(context, NotificationActivity.class);

                notificationIntent.putExtra("channel", channel);
                notificationIntent.putExtra("message", chatMessage);
                notificationIntent.putExtra("user", user);

                String appName = getAppName(context);

                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 9999, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = new NotificationCompat.Builder(context).setContentTitle(channel).setContentText(chatMessage).setSmallIcon(R.drawable.ic_launcher)
                        .setContentIntent(pendingIntent).setAutoCancel(true).build();
                notificationManager.notify(appName, 9999, notification);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private String getAppName(Context context)
    {
        CharSequence appName =
                context
                        .getPackageManager()
                        .getApplicationLabel(context.getApplicationInfo());

        return (String)appName;
    }

}