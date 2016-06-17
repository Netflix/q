package com.netflix.search.query.engine.solr;

import java.util.Map;

import com.netflix.search.query.engine.BaseIndexer;

public class SolrIndexer extends BaseIndexer{

    
    public SolrIndexer(String inputFileName, String testName) {
        super(inputFileName, testName);
    }

	@Override
	protected String getUrlForAddingDoc(Map<String, Object> doc)
	{
		return getServerUrl()+"/update";
	}

	@Override
	protected String getUrlForCommitting()
	{
		return getServerUrl()+"/update?commit=true";
	}
    
}