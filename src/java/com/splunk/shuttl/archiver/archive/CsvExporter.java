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

import static com.splunk.shuttl.archiver.LocalFileSystemConstants.*;
import static com.splunk.shuttl.archiver.LogFormatter.*;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;

import com.splunk.shuttl.archiver.model.Bucket;

/**
 * Exports a bucket to .csv format.
 */
public class CsvExporter {

    private static final Logger logger = Logger.getLogger(CsvExporter.class);

    private final SplunkExportTool exportTool;
    private final GetsBucketsCsvFile getsBucketsCsvFile;
    private final ShellExecutor shellExecutor;

    public CsvExporter(SplunkExportTool exportTool,
	    GetsBucketsCsvFile getsBucketsCsvFile, ShellExecutor shellExecutor) {
	this.exportTool = exportTool;
	this.getsBucketsCsvFile = getsBucketsCsvFile;
	this.shellExecutor = shellExecutor;
    }

    /**
     * @return Csv file of the {@link Bucket}
     */
    public File exportBucketToCsv(Bucket bucket) {
	File csvFile = getsBucketsCsvFile.getCsvFile(bucket);
	String[] command = constructCommand(bucket, csvFile);
	Map<String, String> env = exportTool.getEnvironmentVariables();
	int exit = shellExecutor.executeCommand(env, command);
	throwCsvExceptionIfExportFailed(csvFile, exit, command);
	return csvFile;
    }

    private String[] constructCommand(Bucket bucket, File csvFile) {
	String exportToolCommand = exportTool.getExecutableCommand();
	String[] command = new String[] { exportToolCommand,
		bucket.getDirectory().getAbsolutePath(),
		csvFile.getAbsolutePath(), "-csv" };
	return command;
    }

    private void throwCsvExceptionIfExportFailed(File csvFile, int exit,
	    String[] command) {
	if (exit != 0) {
	    logger.debug(did("Exported a bucket to Csv",
		    "Got a non zero exit code from export tool",
		    "Zero exit code from export tool.", "exit_code", exit,
		    "csv_file", csvFile, "command", Arrays.toString(command)));
	    throw new CsvExportFailedException("Exporttool exited with"
		    + " non zero exit status. Ran exporttool with command: "
		    + Arrays.toString(command));
	} else if (!csvFile.exists()) {
	    logger.debug(did("Exported a bucket to Csv",
		    "Csv file didn't exist after the export",
		    "The csv file to exist", "csv_file", csvFile));
	    throw new CsvExportFailedException("Csv file didn't exist after "
		    + "the export. Something went wrong.");
	}
    }

    /**
     * @return a CsvExporter
     */
    public static CsvExporter create() {
	return new CsvExporter(new SplunkExportTool(), new GetsBucketsCsvFile(
		getCsvDirectory()), ShellExecutor.getInstance());
    }

}
