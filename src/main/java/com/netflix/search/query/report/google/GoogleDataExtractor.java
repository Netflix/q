/**
 * Copyright 2016 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.search.query.report.google;

import java.io.*;
import java.util.List;
import java.util.Map;

import com.netflix.search.query.utils.HeaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.netflix.search.query.Properties;
import com.netflix.search.query.input.TitleWithQueries;
import com.netflix.search.query.report.Report;
import com.netflix.search.query.report.detail.DetailReport;
import com.netflix.search.query.report.summary.SummaryReport;


public class GoogleDataExtractor {
    public static final Logger logger = LoggerFactory.getLogger(GoogleDataExtractor.class);

    private static final String ENCODING = "UTF-8";
    private Map<String, Map<Integer, TitleWithQueries>> titlesWithQueriesPerDataset = Maps.newLinkedHashMap();
    private Report previousDetailReport = new DetailReport();
    private Report previousSummaryReport = new SummaryReport();

    private GoogleSheetsService searchGoogleSheetsService = null;

    public GoogleDataExtractor() {
        super();
    }

    public Map<String, Map<Integer, TitleWithQueries>> getTitlesWithQueriesPerDataset() {
        return titlesWithQueriesPerDataset;
    }

    public Report getPreviousDetailReport() {
        return previousDetailReport;
    }

    public Report getPreviousSummaryReport() {
        return previousSummaryReport;
    }

    public static void main(String[] args) {
        GoogleDataExtractor s = new GoogleDataExtractor();
    }

    public void initExtractor() {
        searchGoogleSheetsService = new GoogleSheetsService();
    }

    public void setReportNamesAndDownloadData() throws Throwable {
        searchGoogleSheetsService.setUpReportNames();
        downloadQueries();
        downloadReports();
    }

    public void downloadQueries() throws Throwable {
        for (String sheetId : Properties.validDataSetsId.get()) {
            logger.info("Initializing and Downloading: " + sheetId);
            List<List<Object>> spreadsheetData = searchGoogleSheetsService.getSpreadsheetDataForQueries(sheetId);
            if (spreadsheetData != null && spreadsheetData.size() != 0) {
                Map<Integer, TitleWithQueries> titlesWithQueries = searchGoogleSheetsService.getTitlesWithQueries(spreadsheetData, sheetId);
                titlesWithQueriesPerDataset.put(sheetId, titlesWithQueries);
                List<String> titlesWithQueriesAsTsv = searchGoogleSheetsService.extractWorksheetData(spreadsheetData, null);
                writeReportToLocalDisk(sheetId, titlesWithQueriesAsTsv);
            } else {
                logger.info("Sheet doesn't exist or it is empty: " + sheetId);
            }
            if (Properties.googleApiThrottlePause.get() > 0) Thread.sleep(Properties.googleApiThrottlePause.get());
        }
    }

    public void downloadReports() throws Throwable {
        previousSummaryReport = searchGoogleSheetsService.extractReport(false);
        logger.info("Initializing and Downloading: " + previousSummaryReport);
        List<String> previousSummaryReportAsTsv = searchGoogleSheetsService.getLatestSummaryReportAsTsv(previousSummaryReport);
        writeReportToLocalDisk("summary_previous", previousSummaryReportAsTsv);

        previousDetailReport = searchGoogleSheetsService.extractReport(true);
        logger.info("Initializing and Downloading: " + previousDetailReport);
        List<String> previousDetailReportAsTsv = searchGoogleSheetsService.getLatestDetailReportAsTsv(previousDetailReport);
        writeReportToLocalDisk("details_previous", previousDetailReportAsTsv);
    }

    private void writeReportToLocalDisk(String sheetId, List<String> titlesWithQueries) throws Throwable {
        if (titlesWithQueries != null) {
            File file = new File(Properties.dataDir.get() + sheetId + ".tsv");
            OutputStream out = new FileOutputStream(file);
            Writer writer = new OutputStreamWriter(out, ENCODING);
            for (String t : titlesWithQueries) {
                writer.write(t);
                writer.write("\n");
            }
            writer.close();
            out.close();
        }
    }


    public void writeTitleQueriesToLocalDisk(String sheetId, Map<Integer, TitleWithQueries> titlesWithQueries) throws Throwable {
        if (titlesWithQueries != null) {
            File file = new File(Properties.dataDir.get() + sheetId);
            OutputStream out = new FileOutputStream(file);
            Writer writer = new OutputStreamWriter(out, ENCODING);
            for (TitleWithQueries t : titlesWithQueries.values()) {
                writer.write(t.toString());
                writer.write("\n");
            }
            writer.close();
            out.close();
        }
    }

    public void publishReportToGoogleSpreadsheet(Report report) throws Throwable {
        searchGoogleSheetsService.updateReport(report.reportNameForUpload(), HeaderUtils.getHeader(report.getReportType()), report.getItems(), (report instanceof DetailReport ? true : false));
    }

}
