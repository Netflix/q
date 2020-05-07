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
package com.netflix.search.query.report;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.netflix.search.query.report.detail.DetailReport;
import com.netflix.search.query.report.detail.DetailReportItem;
import com.netflix.search.query.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.common.base.Joiner;
import com.netflix.search.query.Properties;
import com.netflix.search.query.utils.DateUtil;
import com.netflix.search.query.utils.HeaderUtils;

public abstract class Report {
    public static final Logger logger = LoggerFactory.getLogger(Report.class);

    private static final String ENCODING = "UTF-8";
    private static final int BUFFER_SIZE = 1 << 16; // 64K

    private List<ReportItem> items = Lists.newArrayList();

    private Date date;
    private DateUtil dateUtil = new DateUtil();

    public Report() {
		this.date = dateUtil.getDateFromCurrentTime();
    }

    public void setDate(String dateString)
    {
		if (dateString != null)
			this.date = dateUtil.getDateFromString(dateString);
    }

    public String reportNameForUpload()
    {
        return dateUtil.getStringFromDate(date);
    }

    @Override
    public String toString()
    {
        return getReportName() + "_" + dateUtil.getStringFromDate(date);
    }

    public List<ReportItem> getItems()
    {
        return items;
    }

    public void setItems(List<ReportItem> items)
    {
        this.items = items;
    }

    protected abstract String getReportName();

    public abstract ReportType getReportType();

    protected abstract ReportItem getDiffForReportItem(ReportItem previousItem, ReportItem currentItem);

    protected abstract Report newReport(List<ReportItem> items);

    public Report createReportDiffs(Report previous)
    {
        List<ReportItem> returnValueItems = Lists.newArrayList();

        Map<ReportItem, ReportItem> currentMap = Maps.newLinkedHashMap();
        for (ReportItem currentItem : this.getItems()) {
            currentMap.put(currentItem, currentItem);
        }

		Map<ReportItem, ReportItem> previousMap = Maps.newLinkedHashMap();
		if (previous != null && previous.getItems() != null)
		{
			for (ReportItem previousItem : previous.getItems())
			{
				previousMap.put(previousItem, previousItem);
			}

			for (ReportItem key : previous.getItems())
			{
				ReportItem diffForReportItem = getDiffForReportItem(key, currentMap.get(key));
				if (diffForReportItem != null)
					returnValueItems.add(diffForReportItem);
			}

			for (ReportItem key : this.getItems())
			{
				if (!previous.getItems().contains(key))
				{
					ReportItem diffForReportItem = getDiffForReportItem(previousMap.get(key), key);
					if (diffForReportItem != null)
					{
						returnValueItems.add(diffForReportItem);
					}
				}
			}
		}

        return newReport(returnValueItems);
    }

    public void saveToLocalDisk() throws Throwable
    {
        String header = getHeaderForFlatFilePrint(HeaderUtils.getHeader(getReportType()));
        printReportToLocalDisk(Properties.dataDir.get() + getReportName(), header, items);
    }

    public static DetailReport copyCurrentFileToPreviousAndGetPrevious(String currentName, String previousName) throws IOException {
        File currentFile = new File(Properties.dataDir.get() + currentName);
        Path currentPath = currentFile.toPath();
        File previousFile = new File(Properties.dataDir.get() + previousName+".tsv");
        Path previousPath = previousFile.toPath();
        Files.copy(currentPath, previousPath, StandardCopyOption.REPLACE_EXISTING);

        DetailReport previousDetailReport = new DetailReport();
        List<ReportItem> items = Lists.newArrayList();

        InputStream is = new BufferedInputStream(new FileInputStream(previousFile), BUFFER_SIZE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, ENCODING), BUFFER_SIZE);
        String lineString = null;
        while ((lineString = reader.readLine()) != null) {
            String[] line = lineString.split(Properties.inputDelimiter.get());

            String name = line[0];
            ResultType failure = ResultType.valueOf(line[1]);
            String query = line[2];
            String expected = line[3];
            String actual = line[4];

            ReportItem reportItem = new DetailReportItem(name, failure, query, expected, actual);
            items.add(reportItem);
        }
        previousDetailReport.setItems(items);

        reader.close();
        is.close();
        return previousDetailReport;
    }

    private void printReportToLocalDisk(String fileName, String header, List<ReportItem> reportLines) throws Throwable
    {
        File file = new File(fileName);
        OutputStream out = new FileOutputStream(file);
        Writer writer = new OutputStreamWriter(out, ENCODING);
        for (ReportItem line : reportLines) {
            writer.write(line.toString());
            writer.write("\n");
        }
        writer.close();
        out.close();
    }

    private String getHeaderForFlatFilePrint(String[] reportHeader)
    {
        Joiner joiner = Joiner.on(Properties.inputDelimiter.get());
        return joiner.join(reportHeader);
    }

}
