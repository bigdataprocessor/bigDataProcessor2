package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.progress.ProgressListener;

public interface ImgSaver
{
	void setProgressListener( ProgressListener l );
	void startSave();
	void stopSave();
}
