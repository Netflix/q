package com.netflix.search.query;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netflix.search.query.QueryTests;
import com.netflix.search.query.engine.solr.SolrIndexer;
import com.netflix.search.query.engine.solr.SolrSearcher;
import com.netflix.search.query.report.ReportItem;
import com.netflix.search.query.report.ResultType;
import com.netflix.search.query.report.detail.DetailReport;
import com.netflix.search.query.report.detail.DetailReportHeader;
import com.netflix.search.query.report.detail.DetailReportItem;
import com.netflix.search.query.report.google.GoogleDataExtractor;
import com.netflix.search.query.report.summary.SummaryReport;
import com.netflix.search.query.report.summary.SummaryReportHeader;
import com.netflix.search.query.report.summary.SummaryReportItem;

public class QueryTestsTest {
	
	private static final String LANG1_EN = "en";
	private static final String TEST1 = "test1";
	private static final String DOC1 = "1";
	private static final String TITLE1 = "title1";
	private static final String DOC2 = "2";
	private static final String TITLE2 = "title2";
	private static final String Q1 = "a";
	private static final String TEST1_EN = "test1-english";
	
	SolrIndexer solrIndexerMock;
	SolrSearcher solrSearchMock;;
	GoogleDataExtractor googleDataExtractor;
	QueryTests queryTests;
	Map<String, Map<String, Set<String>>> queries;
	Map<String, Set<String>> queryToTitles;
	Map<String, String> titleIdToName;
	Set<String> titles;
	DetailReport detailReport;
	DetailReport detailReportSpy;
	SummaryReport summaryReport;
	SummaryReport summaryReportSpy;
	List<String> languages; 
	Set<String> hits;
	Map<String, ReportItem> expectedSummaryItems;
	Map<String, ReportItem> expectedDetailItems;
	Map<ResultType, Integer> countersForExpectedReport;
	
	@BeforeMethod
	public void setup() throws Throwable{
		solrIndexerMock = Mockito.mock(SolrIndexer.class);
		solrSearchMock = Mockito.mock(SolrSearcher.class);
		googleDataExtractor = Mockito.mock(GoogleDataExtractor.class);
		
		queryTests = new QueryTests();

        Mockito.doNothing().when(googleDataExtractor).initExtractor();
		
		queryTests.setIndexer(solrIndexerMock);
		queryTests.setSearcher(solrSearchMock);
		queryTests.setGoogleDataExtractor(googleDataExtractor);
		
		titleIdToName = Maps.newHashMap();
		queries = Maps.newHashMap();
		queryToTitles = Maps.newHashMap();
		titles = Sets.newHashSet();
		titles.add(DOC1);
		queryToTitles.put(Q1, titles );
		queries.put(TEST1_EN, queryToTitles);
		queryTests.setQueries(queries );
		
		titleIdToName.put(DOC1, TITLE1);
		titleIdToName.put(DOC2, TITLE2);
		
		detailReport = new DetailReport();
		detailReportSpy = Mockito.spy(detailReport);
		
		Mockito.doNothing().when(detailReportSpy).saveToLocalDisk();
		
		queryTests.setDetailReport(detailReportSpy);
		
		summaryReport = new SummaryReport();
		summaryReportSpy = Mockito.spy(summaryReport);
		
		Mockito.doNothing().when(summaryReportSpy).saveToLocalDisk();
		
		queryTests.setSummaryReport(summaryReportSpy);
		
		languages = Lists.newArrayList();
		languages.add(LANG1_EN);

		hits = Sets.newLinkedHashSet();

		expectedSummaryItems = Maps.newHashMap();
		expectedDetailItems = Maps.newHashMap();
		countersForExpectedReport = Maps.newHashMap();
		
        Mockito.when(solrIndexerMock.getTitleToIds()).thenReturn(titleIdToName);
        Mockito.when(solrSearchMock.getResults(Q1, languages, TEST1)).thenReturn(hits);
	}
	
