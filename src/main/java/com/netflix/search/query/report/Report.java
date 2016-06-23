package com.netflix.search.query.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.common.base.Joiner;
import com.netflix.search.query.Properties;
import com.netflix.search.query.utils.DateUtil;
import com.netflix.search.query.utils.HeaderUtils;

public abstract class Report {
    private static final String ENCODING = "UTF-8";

    private List<ReportItem> items = Lists.newArrayList();

    private Date date;
    private DateUtil dateUtil = new DateUtil();

    public Report() {
        try {
            this.date = dateUtil.getDateFromCurrentTime();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void setDate(String dateString)
    {
        try {
        	if(dateString!=null)
        		this.date = dateUtil.getDateFromString(dateString);
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
