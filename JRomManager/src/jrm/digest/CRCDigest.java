package jrm.digest;

import java.util.zip.CRC32;

class CRCDigest extends MDigest
{
	private CRC32 crc = new CRC32();
	
	@Override
	public void update(byte[] input, int offset, int len)
	{
		crc.update(input, offset, len);
	}
	
	@Override
	public String toString()
	{
		return String.format("%08x", crc.getValue());
	}

	@Override
	public String getAlgorithm()
	{
		return "CRC";
	}

	@Override
	public void reset()
	{
		crc.reset();
	}
	
}