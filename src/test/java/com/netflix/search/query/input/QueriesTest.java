package com.netflix.search.query.input;

import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Maps;

import com.google.common.collect.Sets;
import com.netflix.search.query.report.google.GoogleDataExtractor;

public class QueriesTest {
	private static final String DATASET_ID = "english-video";
	private static final String TEST1 = "regular";
	private static final String ID1 = "123";
	private static final String ID2 = "1234";
	private static final String SPANISH_TITLE = "title es";
	private static final String ENGLISH_TITLE = "title en";
	private static final String Q1 = "abc";

	@Test
	void createDocTest()
	{
		GoogleDataExtractor titleExtractor = Mockito.mock(GoogleDataExtractor.class);
		Map<String, Map<Integer, TitleWithQueries>> mapOfQueriesToTitles = Maps.newHashMap();
		
		Map<Integer, TitleWithQueries> titlesWithQueries = Maps.newHashMap();
		TitleWithQueries titleWithQueries = new TitleWithQueries(DATASET_ID);
		titleWithQueries.setValue(TitleWithQueries.ID, ID1);
		titleWithQueries.setValue(TitleWithQueries.TITLE_EN, ENGLISH_TITLE);
		titleWithQueries.setValue(TitleWithQueries.TITLE_LOCALE, SPANISH_TITLE);
		titleWithQueries.setValue(TitleWithQueries.Q_ + "regular", Q1);
		titlesWithQueries.put(1, titleWithQueries);

		TitleWithQueries titleWithQueries2 = new TitleWithQueries(DATASET_ID);
		titleWithQueries2.setValue(TitleWithQueries.ID, ID2);
		titleWithQueries2.setValue(TitleWithQueries.TITLE_EN, ENGLISH_TITLE);
		titleWithQueries2.setValue(TitleWithQueries.TITLE_LOCALE, SPANISH_TITLE);
		titleWithQueries2.setValue(TitleWithQueries.Q_ + "regular", Q1);
		titlesWithQueries.put(2, titleWithQueries2);

		mapOfQueriesToTitles.put(DATASET_ID, titlesWithQueries);

		Mockito.when(titleExtractor.getTitlesWithQueriesPerDataset()).thenReturn(mapOfQueriesToTitles);

		Queries queries = new Queries(DATASET_ID, TEST1, titleExtractor);
		queries.populateFromGoogleSpreadsheets();

		Map<String, Set<String>> queryToIdMap = queries.getQueryToIdMap();

		Map<String, Set<String>> expectedQueryToIdMap = Maps.newHashMap();
		Set<String> titles = Sets.newHashSet();
		titles.add(ID1+"_"+DATASET_ID);
		titles.add(ID2+"_"+DATASET_ID);
		expectedQueryToIdMap.put(Q1, titles);

		Assert.assertEquals(queryToIdMap, expectedQueryToIdMap);
	}
}
