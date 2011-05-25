package org.springframework.samples.springtz;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.commons.compress.compressors.CompressorException;
import org.junit.Test;

public class TarGzDecompressorTest {

	@Test
	public void testDecompress() throws CompressorException, IOException,
			URISyntaxException {
		TarGzDecompressor decompressor = new TarGzDecompressor();
		Collection<File> decompressedFiles = decompressor.decompress(new File(
				this.getClass().getResource("tzdata2011g.tar.gz").toURI()));
		assertEquals(19, decompressedFiles.size());
		// Tidy up
		for (File decompressedFile : decompressedFiles) {
			decompressedFile.delete();
		}
	}
}
