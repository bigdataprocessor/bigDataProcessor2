package de.embl.cba.bdp2.save;

import net.imagej.DefaultDataset;
import net.imagej.ImgPlus;
import net.imagej.plugins.commands.imglib.Binner;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.converter.RealUnsignedShortConverter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.IntervalView;

public class SaveImgHelper
{

    public static <T extends RealType<T>> RandomAccessibleInterval convertor(RandomAccessibleInterval newRai, SavingSettings savingSettings){
        if (savingSettings.convertTo8Bit) {
            if (!(((IntervalView) newRai).firstElement() instanceof UnsignedByteType)){
                newRai = Converters.convert(newRai, new RealUnsignedByteConverter<T>(savingSettings.mapTo0,savingSettings.mapTo255), new UnsignedByteType());
            }
        }else if (savingSettings.convertTo16Bit) {
            if (!(((IntervalView) newRai).firstElement() instanceof UnsignedShortType)) {
                newRai = Converters.convert(newRai, new RealUnsignedShortConverter<T>(savingSettings.mapTo0,savingSettings.mapTo255), new UnsignedShortType()); //TODO : ashis
            }
        }
        return newRai;
    }

    public static<T extends RealType<T>> String doBinning(ImgPlus<T> impBinned,int[] binningA, String path, org.scijava.Context context){
        Binner binner = new Binner();
        DefaultDataset ds = new DefaultDataset(context,impBinned);
        binner.setDataset(ds);
        binner.setFactor(0,binningA[0]);
        binner.setFactor(1,binningA[1]);
        binner.setFactor(2,binningA[2]);
        binner.setValueMethod("Average");
        binner.run();
        ds= (DefaultDataset) binner.getDataset();
        impBinned =  (ImgPlus<T>)ds.getImgPlus();
        String newPath = path + "--bin-" + binningA[0] + "-" + binningA[1] + "-" + binningA[2];
        return newPath;
    }

}
