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
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Maps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netflix.search.query.Properties;
import com.netflix.search.query.engine.BaseIndexer;

public class ElasticsearchIndexerTest {
	private static final String LOCALE = "es";
	private static final String ID = "123";
	private static final String ALT_TITLE = "title es 2";
	private static final String SPANISH_TITLE = "title es";
	private static final String ENGLISH_TITLE = "title en";
	private static final String TEST1 = "test1";

	@Test
	void createDocTest()
	{

		BaseIndexer indexer = new ElasticsearchIndexer("", TEST1);
		List<String> languages = Lists.newArrayList();
		languages.add(LOCALE);
		Map<String, Object> createdDoc = indexer.createDoc(ID, ENGLISH_TITLE, SPANISH_TITLE, ALT_TITLE, languages);
		Map<String, Object> expectedDoc = Maps.newHashMap();
		expectedDoc.put(Properties.idField.get(), ID + "_" + TEST1);
		expectedDoc.put(Properties.titleFields.get().get(0) + "_en", ENGLISH_TITLE);
		Set<Object> localizedTitles = Sets.newHashSet();
		localizedTitles.add(SPANISH_TITLE);
		expectedDoc.put(Properties.titleFields.get().get(0) + "_es", localizedTitles);
		expectedDoc.put(Properties.docTypeFieldName.get(), TEST1);

		Assert.assertEquals(createdDoc, expectedDoc);

		StringBuilder jsonStringOfDoc = indexer.getJsonStringOfDoc(new ObjectMapper().valueToTree(createdDoc));
		Assert.assertEquals(jsonStringOfDoc.toString(), "{\"query_testing_type\":\"test1\",\"title_en\":\"title en\",\"id\":\"123_test1\",\"title_es\":[\"title es\"]}");
		
		String urlForAddingDoc = indexer.getUrlForAddingDoc(createdDoc);
		Assert.assertEquals(urlForAddingDoc, "http://localhost:8983/solr/qtest/test_doc/123_test1");

		String urlForCommitting = indexer.getUrlForCommitting();
		Assert.assertEquals(urlForCommitting, "http://localhost:8983/solr/qtest/_flush");

	}
}
