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
package com.splunk.shuttl.archiver.usecases;

import static com.splunk.shuttl.testutil.TUtilsFile.*;
import static com.splunk.shuttl.testutil.TUtilsFunctional.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.splunk.shuttl.archiver.LocalFileSystemPaths;
import com.splunk.shuttl.archiver.archive.ArchiveConfiguration;
import com.splunk.shuttl.archiver.archive.BucketArchiver;
import com.splunk.shuttl.archiver.archive.BucketArchiverFactory;
import com.splunk.shuttl.archiver.flush.Flusher;
import com.splunk.shuttl.archiver.listers.ArchivedIndexesListerFactory;
import com.splunk.shuttl.archiver.model.Bucket;
import com.splunk.shuttl.archiver.model.IllegalIndexException;
import com.splunk.shuttl.archiver.thaw.BucketThawer;
import com.splunk.shuttl.archiver.thaw.BucketThawerFactory;
import com.splunk.shuttl.archiver.thaw.SplunkSettings;
import com.splunk.shuttl.testutil.TUtilsBucket;
import com.splunk.shuttl.testutil.TUtilsDate;
import com.splunk.shuttl.testutil.TUtilsFunctional;

@Test(groups = { "functional" })
public class FlusherFunctionalTest {

	private SplunkSettings splunkSettings;
	private LocalFileSystemPaths localFileSystemPaths;
	private ArchiveConfiguration config;
	private String index;
	private File thawDir;
	private File tmp;

	@BeforeMethod
	public void setUp() throws IllegalIndexException {
		index = "foo";

		config = TUtilsFunctional.getLocalFileSystemConfiguration();
		tmp = createDirectory();
		localFileSystemPaths = new LocalFileSystemPaths(tmp.getAbsolutePath());

		splunkSettings = mock(SplunkSettings.class);
		thawDir = createDirectory();
		when(splunkSettings.getThawLocation(index)).thenReturn(thawDir);
	}

	@AfterMethod
	public void tearDown() {
		FileUtils.deleteQuietly(tmp);
		FileUtils.deleteQuietly(thawDir);
	}

	public void _archiveTwoBuckets_thawThem_flushOneOfThem()
			throws IllegalIndexException {
		Date early = TUtilsDate.getNowWithoutMillis();
		Date later = TUtilsDate.getLaterDate(early);

		Bucket b1 = TUtilsBucket.createBucketWithIndexAndTimeRange(index, early,
				early);
		Bucket b2 = TUtilsBucket.createBucketWithIndexAndTimeRange(index, later,
				later);

		archiveBuckets(b1, b2);
		thawBuckets(early, later);

		assertArchivingAndThawingWasSuccessful(b1, b2);

		Flusher flusher = new Flusher(splunkSettings,
				ArchivedIndexesListerFactory.create(config));
		flusher.flush(index, later, later);

		List<Bucket> flushedBuckets = flusher.getFlushedBuckets();
		assertEquals(1, flushedBuckets.size());
		Bucket flushedBucket = flushedBuckets.get(0);
		assertEquals(b2.getName(), flushedBucket.getName());
		assertEquals(b2.getIndex(), flushedBucket.getIndex());
	}

	private void thawBuckets(Date early, Date later) {
		BucketThawer bucketThawer = BucketThawerFactory
				.createWithConfigAndSplunkSettingsAndLocalFileSystemPaths(config,
						splunkSettings, localFileSystemPaths);

		bucketThawer.thawBuckets(index, early, later);
	}

	private void archiveBuckets(Bucket b1, Bucket b2) {
		BucketArchiver bucketArchiver = BucketArchiverFactory
				.createWithConfiguration(config, localFileSystemPaths);

		archiveBucket(b1, bucketArchiver);
		archiveBucket(b2, bucketArchiver);
	}

	private void assertArchivingAndThawingWasSuccessful(Bucket b1, Bucket b2) {
		List<String> dirsInThaw = new java.util.ArrayList<String>();
		for (File f : thawDir.listFiles())
			dirsInThaw.add(f.getName());

		assertTrue(dirsInThaw.contains(b1.getName()));
		assertTrue(dirsInThaw.contains(b2.getName()));
	}

}
