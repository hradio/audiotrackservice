package eu.hradio.core.audiotrackservice;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceAudiodataListener;

public class AudioTrackSinkSdk16 extends AudioTrackSink implements RadioServiceAudiodataListener {

	private static final String TAG = "AudioTrackSinkSdk16";

	private AudioTrack mAudioTrack = null;

	private int mNumChannels = 2;
	private int mSamplingRate = 48000;

	private int mAudioTrackVolume = 100;
	private int mLastVolume = -1;

	private int mAudioTrackChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;

	private RadioService mRadioService;

	private byte[] mShortWriteData = null;

	AudioTrackSinkSdk16() {
		setupAudiotrack(mSamplingRate, mNumChannels);
	}

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

		int minBuffSize = (int)framesToBytes(durationMsToFrames(360));

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSamplingRate, mAudioTrackChannelConfig, AudioFormat.ENCODING_PCM_16BIT, minBuffSize, AudioTrack.MODE_STREAM);

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
		}
	}

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

	@Override
	void setVolume(int newVolume) {
		newVolume = Math.min(Math.max(newVolume, 0), 100);

		float gain = ((float) newVolume / 100);
		if(mAudioTrack.setStereoVolume(gain, gain) == AudioTrack.SUCCESS) {
			mAudioTrackVolume = newVolume;
		}
	}

	@Override
	int getVolume() {
		return mAudioTrackVolume;
	}

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

	@Override
	boolean isMuted() {
		return mAudioTrackVolume == 0;
	}

	@Override
	void flush() {
		if(mAudioTrack != null) {
			if(BuildConfig.DEBUG)Log.d(TAG, "Flushing AudioTrack");
			mAudioTrack.pause();
			mAudioTrack.flush();
			mAudioTrack.play();
		}
	}

	@Override
	void cleanUp() {
		if(BuildConfig.DEBUG)Log.d(TAG, "Cleaning up...");
		if(mRadioService != null) {
			if(BuildConfig.DEBUG)Log.d(TAG, "Cleaning up RadioService");
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
