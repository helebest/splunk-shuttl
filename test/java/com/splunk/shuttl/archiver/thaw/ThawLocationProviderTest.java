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
package com.splunk.shuttl.archiver.thaw;

import static com.splunk.shuttl.testutil.TUtilsFile.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.splunk.shuttl.archiver.model.Bucket;
import com.splunk.shuttl.archiver.model.IllegalIndexException;
import com.splunk.shuttl.archiver.thaw.SplunkSettings;
import com.splunk.shuttl.archiver.thaw.ThawLocationProvider;
import com.splunk.shuttl.testutil.TUtilsBucket;

@Test(groups = { "fast-unit" })
public class ThawLocationProviderTest {

    ThawLocationProvider thawLocationProvider;
    SplunkSettings splunkSettings;
    Bucket bucket;
    File thawLocation;

    @BeforeMethod
    public void setUp() throws IllegalIndexException {
	bucket = TUtilsBucket.createTestBucket();
	splunkSettings = mock(SplunkSettings.class);
	thawLocationProvider = new ThawLocationProvider(splunkSettings);

	stubSplunkSettingsToReturnThawLocation();
    }

    private void stubSplunkSettingsToReturnThawLocation()
	    throws IllegalIndexException {
	thawLocation = createTestFilePath();
	when(splunkSettings.getThawLocation(bucket.getIndex())).thenReturn(
		thawLocation);
    }

    @Test(groups = { "fast-unit" })
    public void getLocationInThawForBucket_givenThawLocation_returnedBucketDirectorysParentIsThawLocation()
	    throws IOException {
	File locationInThawDirectory = thawLocationProvider
		.getLocationInThawForBucket(bucket);
	assertEquals(thawLocation.getAbsolutePath(), locationInThawDirectory
		.getParentFile().getAbsolutePath());
    }

    public void getLocationInThawForBucket_givenThawLocation_directoryHasNameOfBucket()
	    throws IOException {
	File bucketsLocation = thawLocationProvider
		.getLocationInThawForBucket(bucket);
	assertEquals(bucket.getName(), bucketsLocation.getName());
    }
}
