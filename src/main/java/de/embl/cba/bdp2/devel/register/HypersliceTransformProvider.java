package de.embl.cba.bdp2.devel.register;

import net.imglib2.realtransform.AffineTransform;

public interface HypersliceTransformProvider
{
	AffineTransform getTransform( long slice );

	boolean wasStopped();
}
