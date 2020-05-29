package eu.hradio.core.audiosinkinterface;

import android.os.Binder;

public abstract class AudioSinkBinder extends Binder {

	/**
	 * Sets the output volume of this AudioSink
	 * @param volumePercent the volume in the range 0% - 100%
	 */
	public abstract void setVolume(int volumePercent);

	/**
	 * Returns the current set output volume
	 * @return the current set output volume
	 */
	public abstract int getVolume();

	/**
	 * Mutes/un-mutes this AudioSink
	 * @param mute <em>true</em> to mute, <em>false</em> otherwise
	 */
	public abstract void mute(boolean mute);

	/**
	 * Indicates if this AudioSink is currently muted
	 * @return <em>true</em> if this AudioSink is currently muted, <em>false</em> otherwise
	 */
	public abstract boolean isMuted();
}
