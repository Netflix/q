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
package com.netflix.search.query.report.detail;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netflix.search.query.report.Report;
import com.netflix.search.query.report.ReportItem;
import com.netflix.search.query.report.ResultType;

public class DetailReportTest {

	private static final String TITLE1 = "title1";
	private static final String TITLE2 = "title2";
	private static final String Q1 = "a";
	private static final String Q2 = "b";
	private static final String TEST1_EN = "test1-english";

	@Test
	void emptyReportsTest()
	{
		Report previousReport = new DetailReport();
		Report report = new DetailReport();
		Report diffReport = report.createReportDiffs(previousReport);
		List<ReportItem> items = diffReport.getItems();
		List<ReportItem> expectedItems = Lists.newArrayList();
		Assert.assertEquals(items, expectedItems);
	}

	@Test
	void noDiffReportsTest()
	{
		Report previousReport = new DetailReport();
		List<ReportItem> itemsForPreviousReport = Lists.newArrayList();
		itemsForPreviousReport.add(new DetailReportItem(TEST1_EN, ResultType.noResultsFailed, Q1, TITLE1, DetailReport.NONE));
		previousReport.setItems(itemsForPreviousReport);

		Report report = new DetailReport();
		List<ReportItem> itemsForCurrentReport = Lists.newArrayList();
		itemsForCurrentReport.add(new DetailReportItem(TEST1_EN, ResultType.noResultsFailed, Q1, TITLE1, DetailReport.NONE));

		report.setItems(itemsForCurrentReport);

		Report diffReport = report.createReportDiffs(previousReport);

		List<ReportItem> itemsForDiffsReport = diffReport.getItems();

		Map<String, ReportItem> expectedItems = Maps.newHashMap();
		expectedItems.put("test1", new DetailReportItem(TEST1_EN, ResultType.noResultsFailed, Q1, TITLE1, DetailReport.NONE));

		for (ReportItem item : itemsForDiffsReport)
		{
			Assert.assertEquals(item.getNamedValues(), expectedItems.get(item.getNamedValues().get(DetailReportHeader.name.toString())).getNamedValues());
		}
	}

	@Test
	void diffReportsTest()
	{
		Report previousReport = new DetailReport();
		List<ReportItem> itemsForPreviousReport = Lists.newArrayList();
		itemsForPreviousReport.add(new DetailReportItem(TEST1_EN, ResultType.noResultsFailed, Q1, TITLE1, DetailReport.NONE));
		previousReport.setItems(itemsForPreviousReport);

		Report report = new DetailReport();
		List<ReportItem> itemsForCurrentReport = Lists.newArrayList();
		itemsForCurrentReport.add(new DetailReportItem(TEST1_EN, ResultType.supersetResultsFailed, Q2, TITLE1, TITLE2));

		report.setItems(itemsForCurrentReport);

		Report diffReport = report.createReportDiffs(previousReport);

		List<ReportItem> itemsForDiffsReport = diffReport.getItems();

		Map<String, ReportItem> expectedItems = Maps.newHashMap();

		DetailReportItem fixedItem = new DetailReportItem(TEST1_EN, ResultType.noResultsFailed, Q1, TITLE1, DetailReport.NONE);
		fixedItem.setValue(DetailReportHeader.comments.toString(), DetailReport.FIXED);
		expectedItems.put(TEST1_EN + ResultType.noResultsFailed.toString(), fixedItem);

		DetailReportItem newItem = new DetailReportItem(TEST1_EN, ResultType.supersetResultsFailed, Q2, TITLE1, TITLE2);
		newItem.setValue(DetailReportHeader.comments.toString(), DetailReport.NEW);
		expectedItems.put(TEST1_EN + ResultType.supersetResultsFailed.toString(), newItem);

		for (ReportItem item : itemsForDiffsReport)
		{
			Map<String, String> actualNamedValues = item.getNamedValues();
			String testName = actualNamedValues.get(DetailReportHeader.name.toString());
			String failure = actualNamedValues.get(DetailReportHeader.failure.toString());
			Map<String, String> expectedNamedValues = expectedItems.get(testName + failure).getNamedValues();
			Assert.assertEquals(actualNamedValues, expectedNamedValues);
		}

	}
}
