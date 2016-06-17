package com.netflix.search.query.report;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Queries {
    private Map<String, Set<String>> queryToIdMap = Maps.newHashMap();
    private String datasetId;
    private String queryCategory;
    private GoogleDataExtractor titleExtractor;

    public Queries(String datasetId, String queryCategory, GoogleDataExtractor titleExtractor) {
        this.queryToIdMap = Maps.newHashMap();
        this.datasetId = datasetId;
        this.queryCategory = queryCategory;
        this.titleExtractor = titleExtractor;
    }

    public void populateFromGoogleSpreadsheets()
    {
        Map<String, Map<Integer, TitleWithQueries>> titlesWithQueriesPerDataset = titleExtractor.getTitlesWithQueriesPerDataset();
        Map<Integer, TitleWithQueries> titlesWithQueries = titlesWithQueriesPerDataset.get(datasetId);
        if (titlesWithQueries != null)
            for (Integer row : titlesWithQueries.keySet()) {
                TitleWithQueries titleWithQueries = titlesWithQueries.get(row);
                Set<String> queriesForThisCategory = titleWithQueries.getQueriesByCategory().get(queryCategory);
                if (queriesForThisCategory != null)
                    for (String q : queriesForThisCategory)
                        put(q, titleWithQueries.getId());
            }
    }

    public Map<String, Set<String>> getQueryToIdMap()
    {
        return queryToIdMap;
    }

    public void put(String query, String id)
    {
        if (query != null && !query.isEmpty()) {
            Set<String> ids = queryToIdMap.get(query);
            if (ids == null)
                ids = Sets.newHashSet();
            ids.add(id);
            queryToIdMap.put(query, ids);
        }
    }

}