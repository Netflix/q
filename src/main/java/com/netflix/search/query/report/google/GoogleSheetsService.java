/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.search.query.report.google;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.google.gdata.client.batch.BatchInterruptedException;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;
import com.netflix.search.query.Properties;
import com.netflix.search.query.input.TitleWithQueries;
import com.netflix.search.query.report.Report;
import com.netflix.search.query.report.ReportItem;
import com.netflix.search.query.report.ReportType;
import com.netflix.search.query.report.detail.DetailReportItem;
import com.netflix.search.query.report.summary.SummaryReportItem;
import com.netflix.search.query.utils.DateUtil;
import com.netflix.search.query.utils.HeaderUtils;

public class GoogleSheetsService {
    public static final Logger logger = LoggerFactory.getLogger(GoogleSheetsService.class);

    private static final Pattern VALID_A1_PATTERN = Pattern.compile("([A-Z]+)([0-9]+)");

    private String summaryReportName = ""; 
    private String detailReportName = "";
    
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private static final FeedURLFactory FEED_URL_FACTORY = FeedURLFactory.getDefault();
    private static SpreadsheetService spreadsheetService = null;
    private static URL spreadsheetsFeedUrl = null;

    private DateUtil dateUtil = new DateUtil();

    public GoogleSheetsService() {
        super();
        try {
            this.summaryReportName =  Properties.sumReportSheet.get();
            this.detailReportName =  Properties.detailReportSheet.get();
            if(Properties.isDevOnlyTest.get()){
                this.summaryReportName +=Properties.devSpreadsheetSuffix.get(); 
                this.detailReportName +=Properties.devSpreadsheetSuffix.get();
            }
            initSpreadsheetService();
        } catch (Throwable e) {
            logger.error("Error trying to init the GoogleSheetsService", e);
        }
    }

    private void initSpreadsheetService() throws Throwable, IOException
    {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        spreadsheetService = new SpreadsheetService(Properties.googleAppName.get());
        GoogleCredential googleCredential = buildGoogleCredential();
        spreadsheetService.setOAuth2Credentials(googleCredential);
        spreadsheetsFeedUrl = FEED_URL_FACTORY.getSpreadsheetsFeedUrl();
    }

