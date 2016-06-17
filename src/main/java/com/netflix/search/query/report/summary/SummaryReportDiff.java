package com.netflix.search.query.report.summary;

import java.util.List;

import com.netflix.search.query.report.ReportItem;

public class SummaryReportDiff extends SummaryReport {

    public SummaryReportDiff(List<ReportItem> items) {
        super(items);
    }

    @Override
    protected String getReportName()
    {
        return super.getReportName()+"_diff";
    }
    
    @Override
    public String reportNameForUpload(){
        return "diff_"+super.reportNameForUpload();
    }
}
