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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
import com.google.common.collect.Sets;
import com.netflix.search.query.Properties;
import com.netflix.search.query.engine.BaseIndexer;
import com.netflix.search.query.engine.BaseSearcher;

public class SolrSearcher extends BaseSearcher {

	public String getUrlForGettingDoc(String q, List<String> languages, String dataSetId)
	{
		List<NameValuePair> parameters = Lists.newArrayList();

		parameters.add(new BasicNameValuePair("q", getPhraseQueryString(q)));
		parameters.add(new BasicNameValuePair("defType", "edismax"));
		parameters.add(new BasicNameValuePair("lowercaseOperators", "false"));
		parameters.add(new BasicNameValuePair("rows", "100000"));
		parameters.add(new BasicNameValuePair("qs", "10"));
		parameters.add(new BasicNameValuePair("fl", Properties.idField.get() + ", " + Properties.titleFields.get().get(0) + "_en"));
		parameters.add(new BasicNameValuePair("sort", Properties.idField.get() + " DESC"));
		parameters.add(new BasicNameValuePair("qf", getQueryFields(languages)));
		parameters.add(new BasicNameValuePair("fq", Properties.docTypeFieldName.get() + ":" + dataSetId));
		parameters.add(new BasicNameValuePair("wt", "json"));

		return getServerUrl() + "/select?" + URLEncodedUtils.format(parameters, BaseIndexer.ENCODING);
	}

	public Set<String> getResultsFromServerResponse(String output) throws IOException, JsonProcessingException
	{
		Set<String> results = Sets.newHashSet();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(output);
		JsonNode arrNode = actualObj.get("response").get("docs");
		if (arrNode.isArray())
		{
			for (final JsonNode objNode : arrNode)
			{
				results.add(objNode.get("id").textValue());
			}
		}
		return results;
	}

}
