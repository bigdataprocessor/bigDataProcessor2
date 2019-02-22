package de.embl.cba.bdp2;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.embl.cba.bdp2.ui.BigDataProcessor;
import de.embl.cba.bdp2.fileinfosource.FileInfoSource;
import de.embl.cba.bdp2.fileinfosource.SerializableFileInfo;
import ij.ImagePlus;
import javafx.geometry.Point3D;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.embl.cba.bdp2.fileinfosource.FileInfoConstants.*;
import static de.embl.cba.bdp2.fileinfosource.FileInfoConstants.Z;


public class ImageLoader implements CellLoader {

    private String directory;
    private long[] dimensions;
    private int[] cellDims;
    private LoadingCache<List<Integer>, SerializableFileInfo[]> serializableFileInfoCache;

    public ImageLoader( FileInfoSource infoSource ) {
        this.cellDims = new int[]{ infoSource.nX, CELL_DIM_Y, 1, 1, 1};
        this.dimensions = infoSource.getDimensions();
        this.directory = infoSource.directory;
        //Google Guava cache
        CacheLoader<List<Integer>, SerializableFileInfo[]> loader =
                new CacheLoader<List<Integer>, SerializableFileInfo[]>(){
                    @Override
                    public SerializableFileInfo[] load( List<Integer> c_t ){
                        return infoSource.getSerializableFileStackInfo( c_t.get(0), c_t.get(1) );
                    }
        };
        serializableFileInfoCache = CacheBuilder.newBuilder().maximumSize(50).build(loader);
    }


    public ImagePlus getDataCube( long[] min,  long[] max )
    {
        int z = Math.toIntExact( max[ Z ]);
        int channel = Math.toIntExact( max[ C ]);
        int time = Math.toIntExact( max[ T ]);
        List<Integer> c_t = Arrays.asList(channel,time);
        SerializableFileInfo[] infos_c_t = getFileInfoStack(c_t);
        SerializableFileInfo fileInfo = infos_c_t[z];
        Point3D po, ps;
        po = new Point3D(0, min[ Y ], z);
        long diff = max[ Y ]-min[ Y ];
        ps = new Point3D(fileInfo.width,diff+1 , 1);
        ImagePlus imagePlus = new OpenerExtension().readDataCube(directory, infos_c_t, 1, po, ps, BigDataProcessor.executorService); //TODO: get rid of ImagePlus
        return imagePlus;
    }

    private SerializableFileInfo[] getFileInfoStack(List<Integer> c_t) {
        SerializableFileInfo[] infos_c_t = null;
        try {
             infos_c_t = serializableFileInfoCache.get(c_t);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return infos_c_t;
    }

    @Override
    public synchronized void load( final SingleCellArrayImg cell ) {
        long[] max = new long[ TOTAL_AXES ];
        long[] min = new long[ TOTAL_AXES ];
        cell.max(max);
        cell.min(min);
        ImagePlus imagePlus = getDataCube( min, max );
        if (cell.firstElement() instanceof UnsignedByteType) {
            final byte[] impData = (byte[]) imagePlus.getProcessor().getPixels();
            final byte[] cellData = (byte[]) cell.getStorageArray();
            System.arraycopy(impData, 0, cellData, 0, cellData.length);
        } else if (cell.firstElement() instanceof UnsignedShortType) {
            final short[] impData = (short[]) imagePlus.getProcessor().getPixels();
            final short[] cellData = (short[]) cell.getStorageArray();
            System.arraycopy(impData, 0, cellData, 0, cellData.length);
        } else if (cell.firstElement() instanceof FloatType) {
            final float[] impData = (float[]) imagePlus.getProcessor().getPixels();
            final float[] cellData = (float[]) cell.getStorageArray();
            System.arraycopy(impData, 0, cellData, 0, cellData.length);
        }
    }

    public long[] getDimensions() {
        return dimensions;
    }

    public int[] getCellDims() {
        return cellDims;
    }

}
