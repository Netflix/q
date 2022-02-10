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
package com.netflix.search.query.input;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netflix.search.query.report.google.GoogleDataExtractor;

public class Queries {
    private Map<String, Set<String>> queryToIdMap = Maps.newHashMap();
    private String datasetId;
    private String queryCategory;
    private Map<String, Map<Integer, TitleWithQueries>> titlesWithQueriesPerDataset;

    public Queries(String datasetId, String queryCategory, Map<String, Map<Integer, TitleWithQueries>> titlesWithQueriesPerDataset) {
        this.queryToIdMap = Maps.newHashMap();
        this.datasetId = datasetId;
        this.queryCategory = queryCategory;
        this.titlesWithQueriesPerDataset = titlesWithQueriesPerDataset;
    }

    public void populateFromGoogleSpreadsheets()
    {
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