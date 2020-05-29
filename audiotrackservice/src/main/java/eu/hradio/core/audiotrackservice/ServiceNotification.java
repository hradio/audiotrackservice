package eu.hradio.core.audiotrackservice;

import android.app.PendingIntent;
import android.app.Service;
import android.graphics.Bitmap;

public abstract class ServiceNotification {

	abstract void showNotification(Service context);

	public abstract void dismissNotification();

	public abstract void setContentIntent(PendingIntent contentIntent);

	public abstract void setNotificationTitle(String title);

	public abstract void setNotificationText(String text);

	public abstract void setLargeIcon(Bitmap largeIcon);
}
