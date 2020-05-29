package eu.hradio.core.audiosinkinterface;

import android.app.Service;
import android.content.Intent;

public abstract class AudioSinkService extends Service {

	@Override
	public abstract AudioSinkBinder onBind(Intent intent);
}
