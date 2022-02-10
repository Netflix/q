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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.netflix.search.query.report.detail.DetailReport;
import com.netflix.search.query.report.detail.DetailReportHeader;
import com.netflix.search.query.report.summary.SummaryReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.AppendDimensionRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridCoordinate;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

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

    private String summaryReportName = "";
    private String detailReportName = "";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Sheets spreadsheetService = null;
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    private DateUtil dateUtil = new DateUtil();

    public GoogleSheetsService() {
        super();
        try {
            initSpreadsheetService();
        } catch (Throwable e) {
            logger.error("Error trying to init the GoogleSheetsService", e);
        }
    }

    public void setUpReportNames() {
        this.summaryReportName = Properties.sumReportSheet.get();
        this.detailReportName = Properties.detailReportSheet.get();
        if (Properties.isDevOnlyTest.get()) {
            this.summaryReportName = Properties.sumReportSheetDev.get();
            this.detailReportName = Properties.detailReportSheetDev.get();
        }
    }

    private void initSpreadsheetService() throws Throwable {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        spreadsheetService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(Properties.googleAppName.get()).build();

    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        File privateKeyFile = new File(Properties.googleSheetsKeyDir.get() + Properties.p12KeyFileName.get());
        GoogleCredential cr = GoogleCredential
                .fromStream(new FileInputStream(privateKeyFile))
                .createScoped(SCOPES);
        GoogleCredential.Builder builder = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountScopes(SCOPES)
                .setServiceAccountId(cr.getServiceAccountId())
                .setServiceAccountPrivateKey(cr.getServiceAccountPrivateKey())
                .setServiceAccountPrivateKeyId(cr.getServiceAccountPrivateKeyId())
                .setTokenServerEncodedUrl(cr.getTokenServerEncodedUrl())
                .setServiceAccountUser(Properties.serviceAccountEmail.get());

        return builder.build();
    }

    public List<List<Object>> getSpreadsheetDataForQueries(String worksheetId) throws Throwable {
        List<List<Object>> values = null;
        Spreadsheet spreadsheet = getSpreadsheet(Properties.inputQueriesSheet.get());
        Sheet worksheet = getWorksheet(spreadsheet, worksheetId);
        if (worksheet != null) {
            ValueRange response = spreadsheetService.spreadsheets().values().get(Properties.inputQueriesSheet.get(), worksheetId).execute();
            values = response.getValues();
        }
        return values;
    }

    public List<List<Object>> getSpreadsheetData(Spreadsheet spreadsheet, String spreadsheetName, String worksheetId) throws Throwable {
        List<List<Object>> values = null;
        Sheet worksheet = getWorksheet(spreadsheet, worksheetId);
        if (worksheet != null) {
            ValueRange response = spreadsheetService.spreadsheets().values().get(spreadsheetName, worksheetId).execute();
            values = response.getValues();
        }
        return values;
    }

    public List<String> getLatestSummaryReportAsTsv(Report report) {
        return extractWorksheetData(report, HeaderUtils.getHeader(ReportType.summary));
    }

    public List<String> getLatestDetailReportAsTsv(Report report) {
        return extractWorksheetData(report, HeaderUtils.getHeader(ReportType.details));
    }

    public Report extractReport(boolean isDetailReport) throws Throwable {
        Report report = null;
        if (isDetailReport) report = new DetailReport();
        else report = new SummaryReport();
        List<ReportItem> reportItems = null;
        String spreadsheetName = getReportName(isDetailReport);
        Spreadsheet spreadsheet = getSpreadsheet(spreadsheetName);
        String worksheetId = getLatestWorksheetId(spreadsheet);
        List<List<Object>> spreadsheetData = getSpreadsheetData(spreadsheet, spreadsheetName, worksheetId);
        if (spreadsheetData != null && spreadsheetData.size() > 0) {
            reportItems = getReport(spreadsheetData, isDetailReport);
        }
        report.setItems(reportItems);
        report.setDate(worksheetId);
        return report;
    }


    private String getLatestWorksheetId(Spreadsheet spreadsheet) {
        String worksheetId = null;
        Date reportCurrentDate = new Date(Long.MIN_VALUE);
        for (Sheet sheet : spreadsheet.getSheets()) {
            String title = sheet.getProperties().getTitle();
            if (title.equals("instructions") || title.equals("Sheet1") || title.startsWith("diff_") || title.startsWith("ignore_"))
                continue;
            Date date = dateUtil.getDateFromString(title);
            if (date.after(reportCurrentDate)) {
                reportCurrentDate = date;
                worksheetId = title;
            }
        }
        return worksheetId;
    }

    protected List<String> extractWorksheetData(List<List<Object>> values, String[] headerDefault) {
        List<String> returnValue = Lists.newArrayList();
        int startingIndex = headerDefault == null ? 0 : 1;
        int headerSize = 0;
        if (values != null && values.size() > 0) {
            if (headerDefault != null) {
                returnValue.add(Arrays.asList(headerDefault).stream().collect(Collectors.joining(Properties.inputDelimiter.get())));
                headerSize = headerDefault.length;
            } else {
                headerSize = values.get(0).size();
            }
            for (List<Object> row : values.subList(startingIndex, values.size())) {
                int diffInRowSize = headerSize - row.size();
                StringBuilder trailingEmptyCells = new StringBuilder();
                if (diffInRowSize > 0)
                    for (int i = 0; i < diffInRowSize; i++) trailingEmptyCells.append(Properties.inputDelimiter.get());
                String rowAsString = row.stream()
                        .map(object -> Objects.toString(object))
                        .collect(Collectors.joining(Properties.inputDelimiter.get()));
                returnValue.add(rowAsString.concat(trailingEmptyCells.toString()));
            }
        }
        return returnValue;
    }

    protected List<String> extractWorksheetData(Report report, String[] headerDefault) {
        List<String> returnValue = Lists.newArrayList();
        if (report != null && report.getItems().size() > 0) {
            returnValue.add(Arrays.asList(headerDefault).stream().collect(Collectors.joining(Properties.inputDelimiter.get())));
            for (ReportItem reportItem : report.getItems()) {
                returnValue.add(reportItem.toString());
            }
        }
        return returnValue;
    }

    private String getReportName(boolean isDetailReport) {
        if (isDetailReport)
            return detailReportName;
        else
            return summaryReportName;
    }

    private Spreadsheet getSpreadsheet(String reportSpreadsheetName) throws Throwable {
        Spreadsheet spreadsheet = spreadsheetService.spreadsheets().get(reportSpreadsheetName).execute();
        return spreadsheet;
    }

    public void updateReport(String worksheetId, String[] reportHeader, List<ReportItem> reportItems, boolean isDetailReport) throws Throwable {
        String reportSpreadsheetName = getReportName(isDetailReport);
        Spreadsheet spreadsheet = getSpreadsheet(reportSpreadsheetName);

        int numberOfRows = reportItems.size() + 1;
        int numberOfColumns = reportHeader.length;

        Sheet sheet = addNewWorksheet(spreadsheet, worksheetId, numberOfRows, numberOfColumns);

        if (sheet != null) {
            importData(spreadsheet, sheet, reportItems, reportHeader, isDetailReport);
        }
    }

    private Request insertValues(Integer sheetId, List<ReportItem> reportItems, String[] reportHeader, boolean isDetailReport) {
        GridCoordinate grid = new GridCoordinate().setSheetId(sheetId).setRowIndex(0).setColumnIndex(0);

        List<RowData> rowData = new ArrayList<>();

        List<CellData> cellData = new ArrayList<>();
        for (String headerItem : reportHeader) {
            cellData.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(headerItem)));
        }
        rowData.add(new RowData().setValues(cellData));

        for (ReportItem row : reportItems) {
            cellData = new ArrayList<>();
            int columnCount = 0;
            for (Map.Entry<String, String> cell : row.getNamedValues().entrySet()) {
                if (isDetailReport || (!isDetailReport && (cell.getKey().equals("name") || cell.getKey().equals("precision") || cell.getKey().equals("recall") || cell.getKey().equals("fmeasure") || cell.getKey().equals("comments"))))
                    cellData.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(cell.getValue())));
                else
                    cellData.add(new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue(Double.valueOf(cell.getValue()))));
                columnCount++;
            }
            rowData.add(new RowData().setValues(cellData));
        }

        Request request = new Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setStart(grid)
                        .setRows(rowData)
                        .setFields("userEnteredValue"));
        return request;
    }

    public void importData(Spreadsheet spreadsheet, Sheet sheet, List<ReportItem> reportItems, String[] reportHeader, boolean isDetailReport) throws IOException {
        Integer sheetId = sheet.getProperties().getSheetId();
        List<Request> requests = new ArrayList<Request>();
        if (reportItems.size() > 1000) {
            requests.add(appendEmptyRows(sheetId, reportItems.size() - 1000));
        }
        requests.add(insertValues(sheetId, reportItems, reportHeader, isDetailReport));
        postRequests(spreadsheet, requests);
        logger.info("Events imported.");
    }

    private Sheet addNewWorksheet(Spreadsheet spreadsheet, String worksheetId, int numberOfRows, int numberOfColumns) throws Throwable {
        Sheet sheet = null;
        if (spreadsheet != null) {
            Request request = new Request().setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().setTitle(worksheetId).setGridProperties(new GridProperties().setRowCount(numberOfRows).setColumnCount(numberOfColumns))));
            BatchUpdateSpreadsheetResponse response = postRequest(spreadsheet, request);
            Spreadsheet updatedSpreadsheet = response.getUpdatedSpreadsheet();
            sheet = getWorksheet(updatedSpreadsheet, worksheetId);
            logger.info("Sheet {} created.", worksheetId);
            return sheet;
        }
        return sheet;
    }

    private Sheet getWorksheet(Spreadsheet spreadsheet, String worksheetId) {
        for (Sheet sheet : spreadsheet.getSheets()) {
            if (sheet.getProperties().getTitle().equals(worksheetId))
                return sheet;
        }
        logger.error("Sheet {} NOT found.", worksheetId);
        return null;
    }

    public Map<Integer, TitleWithQueries> getTitlesWithQueries(List<List<Object>> values, String worksheetId) {
        Map<Integer, TitleWithQueries> returnValue = Maps.newLinkedHashMap();
        for (int rowIndex = 1; rowIndex < values.size(); rowIndex++) {
            TitleWithQueries titleWithQueries = returnValue.get(rowIndex);
            if (titleWithQueries == null)
                titleWithQueries = new TitleWithQueries(worksheetId);
            int columnIndex = 0;
            for (Object cell : values.get(rowIndex)) {
                if (cell != null) {
                    String cellValue = cell.toString().trim();
                    String headerValue = values.get(0).get(columnIndex).toString();
                    titleWithQueries.setValue(headerValue, cellValue);
                }
                columnIndex++;
            }
            returnValue.put(rowIndex, titleWithQueries);
        }
        return returnValue;
    }


    private List<ReportItem> getReport(List<List<Object>> values, boolean isDetailReport) {
        List<ReportItem> returnValue = Lists.newArrayList();
        ReportItem reportRowItem = null;

        for (int rowIndex = 1; rowIndex < values.size(); rowIndex++) {
            int columnIndex = 0;

            if (isDetailReport) reportRowItem = new DetailReportItem();
            else reportRowItem = new SummaryReportItem();

            for (Object cell : values.get(rowIndex)) {
                if (cell != null) {
                    String cellValue = cell.toString().trim();
                    String headerValue = values.get(0).get(columnIndex).toString();
                    reportRowItem.setValue(headerValue, cellValue);
                }
                columnIndex++;
            }
            if (isDetailReport) {
                if (!reportRowItem.getNamedValues().containsKey(DetailReportHeader.expected.toString()))
                    reportRowItem.setValue(DetailReportHeader.expected.toString(), "");
                if (!reportRowItem.getNamedValues().containsKey(DetailReportHeader.actual.toString()))
                    reportRowItem.setValue(DetailReportHeader.actual.toString(), "");
            }
            if (reportRowItem != null)
                returnValue.add(reportRowItem);
        }
        return returnValue;
    }

    private BatchUpdateSpreadsheetResponse postRequest(Spreadsheet spreadsheet, Request request) throws IOException {
        List<Request> requests = Arrays.asList(request);
        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests).setIncludeSpreadsheetInResponse(true);
        BatchUpdateSpreadsheetResponse response = spreadsheetService.spreadsheets().batchUpdate(spreadsheet.getSpreadsheetId(), body).execute();
        return response;
    }

    private BatchUpdateSpreadsheetResponse postRequests(Spreadsheet spreadsheet, List<Request> requests) throws IOException {
        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests).setIncludeSpreadsheetInResponse(true);
        BatchUpdateSpreadsheetResponse response = spreadsheetService.spreadsheets().batchUpdate(spreadsheet.getSpreadsheetId(), body).execute();
        return response;
    }

    private Request appendEmptyRows(Integer sheetId, Integer length) {
        Request request = new Request().setAppendDimension(new AppendDimensionRequest().setSheetId(sheetId).setDimension("ROWS").setLength(length));
        return request;
    }
}
