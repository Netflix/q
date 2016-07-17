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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;

public class ElasticsearchSearcherTest {
	private static final String LOCALE = "es";
	private static final String TEST1 = "test1";

	@Test
	void emptyReportsTest() throws JsonProcessingException
	{
		List<String> languages = Lists.newArrayList();
		languages.add(LOCALE);
		ElasticsearchSearcher searcher = new ElasticsearchSearcher();
		String json = searcher.getJsonForQuery("abc", languages, TEST1);
		Assert.assertEquals(json, "{\"query\":{\"filtered\":{\"filter\":{\"term\":{\"query_testing_type\":\"test1\"}},\"query\":{\"multi_match\":{\"query\":\"abc\",\"type\":\"best_fields\",\"fields\":[\"title_es\"],\"operator\":\"and\"}}}},\"sort\":{\"id\":{\"order\":\"desc\"}}}");
	}
}
