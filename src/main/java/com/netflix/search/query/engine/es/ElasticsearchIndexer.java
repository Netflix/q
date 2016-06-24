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