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
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netflix.search.query.Properties;
import com.netflix.search.query.engine.BaseSearcher;

public class ElasticsearchSearcher extends BaseSearcher {

	@Override
	public String getServerUrl()
	{
		return "http://" + Properties.engineHost.get() + ":" + Properties.enginePort.get() + "/" + Properties.engineIndexName.get();
	}

	@Override
	public String getUrlForGettingDoc(String q, List<String> languages, String dataSetId)
	{
		return getServerUrl() + "/_search";
	}

	/**
	 * Mirrors SolrSearcher: the query is treated as a phrase (edismax with a quoted q and qs=10), matched across
	 * all title fields for the languages under test (dis_max), restricted to the dataset via the type field.
	 */
	@Override
	public String getJsonForQuery(String q, List<String> languages, String dataSetId) throws JsonProcessingException
	{
		Map<String, Object> multiMatch = Maps.newHashMap();
		multiMatch.put("query", q == null ? "" : q.replaceAll("[\"|\\\\]", ""));
		multiMatch.put("type", "phrase");
		multiMatch.put("slop", 10);
		multiMatch.put("fields", getQueryFields(languages).split("\\s+"));

		Map<String, Object> bool = Maps.newHashMap();
		bool.put("must", Lists.newArrayList(ImmutableMap.of("multi_match", multiMatch)));
		bool.put("filter", Lists.newArrayList(ImmutableMap.of("term", ImmutableMap.of(Properties.docTypeFieldName.get(), dataSetId))));

		Map<String, Object> top = Maps.newHashMap();
		top.put("query", ImmutableMap.of("bool", bool));
		top.put("size", 100000);
		top.put("_source", false);
		return new ObjectMapper().writeValueAsString(top);
	}

	@Override
	public Set<String> getResultsFromServerResponse(String output) throws JsonProcessingException, IOException
	{
		Set<String> results = Sets.newHashSet();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(output);
		JsonNode arrNode = actualObj.get("hits").get("hits");
		if (arrNode.isArray())
		{
			for (final JsonNode objNode : arrNode)
			{
				results.add(objNode.get("_id").textValue());
			}
		}
		return results;
		
	}
}
