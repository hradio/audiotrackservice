package eu.hradio.core.audiotrackservice;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceAudiodataListener;

public class AudioTrackSinkSdk21 extends AudioTrackSink implements RadioServiceAudiodataListener {

	private static final String TAG = "AudioTrackSinkSdk21";

	private AudioTrack mAudioTrack = null;

	private int mNumChannels = 2;
	private int mSamplingRate = 48000;

	private int mAudioTrackVolume = 100;
	private int mLastVolume = -1;

	private int mAudioTrackChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;

	private RadioService mRadioService;

	private byte[] mShortWriteData = null;

	@TargetApi(21)
	AudioTrackSinkSdk21() {
		setupAudiotrack(mSamplingRate, mNumChannels);
	}

	@TargetApi(21)
	private void setupAudiotrack(int samplingRate, int channels) {
		mSamplingRate = samplingRate;
		mNumChannels = channels;

		if(mNumChannels == 1) {
			mAudioTrackChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
		} else if(mNumChannels == 2) {
			mAudioTrackChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
		}

		if (mAudioTrack != null) {
			if(BuildConfig.DEBUG) Log.d(TAG, "Stopping old AudioTrack");
			if(mAudioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
				mAudioTrack.pause();
				mAudioTrack.flush();
				mAudioTrack.release();
			}
		}

		int minBuffSize = (int)framesToBytes(durationMsToFrames(120));
		if(BuildConfig.DEBUG)Log.d(TAG, "MinBuffSize: " + minBuffSize);

		AudioFormat format = new AudioFormat.Builder()
				.setEncoding(AudioFormat.ENCODING_PCM_16BIT)
				.setChannelMask(mAudioTrackChannelConfig)
				.setSampleRate(mSamplingRate)
				.build();

		AudioAttributes attrs = new AudioAttributes.Builder()
				.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
				.setUsage(AudioAttributes.USAGE_MEDIA)
				.build();

		mAudioTrack = new AudioTrack(attrs, format, minBuffSize, AudioTrack.MODE_STREAM, 0);

		switch (mAudioTrack.getState()) {
			case AudioTrack.STATE_INITIALIZED: {
				if(BuildConfig.DEBUG)Log.i(TAG, "AudioTrack State Initialized: " + mAudioTrack.getState());
				break;
			}
			case AudioTrack.STATE_UNINITIALIZED: {
				if(BuildConfig.DEBUG)Log.i(TAG, "AudioTrack State Initialized: " + mAudioTrack.getState());
				break;
			}
			case AudioTrack.STATE_NO_STATIC_DATA: {
				if(BuildConfig.DEBUG)Log.i(TAG, "AudioTrack State No Static Data: " + mAudioTrack.getState());
				break;
			}
			default: {
				if(BuildConfig.DEBUG)Log.i(TAG, "AudioTrack State Unknown: " + mAudioTrack.getState());
				break;
			}
		}
	}

	@TargetApi(21)
	@Override
	void playService(RadioService service) {
		if(BuildConfig.DEBUG)Log.d(TAG, "Playing Service: " + (service != null ? service.getServiceLabel() : "null"));
		if(service != null) {
			if(mRadioService != null) {
				mRadioService.unsubscribe(this);

				if (mAudioTrack != null) {
					if (BuildConfig.DEBUG) Log.d(TAG, "Resetting old AudioTrack");
					mAudioTrack.pause();
					mAudioTrack.flush();
				}
			}

			mRadioService = service;
			mRadioService.subscribe(this);
		} else if(mRadioService != null) {
			mRadioService.unsubscribe(this);
			mRadioService = null;
		}
	}

	@TargetApi(21)
	@Override
	RadioService getCurrentService() {
		return mRadioService;
	}

	private long framesToBytes(long frames) {
		return frames * 4;
	}

	private long durationMsToFrames(long durationMs) {
		return (durationMs * mSamplingRate) / 1000L;
	}

