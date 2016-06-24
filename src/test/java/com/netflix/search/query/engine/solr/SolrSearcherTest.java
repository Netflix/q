package com.netflix.search.query.engine.solr;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class SolrSearcherTest {
	private static final String LOCALE = "es";
	private static final String TEST1 = "test1";

	@Test
	void getUrlTest()
	{
		List<String> languages = Lists.newArrayList();
		languages.add(LOCALE);
		SolrSearcher searcher = new SolrSearcher();
		String urlForGettingDoc = searcher.getUrlForGettingDoc("abc", languages, TEST1);
		Assert.assertEquals(urlForGettingDoc, "http://localhost:8080/search/core1/select?q=%22abc%22&defType=edismax&lowercaseOperators=false&rows=100000&qs=10&fl=id%2C+title_en&sort=id+DESC&qf=title_es&fq=query_testing_type%3Atest1&wt=json");
	}
}
