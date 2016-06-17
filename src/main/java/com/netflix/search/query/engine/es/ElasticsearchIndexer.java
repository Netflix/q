package com.netflix.search.query.engine.es;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.util.Lists;
import com.netflix.search.query.Properties;
import com.netflix.search.query.engine.BaseIndexer;

public class ElasticsearchIndexer extends BaseIndexer {

    public ElasticsearchIndexer(String inputFileName, String testName) {
        super(inputFileName, testName);
    }

    public static void main(String[] args) throws Throwable
	{
    	List<String> languages = Lists.newArrayList();
    	languages.add("en");
		new ElasticsearchIndexer("/Users/iprovalov/stash/oss/q/data/q_tests/japanese-video.tsv", "english-video").indexData(languages);
	}
    
	@Override
	protected String getUrlForAddingDoc(Map<String, Object> doc)
	{
		return getServerUrl()+"/"+Properties.esDocType.get()+"/" + doc.get("id").toString();
	}

	@Override
	protected String getUrlForCommitting()
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