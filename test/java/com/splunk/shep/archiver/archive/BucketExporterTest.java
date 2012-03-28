package com.splunk.shep.archiver.archive;

import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.splunk.shep.archiver.model.Bucket;

@Test(groups = { "fast-unit" })
public class BucketExporterTest {

    private BucketExporter bucketExporter;

    @BeforeMethod(groups = { "fast-unit" })
    public void setUp() {
	bucketExporter = new BucketExporter();
    }

    @Test(groups = { "fast" })
    public void getBucketExportedToFormat_whenBucketIsAlreadyInThatFormat_returnTheSameBucket() {
	BucketFormat format = BucketFormat.SPLUNK_BUCKET;
	Bucket bucket = mock(Bucket.class);
	when(bucket.getFormat()).thenReturn(format);
	Bucket exportedToFormat = bucketExporter.getBucketExportedToFormat(
		bucket, format);
	assertEquals(bucket, exportedToFormat);
    }

    @Test(expectedExceptions = { UnknownBucketFormatException.class })
    public void getBucketExportedToFormat_formatIsUnknown_throwUnknownBucketFormatException() {
	Bucket bucket = mock(Bucket.class);
	bucketExporter.getBucketExportedToFormat(bucket, BucketFormat.UNKNOWN);
    }
}
