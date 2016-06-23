package com.netflix.search.query.utils;

import com.netflix.search.query.report.ReportType;
import com.netflix.search.query.report.detail.DetailReportHeader;
import com.netflix.search.query.report.summary.SummaryReportHeader;

public class HeaderUtils {

	public static String[] getHeader(ReportType type)
	{
		if (type.equals(ReportType.summary))
		{
			SummaryReportHeader[] values = SummaryReportHeader.values();
			String[] returnValue = new String[values.length];
			for (int i = 0; i < values.length; i++)
			{
				returnValue[i] = values[i].toString();
			}
			return returnValue;
		} else if (type.equals(ReportType.details))
		{
			DetailReportHeader[] values = DetailReportHeader.values();
			String[] returnValue = new String[values.length];
			for (int i = 0; i < values.length; i++)
			{
				returnValue[i] = values[i].toString();
			}
			return returnValue;
		} else
			return null;
	}
}
