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
package com.netflix.search.query.engine.es;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class ElasticsearchSearcherTest {
	private static final String LOCALE = "es";
	private static final String TEST1 = "test1";

	@Test
	void emptyReportsTest() throws IOException
	{
		List<String> languages = Lists.newArrayList();
		languages.add(LOCALE);
		ElasticsearchSearcher searcher = new ElasticsearchSearcher();
		String json = searcher.getJsonForQuery("abc", languages, TEST1);

		// OpenSearch/ES 7+ typeless phrase query: bool with a multi_match (phrase, slop=10) and a term filter on the dataset.
		// Compared as parsed JSON because the query is built from HashMaps whose key serialization order is not stable.
		String expected = "{\"query\":{\"bool\":{\"must\":[{\"multi_match\":{\"query\":\"abc\",\"type\":\"phrase\",\"slop\":10,\"fields\":[\"title_es\",\"title_aka_es\"]}}],\"filter\":[{\"term\":{\"query_testing_type\":\"test1\"}}]}},\"size\":100000,\"_source\":false}";
		ObjectMapper mapper = new ObjectMapper();
		Assert.assertEquals(mapper.readTree(json), mapper.readTree(expected));
	}
}
