package de.embl.cba.bdp2.open.bioformats;

import bdv.viewer.Source;
import ch.epfl.biop.bdv.bioformats.BioFormatsMetaDataHelper;
import ch.epfl.biop.bdv.bioformats.bioformatssource.BioFormatsBdvOpener;
import ch.epfl.biop.bdv.bioformats.bioformatssource.BioFormatsBdvSource;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.CachedCellImgCreator;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BioFormatsCachedCellImgCreator < R extends RealType< R > & NativeType< R > > implements CachedCellImgCreator< R >
{
	RandomAccessibleInterval raiXYCZT;

	String imageName = "";

	long sizeX, sizeY, sizeZ;

	int sizeC, sizeT;

	int[] cacheSize;

	ARGBType[] channelColors;

	double[] voxelSize = new double[3];

	public BioFormatsCachedCellImgCreator(String dataLocation, int series ) {

		imageName = dataLocation+"_s"+series;

		List<Source> sources =
				BioFormatsBdvOpener.getOpener()
						.location(dataLocation)
						.auto() // patches opener based on specific file formats (-> PR to be  modified)
						//.splitRGBChannels() // split RGB channels into 3 channels
						//.switchZandC(true) // switch Z and C
						//.centerPositionConvention() // bioformats location is center of the image
						.cornerPositionConvention() // bioformats location is corner of the image
						//.useCacheBlockSizeFromBioFormats(true) // true by default
						//.cacheBlockSize(512,512,10) // size of cache block used by diskcached image
						//.micronmeter() // unit = micrometer
						.millimeter() // unit = millimeter
						//.unit(UNITS.YARD) // Ok, if you really want...
						//.getConcreteSources()
						.cacheBounded(100) // TODO : is this value ok ?
						.positionReferenceFrameLength(new Length(1, UNITS.MICROMETER)) // Compulsory
						.voxSizeReferenceFrameLength(new Length(100, UNITS.MICROMETER))
						.getConcreteSources(series+".*") // code for all channels of the series indexed 'series'
						.stream().map(src -> (Source) src).collect(Collectors.toList());


		List<BioFormatsBdvSource> sourcesBF = sources.stream().map(src ->
				BioFormatsBdvSource.class.cast( src )
		).collect(Collectors.toList());

		BioFormatsBdvSource modelSource = sourcesBF.get(0);
		RandomAccessibleInterval<R> modelRAI = sourcesBF.get(0).createSource(0,0);

		sizeX = modelRAI.dimension(0); // limited to 2GPixels in one dimension
		sizeY = modelRAI.dimension(1);
		sizeZ = modelRAI.dimension(2);
		sizeC = sourcesBF.size();
		sizeT = modelSource.numberOfTimePoints;

		channelColors = new ARGBType[sizeC];

		// TODO : sanity check identical size in XYZCT for all channels. Currently assuming selecting one series does the trick

		List<RandomAccessibleInterval<R>> raisXYZCT = new ArrayList<>();

		int[] cacheSizeXYZ = new int[3];

		for (int iTime = 0; iTime<sizeT;iTime++) {
			List<RandomAccessibleInterval<R>> raisXYZC = new ArrayList<>();
			for (int iChannel = 0; iChannel<sizeC;iChannel++) {
				BioFormatsBdvSource source = sourcesBF.get(iChannel);
				channelColors[iChannel] = BioFormatsMetaDataHelper.getSourceColor(source);
				raisXYZC.add(source.createSource(iTime,0));
				source.getVoxelDimensions().dimensions(voxelSize);
			}
			((CachedCellImg) raisXYZC.get(0)).getCellGrid().cellDimensions(cacheSizeXYZ);
			raisXYZCT.add(Views.stack(raisXYZC));
		}

		cacheSize = new int[]{cacheSizeXYZ[0], cacheSizeXYZ[1], cacheSizeXYZ[2],1,1};

		raiXYCZT = Views.stack(raisXYZCT);
	}

	@Override
	public String getImageName()
	{
		return imageName;
	}

	@Override
	public String[] getChannelNames()
	{
		String[] nameChannels = new String[sizeC];

		for (int iChannel = 0;iChannel<sizeC;iChannel++) {
			nameChannels[iChannel] = "channel_"+iChannel;
		}

		return nameChannels;
	}

	@Override
	public ARGBType[] getChannelColors()
	{
		ARGBType[] colorChannels = new ARGBType[sizeC];

		for (int iChannel = 0;iChannel<sizeC;iChannel++) {
			colorChannels[iChannel] = new ARGBType(ARGBType.rgba(255,255,255,128));//"channel_"+iChannel;
		}

		return colorChannels;
	}

	@Override
	public double[] getVoxelSize()
	{
		return voxelSize;
	}

	@Override
	public Unit< Length > getVoxelUnit()
	{
		return UNITS.MILLIMETER;
	}

	@Override
	public int[] getDefaultCellDimsXYZCT()
	{
		return cacheSize;
	}

	@Override
	public RandomAccessibleInterval< R > createCachedCellImg(int[] cellDimsXYZCT, DiskCachedCellImgOptions.CacheType cacheType, long cacheSize )
	{
		return raiXYCZT;
	}

	// TODO: this makes not much sense, just make it this Image
	public Image< R > createImage()
	{
		Image< R > image = new Image( this );

		return image;
	}
}
