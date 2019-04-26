import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import de.embl.cba.bdp2.CachedCellImageCreator;
import de.embl.cba.bdp2.files.FileInfos;
import de.embl.cba.bdp2.viewers.BdvTransformEventHandler;
import net.imglib2.img.Img;

public class TestOpenLeftRightHDF5WithUI {

    /**
     * IMPORTANT NOTE: Adjust Max value to 2550 in the Big Data Viewer. (Settings>Brightness and Color>Max)
     */
    public static void main(String[] args) {


        final String directory = "Y:\\ashis\\movi\\stack_0_channel_2\\";
        FileInfos fileInfosLeft = new FileInfos(directory,"None",
                ".*Left.*.h5","Data");
        FileInfos fileInfosRight = new FileInfos(directory,"None",
                ".*Right.*.h5","Data");

        Img myImgLeft = new CachedCellImageCreator().create( fileInfosLeft,null);
        Img myImgRight = new CachedCellImageCreator().create( fileInfosRight,null);
        double [] voxelSize = new double[]{0,0};
        final BdvStackSource bdvss0 = BdvFunctions.show(myImgLeft, "left", BdvOptions.options().axisOrder(AxisOrder.XYCZT)
                .doubleBuffered(false)
                .transformEventHandlerFactory(new BdvTransformEventHandler.BehaviourTransformEventHandler3DFactory( voxelSize )));



        final BdvStackSource bdvss1 = BdvFunctions.show(myImgRight, "right", BdvOptions.options().axisOrder(AxisOrder.XYCZT)
                .doubleBuffered(false)
                .addTo(bdvss0)
                .transformEventHandlerFactory(new BdvTransformEventHandler.BehaviourTransformEventHandler3DFactory( voxelSize )));



    }

}
