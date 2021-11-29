/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package test.opensave;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.Projector;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import org.junit.jupiter.api.Test;
import test.Utils;

public class TestOpenSingleChannelFloatTiffVolumeWithBioFormatsAndSaveAsTiffVolume
{
    private static Image image;

    public static void main( String[] args)
    {
        Utils.prepareInteractiveMode();

        new TestOpenSingleChannelFloatTiffVolumeWithBioFormatsAndSaveAsTiffVolume().run();
        BigDataProcessor2.showImage( image );
    }

    @Test
    public void run()
    {
        String file = "src/test/resources/test/tiff-nc1-nt1-ImageJFloat/mri-stack-ij-float.tif";
        //file = "/Volumes/cba/exchange/Shuting/Ecad_Sqh_100x_20210511_01_decon.ics";

        image = BigDataProcessor2.openBioFormats( file, 0 );
        image.setVoxelDimensions( 1, 1, 20 );

        final SavingSettings settings = SavingSettings.getDefaults();
        settings.volumesFilePathStump = "src/test/resources/test-output/" + image.getName() + "/volumes/" + image.getName();
        settings.saveVolumes = true;
        settings.projectionsFilePathStump = "src/test/resources/test-output/" + image.getName() + "/projections/" + image.getName();
        settings.saveProjections = true;
        settings.projectionMode = Projector.SUM;
        settings.fileType = SaveFileType.TIFFVolumes;
        settings.numProcessingThreads = 4;
        settings.numIOThreads = 1;
        settings.compression = SavingSettings.COMPRESSION_NONE;
        settings.tStart = 0;
        settings.tEnd = image.getNumTimePoints() - 1;

        BigDataProcessor2.saveImage( image, settings, new LoggingProgressListener( "Progress" ) );
    }
}
