package de.embl.cba.bdp2.scijava.command;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.BdvService;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;

public class AbstractProcessingCommand< R extends RealType< R > & NativeType< R > >
{
    public static final String SHOW_IN_NEW_VIEWER = "Show in new viewer";
    public static final String REPLACE_IN_VIEWER = "Replace input image";
    public static final String DO_NOT_SHOW = "Do not show";

    @Parameter(label = "Input image name", persist = true)
    protected Image inputImage = ImageService.nameToImage.values().iterator().next();

    @Parameter(label = "Output image name")
    protected String outputImageName = ImageService.nameToImage.keySet().iterator().next() + "-binned";

    @Parameter(label = "Output image handling", choices = {
            REPLACE_IN_VIEWER,
            SHOW_IN_NEW_VIEWER,
            DO_NOT_SHOW })
    protected String outputModality;

    protected Image< R > outputImage;

    protected void handleOutputImage( boolean autoContrast, boolean keepViewerTransform )
    {
        outputImage.setName( outputImageName );
        ImageService.nameToImage.put( outputImageName, outputImage );

        if ( outputModality.equals( SHOW_IN_NEW_VIEWER ) )
        {
            new BdvImageViewer<>( outputImage, autoContrast );
        }
        else if ( outputModality.equals( REPLACE_IN_VIEWER ))
        {
            final BdvImageViewer viewer = BdvService.imageNameToBdv.get( inputImage.getName() );
            viewer.replaceImage( outputImage, autoContrast, keepViewerTransform );
        }
        else if ( outputModality.equals( DO_NOT_SHOW ))
        {
            // do nothing
        }
    }
}
