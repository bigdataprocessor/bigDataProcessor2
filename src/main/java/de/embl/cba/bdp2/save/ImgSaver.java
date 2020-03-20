package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.log.progress.ProgressListener;

public interface ImgSaver
{
	void addProgressListener( ProgressListener l );
	void startSave();
	void stopSave();
}
