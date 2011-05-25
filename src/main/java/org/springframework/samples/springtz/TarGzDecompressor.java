/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.springframework.samples.springtz;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 * Decompress a tar gz archive
 * 
 * @author Christopher Hunt
 * 
 */
public class TarGzDecompressor {
	/**
	 * Decompress the archive.
	 * 
	 * @param archive
	 *            the File representation of the archive.
	 * @return a collection of files extracted from the archive.
	 * @throws CompressorException
	 *             problem during decompression.
	 * @throws IOException
	 *             problem during decompression.
	 */
	public Collection<File> decompress(File archive)
			throws CompressorException, IOException {

		Collection<File> files = new ArrayList<File>();

		BufferedInputStream is = new BufferedInputStream(new FileInputStream(
				archive));
		CompressorInputStream gzippedIs = new CompressorStreamFactory()
				.createCompressorInputStream(CompressorStreamFactory.GZIP, is);
		TarArchiveInputStream tarIs = new TarArchiveInputStream(gzippedIs);

		try {
			boolean readTarEntry;
			do {
				TarArchiveEntry tarEntry = tarIs.getNextTarEntry();
				if (tarEntry != null) {
					File osFile = File.createTempFile(tarEntry.getName(), null);
					FileOutputStream os = new FileOutputStream(osFile);
					try {
						long tarEntrySize = tarEntry.getSize();
						long bytesRead = 0;
						int c;
						while ((c = tarIs.read()) != -1
								&& bytesRead < tarEntrySize) {

							++bytesRead;
							os.write(c);
						}

						files.add(osFile);

					} finally {
						try {
							os.close();
						} catch (IOException e) {
						}
					}
					readTarEntry = true;
				} else {
					readTarEntry = false;
				}
			} while (readTarEntry);

		} finally {
			try {
				tarIs.close();
			} catch (IOException e) {
			}
		}

		return files;
	}
}
