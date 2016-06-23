package com.netflix.search.query.report.summary;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netflix.search.query.report.Report;
import com.netflix.search.query.report.ReportItem;
import com.netflix.search.query.report.ResultType;

public class SummaryReportTest {

	private static final String TEST1 = "test1";


	@Test
	void emptyReportsTest()
	{
		Report previousSummaryReport = new SummaryReport();
		Report report = new SummaryReport();
		Report diffReport = report.createReportDiffs(previousSummaryReport);
		List<ReportItem> items = diffReport.getItems();
		List<ReportItem> expectedItems = Lists.newArrayList();
		Assert.assertEquals(items, expectedItems);
	}

	@Test
	void noDiffReportsTest()
	{
		Report previousSummaryReport = new SummaryReport();
		List<ReportItem> itemsForPreviousReport = Lists.newArrayList();
		Map<ResultType, Integer> countersForPreviousReport = Maps.newHashMap();
		itemsForPreviousReport.add(new SummaryReportItem(TEST1, 1, 1, 0.1, 0.1, 0.1, countersForPreviousReport));
		previousSummaryReport.setItems(itemsForPreviousReport);

		Report report = new SummaryReport();
		List<ReportItem> itemsForCurrentReport = Lists.newArrayList();
		Map<ResultType, Integer> countersForCurrentReport = Maps.newHashMap();
		itemsForCurrentReport.add(new SummaryReportItem(TEST1, 1, 1, 0.1, 0.1, 0.1, countersForCurrentReport));

		report.setItems(itemsForCurrentReport);

		Report diffReport = report.createReportDiffs(previousSummaryReport);

		List<ReportItem> itemsForDiffsReport = diffReport.getItems();

		Map<String, ReportItem> expectedItems = Maps.newHashMap();
		Map<ResultType, Integer> countersForExpectedDiffReport = Maps.newHashMap();
		expectedItems.put(TEST1, new SummaryReportItem(TEST1, 0, 0, 0.0, 0.0, 0.0, countersForExpectedDiffReport));
		
		for (ReportItem item: itemsForDiffsReport){
			Assert.assertEquals(item.getNamedValues(), expectedItems.get(item.getNamedValues().get(SummaryReportHeader.name.toString())).getNamedValues());
		}
	}
	
	@Test
	void diffReportsTest()
	{
		Report previousSummaryReport = new SummaryReport();
		List<ReportItem> itemsForPreviousReport = Lists.newArrayList();
		Map<ResultType, Integer> countersForPreviousReport = Maps.newHashMap();
		itemsForPreviousReport.add(new SummaryReportItem(TEST1, 1, 1, 0.1, 0.1, 0.1, countersForPreviousReport));
		previousSummaryReport.setItems(itemsForPreviousReport);

		Report report = new SummaryReport();
		List<ReportItem> itemsForCurrentReport = Lists.newArrayList();
		Map<ResultType, Integer> countersForCurrentReport = Maps.newHashMap();
		itemsForCurrentReport.add(new SummaryReportItem(TEST1, 1, 1, 0.1, 0.1, 0.2, countersForCurrentReport));

		report.setItems(itemsForCurrentReport);

		Report diffReport = report.createReportDiffs(previousSummaryReport);

		List<ReportItem> itemsForDiffsReport = diffReport.getItems();

		Map<String, ReportItem> expectedItems = Maps.newHashMap();
		Map<ResultType, Integer> countersForExpectedDiffReport = Maps.newHashMap();
		expectedItems.put(TEST1, new SummaryReportItem(TEST1, 0, 0, 0.0, 0.0, 0.1, countersForExpectedDiffReport));
		
		for (ReportItem item: itemsForDiffsReport){
			Assert.assertEquals(item.getNamedValues(), expectedItems.get(item.getNamedValues().get(SummaryReportHeader.name.toString())).getNamedValues());
		}
		
	}	
	

	@Test
	void previousNullReportTest()
	{
		Report report = new SummaryReport();
		List<ReportItem> itemsForCurrentReport = Lists.newArrayList();
		Map<ResultType, Integer> countersForCurrentReport = Maps.newHashMap();
		itemsForCurrentReport.add(new SummaryReportItem(TEST1, 1, 1, 0.1, 0.1, 0.1, countersForCurrentReport));

		report.setItems(itemsForCurrentReport);

		Report diffReport = report.createReportDiffs(null);

		List<ReportItem> itemsForDiffsReport = diffReport.getItems();

		Map<String, ReportItem> expectedItems = Maps.newHashMap();
		Map<ResultType, Integer> countersForExpectedDiffReport = Maps.newHashMap();
		expectedItems.put(TEST1, new SummaryReportItem(TEST1, 0, 0, 0.0, 0.0, 0.0, countersForExpectedDiffReport));
		
		for (ReportItem item: itemsForDiffsReport){
			Assert.assertEquals(item.getNamedValues(), expectedItems.get(item.getNamedValues().get(SummaryReportHeader.name.toString())).getNamedValues());
		}
	}	
}