	/* AudioDataListener */
	@TargetApi(21)
	@Override
	public void pcmAudioData(byte[] pcmData, int numChannels, int samplingRate) {
		if(numChannels != mNumChannels || samplingRate != mSamplingRate) {
			setupAudiotrack(samplingRate, numChannels);
		}

		if(mAudioTrack != null && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
			if(mShortWriteData != null) {
				if(BuildConfig.DEBUG)Log.d(TAG, "Writing Shortwritedata: " + mShortWriteData.length);
				int shortWritten = mAudioTrack.write(mShortWriteData, 0, mShortWriteData.length);

				if(BuildConfig.DEBUG)Log.d(TAG, "Shortwritten " + shortWritten + " from " + mShortWriteData.length + " bytes");

				//Should not happen very often
				if(shortWritten < mShortWriteData.length) {
					if(BuildConfig.DEBUG)Log.d(TAG, "Shortwrite on writing ShortwriteData....");

					int shortLeftLen = mShortWriteData.length - shortWritten;

					byte[] tempShortData = new byte[shortLeftLen];
					System.arraycopy(mShortWriteData, shortWritten, tempShortData, 0, shortLeftLen);

					mShortWriteData = new byte[shortLeftLen + pcmData.length];
					System.arraycopy(tempShortData, 0, mShortWriteData, 0, tempShortData.length);
					System.arraycopy(pcmData, 0, mShortWriteData, tempShortData.length, pcmData.length);

					return;
				}

				mShortWriteData = null;
			}

			int written = mAudioTrack.write(pcmData, 0, pcmData.length);
			if(written < pcmData.length) {
				if(BuildConfig.DEBUG)Log.d(TAG, "AudioTrack shortwrite in state: " + mAudioTrack.getPlayState() + ", " + written + " from " + pcmData.length + " bytes");
				if(mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED || mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
					if(BuildConfig.DEBUG)Log.d(TAG, "Starting AudioTrack play");
					mAudioTrack.play();

					written += mAudioTrack.write(pcmData, written, pcmData.length-written);

					if(BuildConfig.DEBUG)Log.d(TAG, "Writing ShortWrit after Play: " + written + " : " + pcmData.length);
				}

				if(written < pcmData.length) {
					int shortWrittenDataLength = pcmData.length - written;
					mShortWriteData = new byte[shortWrittenDataLength];
					System.arraycopy(pcmData, written, mShortWriteData, 0, shortWrittenDataLength);
				}
			}
		} else {
			if(BuildConfig.DEBUG)Log.w(TAG, "Audiotrack is in error condition...trying to resolve...");
			setupAudiotrack(samplingRate, numChannels);
		}
	}

	/* API to AudioTrackService */
	@TargetApi(21)
	@Override
	void setVolume(int newVolume) {
		newVolume = Math.min(Math.max(newVolume, 0), 100);

		float gain = ((float) newVolume / 100);
		if(mAudioTrack.setVolume(gain) == AudioTrack.SUCCESS) {
			mAudioTrackVolume = newVolume;
		}
	}

	@TargetApi(21)
	@Override
	int getVolume() {
		return mAudioTrackVolume;
	}

	@TargetApi(21)
	@Override
	void mute(boolean mute) {
		if(mute && mAudioTrackVolume > 0) {
			mLastVolume = mAudioTrackVolume;
			setVolume(0);
		} else {
			setVolume(mLastVolume);
			mLastVolume = -1;
		}
	}

	@TargetApi(21)
	@Override
	boolean isMuted() {
		return mAudioTrackVolume == 0;
	}

	@TargetApi(21)
	@Override
	void flush() {
		if(mAudioTrack != null) {
			if(BuildConfig.DEBUG)Log.d(TAG, "Flushing AudioTrack");
			mAudioTrack.pause();
			mAudioTrack.flush();
			mAudioTrack.play();
		}
	}

	@TargetApi(21)
	@Override
	void cleanUp() {
		if(BuildConfig.DEBUG)Log.d(TAG, "Cleaning up...");
		if(mRadioService != null) {
			if(BuildConfig.DEBUG)Log.d(TAG, "Cleaning up RadiOService");
			mRadioService.unsubscribe(this);
			mRadioService = null;
		}

		if(mAudioTrack != null) {
			if(BuildConfig.DEBUG)Log.d(TAG, "Cleaning up AudioTrack");
			mAudioTrack.pause();
			mAudioTrack.flush();
			mAudioTrack.release();

			mAudioTrack = null;
		}
	}
}