    private GoogleCredential buildGoogleCredential() throws GeneralSecurityException, IOException
    {
        File privateKeyFile = new File(Properties.googleSheetsKeyDir.get() + Properties.p12KeyFileName.get());
        GoogleCredential credential = new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT).setJsonFactory(JSON_FACTORY).setServiceAccountId(Properties.serviceAccountEmail.get())
                .setServiceAccountScopes(Properties.googleSheetsScopes.get()).setServiceAccountPrivateKeyFromP12File(privateKeyFile).build();
        return credential;
    }

    public Map<Integer, TitleWithQueries> extractTitlesWithQueries(String worksheetId) throws Throwable, IOException, ServiceException
    {
        Map<Integer, TitleWithQueries> titlesWithQueries = null;
        SpreadsheetEntry spreadsheet = getSpreadsheet(Properties.inputQueriesSheet.get());
        WorksheetEntry worksheet = getWorksheet(spreadsheet, worksheetId);
        if (worksheet != null) {
            List<CellEntry> cellEntries = getCellsForWorksheet(worksheet).getEntries();
            Map<String, String> header = getHeader(cellEntries);
            titlesWithQueries = getTitlesWithQueries(cellEntries, header, worksheetId);
        }
        return titlesWithQueries;
    }

    public List<String> getTitlesWithQueriesAsTsv(String worksheetId) throws Throwable
    {
        SpreadsheetEntry spreadsheet = getSpreadsheet(Properties.inputQueriesSheet.get());
        return extractWorksheetData(spreadsheet, worksheetId, null);
    }

    public List<String> getLatestSummaryReportAsTsv() throws Throwable
    {
        SpreadsheetEntry spreadsheet = getSpreadsheet(summaryReportName);
        String worksheetId = getLatestWorksheetId(spreadsheet);
        return extractWorksheetData(spreadsheet, worksheetId, HeaderUtils.getHeader(ReportType.summary));

    }

    public List<String> getLatestDetailReportAsTsv() throws Throwable
    {
        SpreadsheetEntry spreadsheet = getSpreadsheet(detailReportName);
        String worksheetId = getLatestWorksheetId(spreadsheet);
        return extractWorksheetData(spreadsheet, worksheetId, HeaderUtils.getHeader(ReportType.details));
    }

    
    public void extractReport(Report report, boolean isDetailReport) throws Throwable
    {
        List<ReportItem> reportItems = null;
        String spreadsheetName = getReportName(isDetailReport);
        SpreadsheetEntry spreadsheet = getSpreadsheet(spreadsheetName);
        String worksheetId = getLatestWorksheetId(spreadsheet);
        WorksheetEntry worksheet = getWorksheet(spreadsheet, worksheetId);
        if (worksheet != null) {
            List<CellEntry> cellEntries = getCellsForWorksheet(worksheet).getEntries();
            Map<String, String> header = getHeader(cellEntries);
            reportItems = getReport(cellEntries, header, isDetailReport);
        }
        report.setItems(reportItems);
        report.setDate(worksheetId);
    }
    
    private String getLatestWorksheetId(SpreadsheetEntry spreadsheet) throws Throwable
    {
        String worksheetId = null;
        List<WorksheetEntry> worksheets = spreadsheet.getWorksheets();
        Date reportCurrentDate = new Date(Long.MIN_VALUE);
        for (WorksheetEntry worksheet : worksheets) {
            String title = worksheet.getTitle().getPlainText();
            if(title.equals("instructions") || title.equals("Sheet1") || title.startsWith("diff_") || title.startsWith("ignore_")) continue;
            Date date = dateUtil.getDateFromString(title);
            if (date.after(reportCurrentDate)){
                reportCurrentDate = date;
                worksheetId = title;
            }
        }
        return worksheetId;
    }


    private List<String> extractWorksheetData(SpreadsheetEntry spreadsheet, String worksheetId, String[] header) throws Throwable, IOException, ServiceException
    {
        List<String> returnValue = Lists.newArrayList();
        WorksheetEntry worksheet = getWorksheet(spreadsheet, worksheetId);
        if (worksheet != null) {
            ListFeed listFeed = getListFeedForWorksheet(worksheet);
            returnValue = getAllEntries(listFeed, header);
        }
        return returnValue;
    }

    private List<String> getAllEntries(ListFeed listFeed, String[] headerDefault)
    {
        List<String> returnValue = Lists.newArrayList();
		Joiner joiner = Joiner.on(Properties.inputDelimiter.get());
		Set<String> header = null;
		List<ListEntry> entries = listFeed.getEntries();
		if (entries != null && entries.size() > 0) {
			ListEntry listEntry = entries.get(0);
			if (listEntry != null)
				header = listEntry.getCustomElements().getTags();
		}
		if (header == null && headerDefault != null) {
			header = new LinkedHashSet<String>();
			for (String headerItem : headerDefault) {
				header.add(headerItem);
			}
		}
        returnValue.add(joiner.join(header));
        for (ListEntry row : entries) {
            List<String> rowValues = Lists.newArrayList();
            for (String tag : header) {
                String value = row.getCustomElements().getValue(tag);
                if (value == null)
                    value = "";
                rowValues.add(value);
            }
            String rowValuesString = joiner.join(rowValues);
            returnValue.add(rowValuesString);
        }
        return returnValue;
    }

    public void updateReport(String worksheetId, String[] reportHeader, List<ReportItem> reportItems, boolean isDetailReport) throws Throwable, IOException, ServiceException
    {
        String reportSpreadsheetName = getReportName(isDetailReport);
        SpreadsheetEntry spreadsheet = getSpreadsheet(reportSpreadsheetName );

        int numberOfRows = reportItems.size() + 1;
        int numberOfColumns = reportHeader.length;

        addNewWorksheet(spreadsheet, worksheetId, numberOfRows, numberOfColumns);

        WorksheetEntry worksheet = getWorksheet(spreadsheet, worksheetId);
        if (worksheet != null) {
            updateWorksheetWithAllItems(worksheet, reportHeader, reportItems, numberOfRows, numberOfColumns);
        }
    }

    private String getReportName(boolean isDetailReport)
    {
        if (isDetailReport)
            return detailReportName;
        else
            return summaryReportName;
    }

    private SpreadsheetEntry getSpreadsheet(String reportSpreadsheetName) throws Throwable
    {
        SpreadsheetFeed feed = spreadsheetService.getFeed(spreadsheetsFeedUrl, SpreadsheetFeed.class);
        for (SpreadsheetEntry spreadsheet : feed.getEntries()) {
            if (spreadsheet.getTitle().getPlainText().equalsIgnoreCase(reportSpreadsheetName))
                return spreadsheet;
        }
        throw new RuntimeException(String.format("Either user '%s' has no access to the specified spreadsheet, or there is no spreadsheet named '%s'", Properties.serviceAccountEmail.get(), reportSpreadsheetName));
    }

    private void addNewWorksheet(SpreadsheetEntry spreadsheet, String worksheetId, int numberOfRows, int numberOfColumns) throws Throwable
    {
        if (spreadsheet != null) {
            WorksheetEntry worksheet = new WorksheetEntry();
            worksheet.setTitle(new PlainTextConstruct(worksheetId));
            worksheet.setRowCount(numberOfRows);
            worksheet.setColCount(numberOfColumns);

            URL worksheetFeedUrl = spreadsheet.getWorksheetFeedUrl();
            spreadsheetService.insert(worksheetFeedUrl, worksheet);
        }
    }

    private void updateWorksheetWithAllItems(WorksheetEntry worksheet, String[] header, List<ReportItem> reportItems, int numberOfRows, int numberOfColumns)
            throws BatchInterruptedException, MalformedURLException, IOException, ServiceException
    {
        URL cellFeedUrl = worksheet.getCellFeedUrl();
        CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl, CellFeed.class);

        Map<String, CellEntry> cellEntries = prepareBatchByQueringWorksheet(cellFeedUrl, numberOfRows, numberOfColumns);

        int startingRow = 1;
        int rowsInBatch = ((Double) Math.ceil((double)numberOfRows / Properties.googleSheetsBatchUploadSizeSplitFactor.get())).intValue();
        int endingRow = rowsInBatch;
        for (int i = 0; i < Properties.googleSheetsBatchUploadSizeSplitFactor.get(); i++) {
            CellFeed batchRequest = createBatchRequest(header, reportItems, startingRow, endingRow, numberOfColumns, cellEntries);
            Link batchLink = cellFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
            CellFeed batchResponse = spreadsheetService.batch(new URL(batchLink.getHref()), batchRequest);
            boolean isSuccess = checkResults(batchResponse);
            logger.info((isSuccess ? "Batch operations successful: " : "Batch operations failed: ") + worksheet.getTitle().getPlainText());
            startingRow = startingRow + endingRow;
            endingRow = Math.min(numberOfRows, endingRow + rowsInBatch);
        }
    }
    
    protected boolean checkResults(CellFeed batchResponse)
    {
        boolean isSuccess = true;
        for (CellEntry entry : batchResponse.getEntries()) {
            String batchId = BatchUtils.getBatchId(entry);
            if (!BatchUtils.isSuccess(entry)) {
                isSuccess = false;
                BatchStatus status = BatchUtils.getBatchStatus(entry);
                logger.error(String.format("%s failed (%s) %s", batchId, status.getReason(), status.getContent()));
            }
        }
        return isSuccess;
    }

    protected CellFeed createBatchRequest(String[] header, List<ReportItem> reportItems, int startingRow, int endingRow, int numberOfColumns, Map<String, CellEntry> cellEntries)
    {
        CellFeed batchRequest = new CellFeed();
        for (int rowIndex = startingRow; rowIndex <= endingRow; rowIndex++) {
            for (int columnIndex = 1; columnIndex <= numberOfColumns; columnIndex++) {
                String id = getR1C1Id(rowIndex, columnIndex);
                CellEntry batchEntry = new CellEntry(cellEntries.get(id));
                String rowHeader = header[columnIndex - 1];
                if (rowIndex == 1) {
                    batchEntry.changeInputValueLocal(rowHeader);
                } else
                    batchEntry.changeInputValueLocal(reportItems.get(rowIndex - 2).getNamedValues().get(rowHeader));
                BatchUtils.setBatchId(batchEntry, id);
                BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.UPDATE);
                batchRequest.getEntries().add(batchEntry);
            }
        }
        return batchRequest;
    }

    private Map<String, CellEntry> prepareBatchByQueringWorksheet(URL cellFeedUrl, int numberOfRows, int numberOfColumns) throws IOException, ServiceException
    {
        CellFeed batchRequest = new CellFeed();
        for (int rowIndex = 1; rowIndex <= numberOfRows; rowIndex++) {
            for (int columnIndex = 1; columnIndex <= numberOfColumns; columnIndex++) {
                String id = getR1C1Id(rowIndex, columnIndex);
                CellEntry batchEntry = new CellEntry(rowIndex, columnIndex, id);
                batchEntry.setId(String.format("%s/%s", cellFeedUrl.toString(), id));
                BatchUtils.setBatchId(batchEntry, id);
                BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.QUERY);
                batchRequest.getEntries().add(batchEntry);
            }
        }

        CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl, CellFeed.class);
        CellFeed queryBatchResponse = spreadsheetService.batch(new URL(cellFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM).getHref()), batchRequest);

        Map<String, CellEntry> cellEntryMap = new HashMap<String, CellEntry>(numberOfColumns);
        for (CellEntry entry : queryBatchResponse.getEntries()) {
            cellEntryMap.put(BatchUtils.getBatchId(entry), entry);
        }
        return cellEntryMap;
    }

    private WorksheetEntry getWorksheet(SpreadsheetEntry spreadsheet, String worksheetId) throws Throwable
    {
        List<WorksheetEntry> worksheets = spreadsheet.getWorksheets();
        for (WorksheetEntry worksheet : worksheets) {
            String title = worksheet.getTitle().getPlainText();
            if (title.equalsIgnoreCase(worksheetId))
                return worksheet;
        }
        return null;
    }

    private CellFeed getCellsForWorksheet(WorksheetEntry worksheet) throws IOException, ServiceException
    {
        URL cellFeedUrl = worksheet.getCellFeedUrl();
        CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl, CellFeed.class);
        return cellFeed;
    }

    private ListFeed getListFeedForWorksheet(WorksheetEntry worksheet) throws IOException, ServiceException
    {
        URL listFeedUrl = worksheet.getListFeedUrl();
        ListFeed cellFeed = spreadsheetService.getFeed(listFeedUrl, ListFeed.class);
        return cellFeed;
    }

    private Map<String, String> getHeader(List<CellEntry> cellEntries)
    {
        Map<String, String> returnValue = Maps.newLinkedHashMap();
        for (CellEntry cell : cellEntries) {
            String column = getColumnFromCellAddress(cell);
            Integer row = getRowFromCellAddress(cell);
            String value = cell.getCell().getValue();
            if (row == 1)
                returnValue.put(column, value);
        }
        return returnValue;
    }

    private Map<Integer, TitleWithQueries> getTitlesWithQueries(List<CellEntry> cellEntries, Map<String, String> header, String worksheetId)
    {
        Map<Integer, TitleWithQueries> returnValue = Maps.newLinkedHashMap();
        for (CellEntry cell : cellEntries) {
            String column = getColumnFromCellAddress(cell);
            Integer row = getRowFromCellAddress(cell);
            String value = cell.getCell().getValue().trim();
            if (row == 1)
                continue;
            TitleWithQueries titleWithQueries = returnValue.get(row);
            if (titleWithQueries == null)
                titleWithQueries = new TitleWithQueries(worksheetId);
            String headerValue = header.get(column);
            titleWithQueries.setValue(headerValue, value);
            returnValue.put(row, titleWithQueries);
        }
        return returnValue;
    }
    
    private List<ReportItem> getReport(List<CellEntry> cellEntries, Map<String, String> header, boolean isDetailReport)
    {
        List<ReportItem> returnValue = Lists.newArrayList();
        int previousRow = 0;
        ReportItem reportItem = null;
        for (CellEntry cell : cellEntries) {
            String column = getColumnFromCellAddress(cell);
            Integer row = getRowFromCellAddress(cell);
            String value = cell.getCell().getValue();
            if (row == 1)
                continue;
            if (previousRow != row) {
                if (row != 1 && reportItem!=null)
                    returnValue.add(reportItem);
                if (isDetailReport)
                    reportItem = new DetailReportItem();
                else
                    reportItem = new SummaryReportItem();
            }
            String headerValue = header.get(column);
            reportItem.setValue(headerValue, value);
            previousRow = row;
        }
        if(reportItem!=null)
            returnValue.add(reportItem);
        return returnValue;
    }

    private String getColumnFromCellAddress(CellEntry cell)
    {
        Matcher matcher = VALID_A1_PATTERN.matcher(cell.getTitle().getPlainText());
        while (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Integer getRowFromCellAddress(CellEntry cell)
    {
        Matcher matcher = VALID_A1_PATTERN.matcher(cell.getTitle().getPlainText());
        while (matcher.find()) {
            return Integer.valueOf(matcher.group(2));
        }
        return null;
    }

    private String getR1C1Id(int rowIndex, int columnIndex)
    {
        return "R" + rowIndex + "C" + columnIndex;
    }

}
