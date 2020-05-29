package eu.hradio.core.audiotrackservice;

import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceAudiodataListener;

abstract class AudioTrackSink implements RadioServiceAudiodataListener {

	abstract void playService(RadioService service);

	abstract RadioService getCurrentService();

	abstract void setVolume(int volumePercent);

	abstract int getVolume();

	abstract void mute(boolean mute);

	abstract boolean isMuted();

	abstract void flush();

	abstract void cleanUp();
}
