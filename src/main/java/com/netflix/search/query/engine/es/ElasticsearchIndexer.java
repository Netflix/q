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

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.netflix.search.query.Properties;
import com.netflix.search.query.engine.BaseIndexer;

public class ElasticsearchIndexer extends BaseIndexer {

    public ElasticsearchIndexer(String inputFileName, String testName) {
        super(inputFileName, testName);
    }

	@Override
	public String getUrlForAddingDoc(Map<String, Object> doc)
	{
		return getServerUrl()+"/"+Properties.esDocType.get()+"/" + doc.get("id").toString();
	}

	@Override
	public String getUrlForCommitting()
	{
		return getServerUrl()+"/_flush";
	}
    
	@Override
	public StringBuilder getJsonStringOfDoc(JsonNode node)
	{
		StringBuilder jsonString = new StringBuilder();
		nodeAsString(node, jsonString);
		return jsonString;
	}
}