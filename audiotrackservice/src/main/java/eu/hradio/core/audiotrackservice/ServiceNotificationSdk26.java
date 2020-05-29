package eu.hradio.core.audiotrackservice;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.O)
public class ServiceNotificationSdk26 extends ServiceNotification {

	private final static String TAG = "NotificationSdk26";

	private final static String NOTIFICATION_CHANNEL_ID_STRING = "1704";
	private final static int NOTIFICATION_CHANNEL_ID = 1704;
	private final static int NOTIFICATION_IMPORTANCE = NotificationManager.IMPORTANCE_LOW;
	private final static CharSequence NOTIFICATION_CHANNEL_NAME = "AudiotrackServiceChanel";

	private NotificationManager mNotificationManager = null;
	private Notification.Builder mNotificationBuilder = null;
	private Notification mNotification = null;

	@TargetApi(Build.VERSION_CODES.O)
	@Override
	void showNotification(Service context) {
		if(BuildConfig.DEBUG) Log.d(TAG, "Showing notification");

		// Create a channel.
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if(mNotificationManager != null) {
			NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_STRING, NOTIFICATION_CHANNEL_NAME, NOTIFICATION_IMPORTANCE);
			notificationChannel.enableVibration(false);
			notificationChannel.enableLights(false);
			mNotificationManager.createNotificationChannel(notificationChannel);
		}

		mNotificationBuilder = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID_STRING);
		mNotification = mNotificationBuilder.setContentTitle("")
				.setContentText("")
				.setSmallIcon(R.mipmap.audiotrackservice_notification)
				.setContentIntent(null)
				.setStyle(new Notification.BigTextStyle())
				.setOnlyAlertOnce(true)
				.build();

		context.startForeground(NOTIFICATION_CHANNEL_ID, mNotification);
	}

	@TargetApi(Build.VERSION_CODES.O)
	@Override
	public void dismissNotification() {
		if(BuildConfig.DEBUG)Log.d(TAG, "Killing notification");

		if(mNotificationManager != null) {
			mNotificationManager.cancel(NOTIFICATION_CHANNEL_ID);
		}
	}

	@Override
	public void setContentIntent(PendingIntent contentIntent) {
		if(mNotificationBuilder != null && mNotificationManager != null) {
			mNotificationBuilder.setContentIntent(contentIntent);
			mNotificationManager.notify(NOTIFICATION_CHANNEL_ID, mNotificationBuilder.build());
		}
	}

	@TargetApi(Build.VERSION_CODES.O)
	@Override
	public void setNotificationTitle(String title) {
		if(BuildConfig.DEBUG)Log.d(TAG, "Setting new notification title: " + title);
		if(mNotificationBuilder != null && mNotificationManager != null) {
			mNotificationBuilder.setContentTitle(title);
			mNotificationManager.notify(NOTIFICATION_CHANNEL_ID, mNotificationBuilder.build());
		}
	}

	@TargetApi(Build.VERSION_CODES.O)
	@Override
	public void setNotificationText(String text) {
		if(BuildConfig.DEBUG)Log.d(TAG, "Setting new notification text: " + text);
		if(mNotificationBuilder != null) {
			mNotificationBuilder.setContentText(text);
			mNotificationManager.notify(NOTIFICATION_CHANNEL_ID, mNotificationBuilder.build());
		}
	}

	@Override
	public void setLargeIcon(Bitmap largeIcon) {
		if(BuildConfig.DEBUG)Log.d(TAG, "Setting new large icon");
		if(mNotificationBuilder != null) {
			mNotificationBuilder.setLargeIcon(largeIcon);
			mNotificationManager.notify(NOTIFICATION_CHANNEL_ID, mNotificationBuilder.build());
		}
	}
}
