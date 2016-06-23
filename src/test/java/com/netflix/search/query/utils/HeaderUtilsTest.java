package com.netflix.search.query.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.netflix.search.query.report.ReportType;

public class HeaderUtilsTest {

	@Test
	public void testSummary(){
		String[] header = HeaderUtils.getHeader(ReportType.summary);
		Assert.assertEquals(header, new String[]{"name", "titles", "queries", "supersetResultsFailed", "differentResultsFailed", "noResultsFailed", "successQ", "precision", "recall", "fmeasure", "comments"});
	}

	@Test
	public void testDetail(){
		String[] header = HeaderUtils.getHeader(ReportType.details);
		Assert.assertEquals(header, new String[]{"name", "failure", "query", "expected", "actual", "comments"});
	}
}
