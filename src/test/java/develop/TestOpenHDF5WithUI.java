package develop;

import bdv.TransformEventHandler3D;
import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.volatiles.VolatileViews;
import de.embl.cba.bdp2.open.core.CachedCellImgCreator;
//import de.embl.cba.bdp2.open.core.FileInfos;
//import de.embl.cba.bdp2.open.core.NamingSchemes;
import net.imglib2.img.Img;

public class TestOpenHDF5WithUI {

    /**
     * IMPORTANT NOTE: Adjust Max value to 2550 in the Big Data Viewer. (Settings>Brightness and Color>Max)
     */
    public static void main(String[] args) {

        //final String directory = "Y:\\ashis\\2018-07-19_17.34.07\\";
        /*final String directory = "C:\\Users\\user\\Documents\\UNI_HEIDELBERG\\EMBL_HiwiJob\\fiji-plugin-bdp2\\New Folder\\";
        FileInfos fileInfos = new FileInfos(directory,
                NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
                ".*.h5",
                "Data");
        //FileInfoSource file = new FileInfoSource(directory,"None",".*.h5","Datawrong",true,10);
        Img myImg = new CachedCellImgCreator().createCachedCellImg( fileInfos );

        BdvSource bdvSource = BdvFunctions.show(
                VolatileViews.wrapAsVolatile( myImg ), "stream", BdvOptions.options().axisOrder(AxisOrder.XYCZT)

                //.doubleBuffered(false) TODO : useless ?
        //        .transformEventHandlerFactory(new TransformEventHandler3D.TransformEventHandler3DFactory()) TODO : find replacement
        );

            bdvSource.setDisplayRange( fileInfos.min_pixel_val, fileInfos.max_pixel_val);*/

    }
}
