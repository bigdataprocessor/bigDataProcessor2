/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package stuff;

import bdv.img.imaris.Imaris;
import bdv.util.BdvFunctions;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;
import loci.common.DebugTools;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSaveSingleChannel16BitTiffSeriesAsImarisVolumes
{

    //@Test
    public void test( ) throws IOException
    {
        DebugTools.setRootLevel("OFF"); // Bio-Formats

        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory =
                TestConstants.TEST_FOLDER + "data/nc1-nt3-calibrated-16bit-tiff";

        final String loadingScheme = NamingSchemes.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.tif";

        final Image image = bdp.openTIFFSeries(
                directory,
                loadingScheme );

        // bdp.showImage( image );

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SaveFileType.ImarisVolumes;
        savingSettings.numIOThreads = 1;
        savingSettings.numProcessingThreads = 4;
        savingSettings.saveProjections = true;
        savingSettings.volumesFilePathStump =
                TestConstants.TEST_FOLDER + "test-output/nc1-nt3-calibrated-16bit-tiff-imaris-volumes/volume";
        savingSettings.saveVolumes = true;
        savingSettings.projectionsFilePathStump =
                TestConstants.TEST_FOLDER + "test-output/nc1-nt3-calibrated-16bit-tiff-imaris-projections/projection";

        final File testVolumeFile = new File( savingSettings.volumesFilePathStump + "--C00--T00000.h5" );
        if ( testVolumeFile.exists() ) testVolumeFile.delete();

        final File testProjectionsFile = new File( savingSettings.projectionsFilePathStump + "--xyz-max-projection--C00--T00002.tif" );
        if ( testProjectionsFile.exists() ) testProjectionsFile.delete();

        BigDataProcessor2.saveImageAndWaitUntilDone( image, savingSettings );

        assertTrue( testVolumeFile.exists() );
        assertTrue( testProjectionsFile.exists() );

        if ( TestConstants.interactive )
        {
            BdvFunctions.show( Imaris.openIms( savingSettings.volumesFilePathStump + ".ims" ) );
        }
    }

    public static void main( String[] args ) throws IOException
    {
        TestConstants.interactive = true;
        new TestSaveSingleChannel16BitTiffSeriesAsImarisVolumes().test();
    }

}
