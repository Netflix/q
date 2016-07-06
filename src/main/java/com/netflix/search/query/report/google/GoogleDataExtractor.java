package com.netflix.search.query.report.google;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.netflix.search.query.Properties;
import com.netflix.search.query.input.TitleWithQueries;
import com.netflix.search.query.report.Report;
import com.netflix.search.query.report.detail.DetailReport;
import com.netflix.search.query.report.summary.SummaryReport;
import com.netflix.search.query.utils.HeaderUtils;

public class GoogleDataExtractor {
    public static final Logger logger = LoggerFactory.getLogger(GoogleDataExtractor.class);

    private static final String ENCODING = "UTF-8";
	private Map<String, Map<Integer, TitleWithQueries>> titlesWithQueriesPerDataset = Maps.newLinkedHashMap();
    private DetailReport previousDetailReport = new DetailReport();
    private SummaryReport previousSummaryReport = new SummaryReport();

    private GoogleSheetsService searchGoogleSheetsService = null;

    public GoogleDataExtractor() {
        try {
            initExtractor();
        } catch (Throwable e) {
            logger.error("Error trying to init the GoogleDataExtractor", e);
        }
    }

    public Map<String, Map<Integer, TitleWithQueries>> getTitlesWithQueriesPerDataset()
    {
        return titlesWithQueriesPerDataset;
    }

    public DetailReport getPreviousDetailReport()
    {
        return previousDetailReport;
    }
    
    public SummaryReport getPreviousSummaryReport()
    {
        return previousSummaryReport;
    }

    public void initExtractor() throws Throwable
    {
        searchGoogleSheetsService = new GoogleSheetsService();
        for (String sheetId : Properties.validDataSetsId.get()) {
            logger.info("Initializing and Downloading: " + sheetId);
            Map<Integer, TitleWithQueries> titlesWithQueries = searchGoogleSheetsService.extractTitlesWithQueries(sheetId);
            titlesWithQueriesPerDataset.put(sheetId, titlesWithQueries);
            List<String> titlesWithQueriesAsTsv = searchGoogleSheetsService.getTitlesWithQueriesAsTsv(sheetId);
            writeReportToLocalDisk(sheetId, titlesWithQueriesAsTsv);
        }
        List<String> previousSummaryReportAsTsv = searchGoogleSheetsService.getLatestSummaryReportAsTsv();
        writeReportToLocalDisk("summary_previous", previousSummaryReportAsTsv);
        searchGoogleSheetsService.extractReport(previousSummaryReport, false);
        logger.info("Initializing and Downloading: " + previousSummaryReport);


        List<String> previousDetailReportAsTsv = searchGoogleSheetsService.getLatestDetailReportAsTsv();
        writeReportToLocalDisk("details_previous", previousDetailReportAsTsv);
        searchGoogleSheetsService.extractReport(previousDetailReport, true);
        logger.info("Initializing and Downloading: " + previousDetailReport);


    }

    private void writeReportToLocalDisk(String sheetId, List<String> titlesWithQueries) throws Throwable
    {
        if (titlesWithQueries != null) {
            File file = new File(Properties.dataDir.get() + sheetId+".tsv");
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
    
    
    public void writeTitleQueriesToLocalDisk(String sheetId, Map<Integer, TitleWithQueries> titlesWithQueries) throws Throwable
    {
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


    public void publishReportToGoogleSpreadsheet(Report report) throws Throwable
    {
        searchGoogleSheetsService.updateReport(report.reportNameForUpload(), HeaderUtils.getHeader(report.getReportType()), report.getItems(), (report instanceof DetailReport ? true : false));
    }
    
}
