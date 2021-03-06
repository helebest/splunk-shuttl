// Copyright (C) 2011 Splunk Inc.
//
// Splunk Inc. licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.splunk.shuttl.archiver.archive;

import com.splunk.shuttl.archiver.LocalFileSystemPaths;
import com.splunk.shuttl.archiver.bucketsize.ArchiveBucketSize;
import com.splunk.shuttl.archiver.bucketsize.BucketSizeIO;
import com.splunk.shuttl.archiver.filesystem.ArchiveFileSystem;
import com.splunk.shuttl.archiver.filesystem.ArchiveFileSystemFactory;
import com.splunk.shuttl.archiver.importexport.BucketExporter;
import com.splunk.shuttl.archiver.importexport.csv.CsvExporter;

/**
 * Construction code for creating BucketArchivers that archives in different
 * FileSystems.
 */
public class BucketArchiverFactory {

	/**
	 * @return {@link BucketArchiver} as configured in .conf files.
	 */
	public static BucketArchiver createConfiguredArchiver() {
		ArchiveConfiguration config = ArchiveConfiguration.getSharedInstance();
		return createWithConfiguration(config, LocalFileSystemPaths.create());
	}

	/**
	 * Testability with specified configuration.
	 */
	public static BucketArchiver createWithConfiguration(
			ArchiveConfiguration config, LocalFileSystemPaths localFileSystemPaths) {
		ArchiveFileSystem archiveFileSystem = ArchiveFileSystemFactory
				.getWithConfiguration(config);
		return createWithConfFileSystemAndCsvDirectory(config, archiveFileSystem,
				localFileSystemPaths);
	}

	/**
	 * @param config
	 *          for configuring the archiver
	 * @param archiveFileSystem
	 *          for storing the files
	 * @param csvDirectory
	 *          for storing csv exports.
	 * @return a {@link BucketArchiver} to archive with the specified
	 *         configuration.
	 */
	public static BucketArchiver createWithConfFileSystemAndCsvDirectory(
			ArchiveConfiguration config, ArchiveFileSystem archiveFileSystem,
			LocalFileSystemPaths localFileSystemPaths) {
		CsvExporter csvExporter = CsvExporter.create(localFileSystemPaths
				.getCsvDirectory());
		PathResolver pathResolver = new PathResolver(config);
		BucketSizeIO bucketSizeIO = new BucketSizeIO(archiveFileSystem,
				localFileSystemPaths);
		ArchiveBucketSize archiveBucketSize = new ArchiveBucketSize(pathResolver,
				bucketSizeIO, archiveFileSystem);
		return new BucketArchiver(BucketExporter.create(csvExporter),
				new ArchiveBucketTransferer(archiveFileSystem, pathResolver,
						archiveBucketSize), BucketDeleter.create(),
				config.getArchiveFormats());

	}
}
