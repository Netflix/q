package com.netflix.search.query.report;

import java.util.List;

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
