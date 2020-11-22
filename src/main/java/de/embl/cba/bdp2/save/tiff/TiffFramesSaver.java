package de.embl.cba.bdp2.save.tiff;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import de.embl.cba.bdp2.save.AbstractImageSaver;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.log.progress.Progress;
import de.embl.cba.bdp2.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TiffFramesSaver extends AbstractImageSaver
{
    private SavingSettings savingSettings;
    private ExecutorService es;
    private AtomicBoolean stop;

    public TiffFramesSaver( SavingSettings savingSettings, ExecutorService es ) {
        this.savingSettings = savingSettings;
        this.es = es;
        this.stop = new AtomicBoolean(false);
    }

    @Override
    public void startSave() {
        List<Future> futures = new ArrayList<>();

        AtomicInteger counter = new AtomicInteger( 0 );
        final long startTime = System.currentTimeMillis();

        Logger.debug( "# TiffFramesSaver..." );

        for (int t = savingSettings.tStart; t <= savingSettings.tEnd; t++)
        {
            futures.add(
                    es.submit(
                            new TiffFrameSaver(
                                    t,
                                    savingSettings,
                                    counter,
                                    startTime,
                                    stop)
                    ));
        }

        // Inform progress listener about progress in terms of finished time-points (i.e. futures)
        Thread thread = new Thread(
                () -> Progress.informProgressListeners(
                        futures,
                        FileInfos.PROGRESS_UPDATE_MILLISECONDS,
						progressListeners ));

        thread.start();
    }

    @Override
    public void stopSave() {
        stop.set(true);
        Utils.shutdownThreadPack(es,TIME_OUT_SECONDS);
    }
}
