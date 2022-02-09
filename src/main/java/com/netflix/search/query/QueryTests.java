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
package com.netflix.search.query;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netflix.search.query.engine.BaseIndexer;
import com.netflix.search.query.engine.BaseSearcher;
import com.netflix.search.query.engine.es.ElasticsearchIndexer;
import com.netflix.search.query.engine.es.ElasticsearchSearcher;
import com.netflix.search.query.engine.solr.SolrIndexer;
import com.netflix.search.query.engine.solr.SolrSearcher;
import com.netflix.search.query.input.Queries;
import com.netflix.search.query.report.Report;
import com.netflix.search.query.report.ResultType;
import com.netflix.search.query.report.detail.DetailReport;
import com.netflix.search.query.report.google.GoogleDataExtractor;
import com.netflix.search.query.report.summary.SummaryReport;

public class QueryTests {
    public static final Logger logger = LoggerFactory.getLogger(QueryTests.class);

    private static final String MAP_VALUE_DELIMITER = "\\|";
	private static final String SHEET_TAB_NAME_DELIMITER = "-";

	private BaseIndexer indexer = null;
    private BaseSearcher searcher = null;
    private Map<String, Map<String, Set<String>>> queries = Maps.newLinkedHashMap();
    private GoogleDataExtractor googleDataExtractor;
    private DetailReport detailReport;
    private SummaryReport summaryReport;
    
    public BaseIndexer getIndexer()
	{
		return indexer;
	}

	public void setIndexer(BaseIndexer indexer)
	{
		this.indexer = indexer;
	}

	public Map<String, Map<String, Set<String>>> getQueries()
	{
		return queries;
	}

	public void setQueries(Map<String, Map<String, Set<String>>> queries)
	{
		this.queries = queries;
	}

	public GoogleDataExtractor getGoogleDataExtractor()
	{
		if (googleDataExtractor == null)
			googleDataExtractor = new GoogleDataExtractor();
		return googleDataExtractor;
	}

	public void setGoogleDataExtractor(GoogleDataExtractor googleDataExtractor)
	{
		this.googleDataExtractor = googleDataExtractor;
	}

	public DetailReport getDetailReport()
	{
		if (detailReport == null)
			detailReport = new DetailReport();
		return detailReport;
	}

	public void setDetailReport(DetailReport detailReport)
	{
		this.detailReport = detailReport;
	}

	public SummaryReport getSummaryReport()
	{
		if (summaryReport == null)
			summaryReport = new SummaryReport();
		return summaryReport;
	}

	public void setSummaryReport(SummaryReport summaryReport)
	{
		this.summaryReport = summaryReport;
	}

	public void setSearcher(BaseSearcher searcher)
	{
		this.searcher = searcher;
	}

	public static void main(String[] args)
    {
       new QueryTests().getDataRunTestsUpdateReports();
    }

	public void getDataRunTestsUpdateReports()
	{
		try
		{
			long start = System.currentTimeMillis();
			googleDataExtractor = getGoogleDataExtractor();
			detailReport = getDetailReport();
			summaryReport = getSummaryReport();

			Report previousSummaryReport = googleDataExtractor.getPreviousSummaryReport();
			Report previousDetailReport = googleDataExtractor.getPreviousDetailReport();

			populateAllQueriesFromGoogleSpreadsheets();
			runAllTests();

			detailReport.saveToLocalDisk();
			logger.info("Generated: " + detailReport);

			summaryReport.saveToLocalDisk();
			logger.info("Generated: " + summaryReport);

			Report detailDiffs = detailReport.createReportDiffs(previousDetailReport);
			logger.info("Generated: " + detailDiffs);

			Report summaryDiff = summaryReport.createReportDiffs(previousSummaryReport);
			logger.info("Generated: " + summaryDiff);

			detailDiffs.saveToLocalDisk();
			summaryDiff.saveToLocalDisk();

			if (!Properties.isLocalTest.get())
			{
				googleDataExtractor.publishReportToGoogleSpreadsheet(summaryReport);
				googleDataExtractor.publishReportToGoogleSpreadsheet(summaryDiff);
				googleDataExtractor.publishReportToGoogleSpreadsheet(detailReport);
				googleDataExtractor.publishReportToGoogleSpreadsheet(detailDiffs);
			}
			logger.info("All tests took: " + (System.currentTimeMillis() - start) + " ms");
		} catch (Throwable e)
		{
			logger.error("Couldn't proceed running query tests", e);
		}
	}

