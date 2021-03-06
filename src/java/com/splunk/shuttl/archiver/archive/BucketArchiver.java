// Copyright (C) 2011 Splunk Inc.
//
// Splunk Inc. licenses this file
// to you under the Apache License, Version 2.0 (the
// License); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an AS IS BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.splunk.shuttl.archiver.archive;

import java.util.List;

import com.splunk.shuttl.archiver.filesystem.ArchiveFileSystem;
import com.splunk.shuttl.archiver.importexport.BucketExporter;
import com.splunk.shuttl.archiver.model.Bucket;

/**
 * Archives buckets the way that it is configured to archive them.
 */
public class BucketArchiver {

	private final BucketExporter bucketExporter;
	private final ArchiveBucketTransferer archiveBucketTransferer;
	private final BucketDeleter bucketDeleter;
	private final List<BucketFormat> bucketFormats;

	/**
	 * Constructor following dependency injection pattern, makes it easier to
	 * test.<br/>
	 * Use {@link BucketArchiverFactory} for creating a {@link BucketArchiver}.
	 * 
	 * @param exporter
	 *          to export the bucket
	 * @param archiveBucketTransferer
	 *          to transfer the bucket to an {@link ArchiveFileSystem}
	 * @param bucketDeleter
	 *          that deletesBuckets that has been archived.
	 * @param bucketFormats
	 *          the formats to archive the bucket in.
	 */
	public BucketArchiver(BucketExporter exporter,
			ArchiveBucketTransferer archiveBucketTransferer,
			BucketDeleter bucketDeleter, List<BucketFormat> bucketFormats) {
		this.bucketExporter = exporter;
		this.archiveBucketTransferer = archiveBucketTransferer;
		this.bucketDeleter = bucketDeleter;
		this.bucketFormats = bucketFormats;
	}

	public void archiveBucket(Bucket bucket) {
		boolean successfullyArchivedAllFormats = true;
		for (BucketFormat format : bucketFormats)
			if (!archiveBucketTransferer.isArchived(bucket, format))
				if (!isSuccessfulArchiving(bucket, format))
					successfullyArchivedAllFormats = false;

		if (successfullyArchivedAllFormats)
			bucketDeleter.deleteBucket(bucket);
	}

	private boolean isSuccessfulArchiving(Bucket bucket, BucketFormat format) {
		Bucket exportedBucket = bucketExporter.exportBucket(bucket, format);
		try {
			archiveBucketTransferer.transferBucketToArchive(exportedBucket);
			return true;
		} catch (FailedToArchiveBucketException e) {
			return false;
		} finally {
			if (!bucket.equals(exportedBucket))
				bucketDeleter.deleteBucket(exportedBucket);
		}
	}
}
