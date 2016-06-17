package com.netflix.search.query.engine.es;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netflix.search.query.Properties;
import com.netflix.search.query.engine.BaseSearcher;

public class ElasticsearchSearcher extends BaseSearcher {

	@Override
	public String getUrlForGettingDoc(String q, List<String> languages, String dataSetId)
	{
		return getServerUrl() + "/"+Properties.esDocType.get()+"/_search";
	}

	@Override
	public String getJsonForQuery(String q, List<String> languages, String dataSetId) throws JsonProcessingException
	{
		Map<String, Object> multiMatchObject = Maps.newHashMap();
		multiMatchObject.put("query", q);
		multiMatchObject.put("type", "best_fields");
		multiMatchObject.put("operator", "and");
		multiMatchObject.put("fields", getQueryFields(languages).split("\\s+"));

		Map<String, Object> queryObject = Maps.newHashMap();
		queryObject.put("multi_match", multiMatchObject);
		
		Map<String, Object> termObject = Maps.newHashMap();
		termObject.put(Properties.docTypeFieldName.get(), dataSetId);
		
		Map<String, Object> filterObject = Maps.newHashMap();
		filterObject.put("term", termObject);

		Map<String, Object> filteredObject = Maps.newHashMap();
		filteredObject.put("query", queryObject);
		filteredObject.put("filter", filterObject);

		Map<String, Object> sortFieldObject = Maps.newHashMap();
		sortFieldObject.put("order", "desc");

		Map<String, Object> sortObject = Maps.newHashMap();
		sortObject.put(Properties.idField.get(), sortFieldObject);

		Map<String, Object> topLevelQueryObject = Maps.newHashMap();
		topLevelQueryObject.put("filtered", filteredObject);

		Map<String, Object> topNode = Maps.newHashMap();
		topNode.put("query", topLevelQueryObject);
		topNode.put("sort", sortObject);

		JsonNode node = new ObjectMapper().valueToTree(topNode);
		StringBuilder jsonString = new StringBuilder();
		
		jsonString.append(new ObjectMapper().writeValueAsString(node));
		return jsonString.toString();
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
