package de.embl.cba.bdp2.scijava.convert;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.ImageService;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.convert.AbstractConverter;
import org.scijava.plugin.Plugin;

@Plugin(type = org.scijava.convert.Converter.class)
public class StringToImage<I extends String, O extends Image > extends AbstractConverter<I, O> {

    @Override
    public <T> T convert(Object src, Class<T> dest) {
        return (T) ImageService.imageNameToImage.get( src );
    }

    @Override
    public Class<O> getOutputType() {
        return (Class<O>) Image.class;
    }

    @Override
    public Class<I> getInputType() {
        return (Class<I>) String.class;
    }

    public < R extends RealType< R > & NativeType< R > > Image< R > getImage( String imageName )
    {
        Image< R > image = new StringToImage< String, Image< R > >().convert( imageName, Image.class );
        return image;
    }

}
