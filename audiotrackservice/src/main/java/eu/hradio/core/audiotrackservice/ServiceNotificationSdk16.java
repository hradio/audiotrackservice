package eu.hradio.core.audiotrackservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class ServiceNotificationSdk16 extends ServiceNotification {

	private final static String TAG = "NotificationSdk16";

	private final static int NOTIFICATION_CHANNEL_ID = 1704;

	private NotificationManager mNotificationManager = null;
	private Notification mNotification = null;
	private Notification.Builder mNotificationBuilder = null;

	@Override
	void showNotification(Service context) {
		if(BuildConfig.DEBUG) Log.d(TAG, "Showing notification");

		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationBuilder = new Notification.Builder(context);
		mNotification = mNotificationBuilder
						.setContentTitle("AudioTrackService")
						.setContentText("AudioTrack notification")
						.setSmallIcon(R.mipmap.audiotrackservice_notification)
						.setStyle(new Notification.BigTextStyle())
						.build();

		context.startForeground(NOTIFICATION_CHANNEL_ID, mNotification);
	}

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

	@Override
	public void setNotificationTitle(String title) {
		if(BuildConfig.DEBUG)Log.d(TAG, "Setting new notification title: " + title);
		if(mNotificationBuilder != null && mNotificationManager != null) {
			mNotificationBuilder.setContentTitle(title);
			mNotificationManager.notify(NOTIFICATION_CHANNEL_ID, mNotificationBuilder.build());
		}
	}

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
