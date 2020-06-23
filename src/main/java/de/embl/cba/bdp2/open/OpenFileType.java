package de.embl.cba.bdp2.open;

public enum OpenFileType
{
	HDF5("Hdf5"),
	LUXENDO("Luxendo Hdf5"),
	IMARIS("Partitioned Imaris Hdf5"),
	TIFF_STACKS("Tiff stacks"),
	TIFF_PLANES("Tiff planes");

	private final String text;
	private OpenFileType( String s)
{
text = s;
}
	@Override
	public String toString() {
return text;
}
}
