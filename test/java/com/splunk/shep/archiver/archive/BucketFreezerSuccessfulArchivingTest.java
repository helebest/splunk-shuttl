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
package com.splunk.shep.archiver.archive;

import static com.splunk.shep.testutil.UtilsFile.createTempDirectory;
import static com.splunk.shep.testutil.UtilsFile.isDirectoryEmpty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.splunk.shep.archiver.archive.recovery.FailedBucketRestorer;
import com.splunk.shep.archiver.model.Bucket;
import com.splunk.shep.testutil.UtilsBucket;
import com.splunk.shep.testutil.UtilsTestNG;

/**
 * Fixture: HttpClient returns HttpStatus codes that represent successful
 * archiving.
 */
@Test(groups = { "fast" })
public class BucketFreezerSuccessfulArchivingTest {

    File tempTestDirectory;
    BucketFreezer bucketFreezer;
    ArchiveRestHandler archiveRestHandler;
    FailedBucketRestorer failedBucketRestorer;

    @BeforeMethod(groups = { "fast" })
    public void beforeClass() throws ClientProtocolException, IOException {
	tempTestDirectory = createTempDirectory();
	archiveRestHandler = mock(ArchiveRestHandler.class);
	failedBucketRestorer = mock(FailedBucketRestorer.class);
	bucketFreezer = new BucketFreezer(getSafeLocationPath(),
		archiveRestHandler, failedBucketRestorer);
    }

    @AfterMethod(groups = { "fast" })
    public void tearDownFast() {
	FileUtils.deleteQuietly(tempTestDirectory);
    }

    /**
     * This location is torn down by the AfterMethod annotation.
     */
    private String getSafeLocationPath() {
	return tempTestDirectory.getAbsolutePath();
    }

    public void should_moveDirectoryToaSafeLocation_when_givenPath()
	    throws IOException {
	File dirToBeMoved = createTempDirectory();
	File safeLocationDirectory = tempTestDirectory;
	assertTrue(isDirectoryEmpty(safeLocationDirectory));

	// Test
	int exitStatus = bucketFreezer.freezeBucket("index-name",
		dirToBeMoved.getAbsolutePath());
	assertEquals(0, exitStatus);

	// Verify
	assertTrue(!dirToBeMoved.exists());
	assertTrue(safeLocationDirectory.exists());
	assertTrue(!isDirectoryEmpty(safeLocationDirectory));
    }

    public void freezeBucket_givenNonExistingSafeLocation_createSafeLocation()
	    throws IOException {
	File dirToBeMoved = createTempDirectory();

	assertTrue(FileUtils.deleteQuietly(tempTestDirectory));
	File nonExistingSafeLocation = tempTestDirectory;
	assertTrue(!nonExistingSafeLocation.exists());

	// Test
	bucketFreezer.freezeBucket("index", dirToBeMoved.getAbsolutePath());

	// Verify
	assertTrue(!dirToBeMoved.exists());
	assertTrue(nonExistingSafeLocation.exists());
    }

    public void freezeBucket_givenBucket_callRestWithMovedBucket() {
	Bucket bucket = UtilsBucket.createTestBucket();

	bucketFreezer.freezeBucket(bucket.getIndex(), bucket.getDirectory()
		.getAbsolutePath());

	ArgumentCaptor<Bucket> bucketCaptor = ArgumentCaptor
		.forClass(Bucket.class);
	verify(archiveRestHandler, times(1)).callRestToArchiveBucket(
		bucketCaptor.capture());
	assertEquals(1, bucketCaptor.getAllValues().size());
	Bucket capturedBucket = bucketCaptor.getValue();

	UtilsTestNG.assertBucketsGotSameIndexFormatAndName(bucket,
		capturedBucket);
    }

    public void freezeBucket_givenFailedBucketRecovery_callItToRecoverBuckets() {
	Bucket bucket = UtilsBucket.createTestBucket();
	bucketFreezer.freezeBucket(bucket.getIndex(), bucket.getDirectory()
		.getAbsolutePath());
	verify(failedBucketRestorer).recoverFailedBuckets(archiveRestHandler);
    }
}
