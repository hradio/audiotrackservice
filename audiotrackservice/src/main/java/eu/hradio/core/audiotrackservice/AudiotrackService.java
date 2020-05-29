package eu.hradio.core.audiotrackservice;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceAudiodataListener;

import eu.hradio.core.audiosinkinterface.AudioSinkBinder;
import eu.hradio.core.audiosinkinterface.AudioSinkService;

public class AudiotrackService extends AudioSinkService {

	private final static String TAG = "AudioTrackService";

	private AudioTrackBinder mBinder = null;

	private AudioTrackSink mAudioTrackSink = null;
	private ServiceNotification mServiceNotification = null;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if(BuildConfig.DEBUG) Log.d(TAG, "onConfigurationChanged: " + newConfig.orientation);
	}

	@Nullable
	@Override
	public AudioSinkBinder onBind(Intent intent) {
		if(BuildConfig.DEBUG) Log.d(TAG, "onBind");

		if(mBinder == null) {
			mBinder = new AudioTrackBinder();
		}

		createAudioTrackSink();

		return mBinder;
	}

	@Override
	public void onCreate() {

		createAudioTrackSink();

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(BuildConfig.DEBUG) Log.d(TAG, "onStartCommand, Flags: " + flags + ", StartID: " + startId);

		createAudioTrackSink();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if(BuildConfig.DEBUG)Log.d(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(BuildConfig.DEBUG)Log.d(TAG, "onDestroy");

		if(mAudioTrackSink != null) {
			mAudioTrackSink.cleanUp();
		}

		if(mServiceNotification != null) {
			mServiceNotification.dismissNotification();
		}
	}

	private void createAudioTrackSink() {
		if(mAudioTrackSink == null) {
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				if(BuildConfig.DEBUG)Log.d(TAG, "creating SDK 21 AudioTrackSink");
				mAudioTrackSink = new AudioTrackSinkSdk21();
			} else {
				if(BuildConfig.DEBUG)Log.d(TAG, "creating SDK 16 AudioTrackSink");
				mAudioTrackSink = new AudioTrackSinkSdk16();
			}

			if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				mServiceNotification = new ServiceNotificationSdk26();
			} else {
				mServiceNotification = new ServiceNotificationSdk16();
			}

			//TODO notification for foreground service
			mServiceNotification.showNotification(this);
		}
	}

	/* Binder interface to interact with the service */
	public class AudioTrackBinder extends AudioSinkBinder {

		public AudiotrackService getServiceInstance() {
			return AudiotrackService.this;
		}

		public ServiceNotification getNotification() {
			return mServiceNotification;
		}

		/**
		 * Set a running {@link RadioService} for playback. Set to <em>null</em> to stop audible playback
		 * @param service the running {@link RadioService} or <em>null</em> to stop audible playback
		 */
		public void setService(RadioService service) {
			if(BuildConfig.DEBUG)Log.d(TAG, "setService: " + (service != null ? service.getServiceLabel() : "null"));

			if(mAudioTrackSink != null) {
				mAudioTrackSink.playService(service);
			}
		}

		/**
		 * Returns the currently running {@link RadioService}
		 * @return the currently running {@link RadioService} or <em>null</em> if none is currently set
		 */
		public RadioService getCurrentService() {
			RadioService retSrv = null;
			if(mAudioTrackSink != null) {
				retSrv = mAudioTrackSink.getCurrentService();
			}

			return retSrv;
		}

		/**
		 * Writes directly the PCM samples
		 * @param pcmData the PCM samples
		 * @param numChannels the number of channels of the PCM samples
		 * @param samplingRate the sampling rate of the PCM samples
		 */
		public void writePcmAudioData(byte[] pcmData, int numChannels, int samplingRate) {
			if(mAudioTrackSink != null) {
				mAudioTrackSink.pcmAudioData(pcmData, numChannels, samplingRate);
			}
		}

		/**
		 * Returns a {@link RadioServiceAudiodataListener} to register it to a {@link RadioService} PCM audio callback
		 * @return a {@link RadioServiceAudiodataListener} to register it to a {@link RadioService} PCM audio callback
		 */
		public RadioServiceAudiodataListener getAudioDataListener() {
			return mAudioTrackSink;
		}

		@Override
		public void setVolume(int volumePercent) {
			if(mAudioTrackSink != null) {
				mAudioTrackSink.setVolume(volumePercent);
			}
		}

		@Override
		public int getVolume() {
			int vol = -1;
			if(mAudioTrackSink != null) {
				vol = mAudioTrackSink.getVolume();
			}

			return vol;
		}

		@Override
		public void mute(boolean mute) {
			if(mAudioTrackSink != null) {
				mAudioTrackSink.mute(mute);
			}
		}

		@Override
		public boolean isMuted() {
			boolean muted = false;
			if(mAudioTrackSink != null) {
				muted = mAudioTrackSink.isMuted();
			}

			return muted;
		}

		public void flush() {
			if(mAudioTrackSink != null) {
				mAudioTrackSink.flush();
			}
		}
	}
}