	@Test
	public void perfectScore() throws Throwable{
        hits.add(DOC1);//expected and fetched

        queryTests.runTest(TEST1_EN, languages);
		
        List<ReportItem> summaryReportItems = summaryReportSpy.getItems();
        countersForExpectedReport.put(ResultType.successQ, 1);
		expectedSummaryItems.put(TEST1_EN, new SummaryReportItem(TEST1_EN, 1, 1, 1.0, 1.0, 1.0, countersForExpectedReport));
		for (ReportItem item: summaryReportItems){
			Assert.assertEquals(item.getNamedValues(), expectedSummaryItems.get(item.getNamedValues().get(SummaryReportHeader.name.toString())).getNamedValues());
		}
		
        List<ReportItem> detailReportItems = detailReportSpy.getItems();
        Assert.assertEquals(detailReportItems.size(), 0);
	}
	
	
	
	@Test
	public void runPrecision() throws Throwable{
        hits.add(DOC1);//expected and fetched
        hits.add(DOC2);//unexpected but fetched
		
        queryTests.runTest(TEST1_EN, languages);
		
        List<ReportItem> summaryReportItems = summaryReportSpy.getItems();
        countersForExpectedReport.put(ResultType.supersetResultsFailed, 1);
		expectedSummaryItems.put(TEST1_EN, new SummaryReportItem(TEST1_EN, 1, 1, 0.5, 1.0, 0.6667, countersForExpectedReport));
		for (ReportItem item: summaryReportItems){
			Assert.assertEquals(item.getNamedValues(), expectedSummaryItems.get(item.getNamedValues().get(SummaryReportHeader.name.toString())).getNamedValues());
		}
		
		List<ReportItem> detailReportItems = detailReportSpy.getItems();
		expectedDetailItems.put(TEST1_EN, new DetailReportItem(TEST1_EN, ResultType.supersetResultsFailed, Q1, "", TITLE2));
		for (ReportItem item: detailReportItems){
			Assert.assertEquals(item.getNamedValues(), expectedDetailItems.get(item.getNamedValues().get(DetailReportHeader.name.toString())).getNamedValues());
		}
	}
	
	
	@Test
	public void runRecall() throws Throwable{
        hits.add(DOC1);//expected and fetched
		titles.add(DOC2);//expected but not fetched
		
        queryTests.runTest(TEST1_EN, languages);
		
        List<ReportItem> summaryReportItems = summaryReportSpy.getItems();
        countersForExpectedReport.put(ResultType.differentResultsFailed, 1);
		expectedSummaryItems.put(TEST1_EN, new SummaryReportItem(TEST1_EN, 2, 1, 1.0, 0.5, 0.6667, countersForExpectedReport));
		for (ReportItem item: summaryReportItems){
			Assert.assertEquals(item.getNamedValues(), expectedSummaryItems.get(item.getNamedValues().get(SummaryReportHeader.name.toString())).getNamedValues());
		}
		
		List<ReportItem> detailReportItems = detailReportSpy.getItems();
		expectedDetailItems.put(TEST1_EN, new DetailReportItem(TEST1_EN, ResultType.differentResultsFailed, Q1, TITLE2, ""));
		for (ReportItem item: detailReportItems){
			Assert.assertEquals(item.getNamedValues(), expectedDetailItems.get(item.getNamedValues().get(DetailReportHeader.name.toString())).getNamedValues());
		}
	}
	
	@Test
	public void runRecallNoResults() throws Throwable{
        hits.clear();//expected but not fetched
		titles.add(DOC1);//expected but not fetched
		
        queryTests.runTest(TEST1_EN, languages);
		
        List<ReportItem> summaryReportItems = summaryReportSpy.getItems();
        countersForExpectedReport.put(ResultType.noResultsFailed, 1);
		expectedSummaryItems.put(TEST1_EN, new SummaryReportItem(TEST1_EN, 1, 1, 0.0, 0.0, 0.0, countersForExpectedReport));
		for (ReportItem item: summaryReportItems){
			Assert.assertEquals(item.getNamedValues(), expectedSummaryItems.get(item.getNamedValues().get(SummaryReportHeader.name.toString())).getNamedValues());
		}
		
		List<ReportItem> detailReportItems = detailReportSpy.getItems();
		expectedDetailItems.put(TEST1_EN, new DetailReportItem(TEST1_EN, ResultType.noResultsFailed, Q1, TITLE1, DetailReport.NONE));
		for (ReportItem item: detailReportItems){
			Assert.assertEquals(item.getNamedValues(), expectedDetailItems.get(item.getNamedValues().get(DetailReportHeader.name.toString())).getNamedValues());
		}
	}	
	
	
}
