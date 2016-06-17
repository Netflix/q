package com.netflix.search.query.report.detail;

import java.util.List;

import com.netflix.search.query.report.ReportItem;

public class DetailReportDiff extends DetailReport {
    public DetailReportDiff(List<ReportItem> items) {
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