    private void runAllTests() throws Throwable
    {
		nextTest: for (String testName : queries.keySet()) {
			long start = System.currentTimeMillis();
			if (queries.get(testName) == null || queries.get(testName).size() == 0)
				continue nextTest;
			List<String> languages = getLanguageForTest(testName);
			indexer = getIndexer(testName);
			if (indexer == null)
				continue nextTest;
			searcher = getSearcher();
			logger.info("Processing: " + testName);
			indexer.indexData(languages);
	        languages.add("en");//for search add en fields
			runTest(testName, languages);
			logger.info(testName + " took: " + (System.currentTimeMillis() - start) + " ms");
		}
    }

    private BaseSearcher getSearcher()
	{
    	if (Properties.engineType.get().equalsIgnoreCase("solr"))
			return new SolrSearcher();
		else if (Properties.engineType.get().equalsIgnoreCase("es"))
			return new ElasticsearchSearcher();
		else
		{
			logger.error("No support for the engine type: " + Properties.engineType.get());
			return null;
		}
	}

	protected List<String> getLanguageForTest(String testName)
    {
        List<String> languages = Lists.newArrayList();
        Map<String, String> languageExpansionBasedOnTestNames = Properties.languageExpansionBasedOnTestNames.getMap();
        String languagePartOfTestName = testName.split(SHEET_TAB_NAME_DELIMITER)[0];
        String lanuguagesAsAString = languageExpansionBasedOnTestNames.get(languagePartOfTestName);
        String[] languagesFromMap = lanuguagesAsAString.split(MAP_VALUE_DELIMITER);
        for(String lanuguage: languagesFromMap)
        	languages.add(lanuguage);
        return languages;
    }

	protected void populateAllQueriesFromGoogleSpreadsheets()
	{
		for (String dataset : Properties.validDataSetsId.get())
		{
			for (String queryCategory : Properties.queryCategories.get())
			{
				String testKey = dataset + SHEET_TAB_NAME_DELIMITER + queryCategory;
				Queries queriesHolder = null;
				queriesHolder = new Queries(dataset, queryCategory, googleDataExtractor);
				queriesHolder.populateFromGoogleSpreadsheets();
				queries.put(testKey, queriesHolder.getQueryToIdMap());
			}
		}
	}

    public void runTest(String testName, List<String> languages) throws Throwable
    {
        Map<String, Set<String>> queryToIds = queries.get(testName);
        Set<String> titlesTested = Sets.newHashSet();
        Map<String, String> titleIdToName = indexer.getTitleToIds();

        List<Double> precisionList = Lists.newArrayList();
        List<Double> recallList = Lists.newArrayList();
        List<Double> fMeasureList = Lists.newArrayList();
        Map<ResultType, Integer> counters = Maps.newLinkedHashMap();
        
        for (String q : queryToIds.keySet()) {
            Set<String> relevantDocuments = queryToIds.get(q);
            if(relevantDocuments!=null)
            	titlesTested.addAll(relevantDocuments);

            Set<String> results = searcher.getResults(q, languages, getDatasetId(testName));

            summaryReport.updateStatistic(relevantDocuments, results, precisionList, recallList, fMeasureList);
            detailReport.updateReport(queryToIds, q, results, testName, titleIdToName, counters);
        }
        summaryReport.updateSummaryReport(testName, titlesTested.size(), queryToIds.size(), precisionList, recallList, fMeasureList, counters);

    }

	private static BaseIndexer getIndexer(String testId) {
		String datasetId = getDatasetId(testId);
		String inputFileName = Properties.dataDir.get() + datasetId + ".tsv";
		if (new File(inputFileName).exists())
			if (Properties.engineType.get().equalsIgnoreCase("solr"))
				return new SolrIndexer(inputFileName, datasetId);
			else if (Properties.engineType.get().equalsIgnoreCase("es"))
				return new ElasticsearchIndexer(inputFileName, datasetId);
			else
			{
				logger.error("No support for the engine type: " + Properties.engineType.get());
				return null;
			}
		else {
			logger.error("Data doesn't exist: " + inputFileName + " skipping the test " + testId);
			return null;
		}
	}

	private static String getDatasetId(String testId)
	{
		return testId.substring(0, testId.lastIndexOf('-'));
	}
}
