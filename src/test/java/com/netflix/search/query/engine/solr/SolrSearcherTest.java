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
		Assert.assertEquals(urlForGettingDoc, "http://localhost:8983/solr/qtest/select?q=%22abc%22&defType=edismax&lowercaseOperators=false&rows=100000&qs=10&fl=id%2C+title_en&sort=id+DESC&qf=title_es&fq=query_testing_type%3Atest1&wt=json");
	}
}
