/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
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
package de.embl.cba.bdp2.misc;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;

@Plugin(type = ShowAsHyperstackCommand.class, name = ShowAsHyperstackCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + "Commands>Misc>" + ShowAsHyperstackCommand.COMMAND_FULL_NAME )
public class ShowAsHyperstackCommand implements Command
{
    public static final String COMMAND_NAME = "Show as Hyperstack...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    /*
     * This parameter is preprocessed by the
     * SwingImageWidget class
     */
    @Parameter(label = "Input image")
    public Image inputImage;

    @Override
    public void run()
    {
        if ( inputImage == null )
        {
            throw new RuntimeException("Input image is null");
        }

        show( inputImage );
    }

    public static void show( Image< ? > image )
    {
        Utils.asImagePlus( image ).show();
    }

    public static void recordMacro( Image< ? > inputImage )
    {
        final ScriptRecorder recorder = new ScriptRecorder( ShowAsHyperstackCommand.COMMAND_FULL_NAME, inputImage );
        recorder.setBDP2FunctionName( "showImageAsHyperstack" );
        recorder.addAPIFunctionPrequelComment( ShowAsHyperstackCommand.COMMAND_NAME );
        recorder.record();
    }
}
