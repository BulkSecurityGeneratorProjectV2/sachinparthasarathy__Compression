
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import chunkedcompression.CompressionBase;
import chunkedcompression.DecompressionBase;
import chunkedcompression.zip.ZipCompression;
import chunkedcompression.zip.ZipDecompression;

public class TestCompressDecompress {

	private void empty(File folder) throws IOException {
		 File[] files = folder.listFiles();
		    if(files!=null) {
		        for(File f: files) {
		            if(f.isDirectory()) {
		            	empty(f);
		            } else {
		                f.delete();
		            }
		        }
		    }
		    folder.delete();
	}

	public void veriyEquals(String inputPath,String outputPath,
			String decompressOutputPath,int maxSplitSize) throws IOException {

		File f = new File(outputPath);
		empty(f);
		f.mkdirs();
		
		f = new File(decompressOutputPath);
		empty(f);
		f.mkdirs();

		CompressionBase compressionAlgorithm = new ZipCompression();
		compressionAlgorithm.run(inputPath,outputPath,maxSplitSize);

		DecompressionBase decompressionAlgorithm = new ZipDecompression();
		decompressionAlgorithm.run(outputPath,decompressOutputPath);		
		boolean equals = EnumerateAndCompare(inputPath,decompressOutputPath); 
		assertEquals(equals, true);
	}

	@Test
	public void test1() throws IOException {   
		String inputPath = new File("test/resources/input/test1").getAbsolutePath();
		String outputPath = System.getProperty("java.io.tmpdir")
				+ File.separator + "zipOutput1";
		String decompressOutputPath = System.getProperty("java.io.tmpdir")
				+ File.separator + "decompressOutput1";
		int maxSplitSize = 2;

		veriyEquals(inputPath, outputPath, decompressOutputPath, maxSplitSize);		
	}
	
	@Test
	public void test2() throws IOException {   
		String inputPath = new File("test/resources/input/test2").getAbsolutePath();
		String outputPath = System.getProperty("java.io.tmpdir")
				+ File.separator + "zipOutput2";
		String decompressOutputPath = System.getProperty("java.io.tmpdir")
				+ File.separator + "decompressOutput2";
		int maxSplitSize = 2;

		veriyEquals(inputPath, outputPath, decompressOutputPath, maxSplitSize);		
	}

	private boolean EnumerateAndCompare(String dir1, String dir2) throws IOException
	{
		boolean isCompare = true;
		File[] fileList1 = new File(dir1).listFiles();
		File[] fileList2 = new File(dir2).listFiles();

		Arrays.sort(fileList1);
		Arrays.sort(fileList2);

		if(fileList1.length != fileList2.length)
			return false;

		for(int i = 0; i<fileList1.length; i++)
		{
			if((fileList1[i].isDirectory() && !fileList2[i].isDirectory()) ||(!fileList1[i].isDirectory() && fileList2[i].isDirectory()))
				return false;
			if (true == fileList1[i].isDirectory())
			{
				isCompare &= EnumerateAndCompare(fileList1[i].getAbsolutePath(), fileList2[i].getAbsolutePath());
			}
			else
				isCompare &= compare(fileList1[i], fileList2[i]);
		}

		return isCompare;
	}

	private final boolean compare(final File file1, final File file2) throws IOException {
		
		Path filea = Paths.get(file1.getAbsolutePath());
		Path fileb = Paths.get(file2.getAbsolutePath());
		
	    if (Files.size(filea) != Files.size(fileb)) {
	        return false;
	    }
	    final long size = Files.size(filea);
	    final int mapspan = 4 * 1024 * 1024;
	    try (FileChannel chana = (FileChannel)Files.newByteChannel(filea);
	            FileChannel chanb = (FileChannel)Files.newByteChannel(fileb)) {
	        for (long position = 0; position < size; position += mapspan) {
	            MappedByteBuffer mba = mapChannel(chana, position, size, mapspan);
	            MappedByteBuffer mbb = mapChannel(chanb, position, size, mapspan);

	            if (mba.compareTo(mbb) != 0) {
	                return false;
	            }
	        }
	    }
	    return true;
	}
	
	private MappedByteBuffer mapChannel(FileChannel channel, long position, long size, int mapspan) throws IOException {
	    final long end = Math.min(size, position + mapspan);
	    final long maplen = (int)(end - position);
	    return channel.map(MapMode.READ_ONLY, position, maplen);
	}
	
	public static void main(String[] args) throws Exception {                    
	       JUnitCore.main(
	         "TestCompressDecompress");
	}
}