package com.netflix.search.query.engine;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.search.query.Properties;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public abstract class BaseSearcher {

    public static final Logger logger = LoggerFactory.getLogger(BaseSearcher.class);

	private Client client = Client.create();

	public BaseSearcher() {
	}

	public Set<String> getResults(String q, List<String> languages, String dataSetId) throws Throwable
	{
		String urlForGettingDoc = getUrlForGettingDoc(q, languages, dataSetId);

		if (Properties.isPrintUrl.get())
			logger.info(urlForGettingDoc);

		String jsonString = getJsonForQuery(q, languages, dataSetId);

		WebResource webResource = client.resource(urlForGettingDoc);
		ClientResponse response = null;

		if (jsonString != null)
			response = webResource.type("application/json").post(ClientResponse.class, jsonString);
		else
			response = webResource.get(ClientResponse.class);
		if (response == null || (response.getStatus() != 201 && response.getStatus() != 200))
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		String output = response.getEntity(String.class);
		Set<String> results = getResultsFromServerResponse(output);
		return results;
	}

	public static String getPhraseQueryString(String q)
	{
		if (q == null)
			return null;
		return "\"" + q.replaceAll("[\"|\\\\]", "") + "\"";
	}

	public static String getQueryFields(List<String> localeList)
	{
		StringBuffer sb = new StringBuffer();
		if (localeList != null)
		{
			for (String fieldName : Properties.titleFields.get())
				addNonDefaultLocaleTitleFieldName(localeList, sb, fieldName);
		}
		return sb.toString().substring(0, sb.length()).trim();
	}

	protected static void addNonDefaultLocaleTitleFieldName(List<String> localeList, StringBuffer sb, String fieldName)
	{
		for (String locale : localeList)
		{
			sb.append(fieldName + "_" + locale + " ");
		}
	}

	public abstract String getUrlForGettingDoc(String q, List<String> languages, String dataSetId);
	public abstract Set<String> getResultsFromServerResponse(String output) throws IOException, JsonProcessingException;

	public String getJsonForQuery(String q, List<String> languages, String dataSetId) throws JsonProcessingException
	{
		return null;
	}

	public String getServerUrl()
	{
		return "http://" + Properties.engineHost.get() + ":" + Properties.enginePort.get() + "/" + Properties.engineServlet.get() + "/" + Properties.engineIndexName.get();
	}

}
