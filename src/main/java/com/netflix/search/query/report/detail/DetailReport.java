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
package com.netflix.search.query.report.detail;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.netflix.search.query.report.Report;
import com.netflix.search.query.report.ReportItem;
import com.netflix.search.query.report.ReportType;
import com.netflix.search.query.report.ResultType;

public class DetailReport extends Report {
	public static final String NEW = "NEW";
	public static final String NONE = "NONE";
	public static final String FIXED = "FIXED";
	private static final String SEPARATOR = "~~~";

    public DetailReport(List<ReportItem> items) {
        super();
        this.setItems(items);
    }

    public DetailReport() {
        super();
    }

    @Override
	public ReportType getReportType()
	{
		return ReportType.details;
	}
    
    @Override
    protected String getReportName()
    {
        return ReportType.details.toString();
    }
    
    @Override
	protected Report newReport(List<ReportItem> items)
    {
        return new DetailReportDiff(items);
    }

    public void updateReport(Map<String, Set<String>> queryToIds, String q, Set<String> results, String testName, Map<String, String> titleIdToName, Map<ResultType, Integer> counters)
    {
        if (!results.equals(queryToIds.get(q))) {
            String expectedTitles = getTitles(queryToIds.get(q), titleIdToName);
            if (results.size() > 0) {
                Set<String> intersection = Sets.intersection(queryToIds.get(q), results);
                Set<String> uniqExpected = new HashSet<String>(queryToIds.get(q));
                uniqExpected.removeAll(intersection);
                expectedTitles = getTitles(uniqExpected, titleIdToName);
                Set<String> uniqActual = new HashSet<String>(results);
                uniqActual.removeAll(intersection);
                String actualTitles = getTitles(uniqActual, titleIdToName);
                if (results.containsAll(queryToIds.get(q))) {
                	getItems().add(new DetailReportItem(testName, ResultType.supersetResultsFailed, q, expectedTitles, actualTitles));
                    updateCounter(counters, ResultType.supersetResultsFailed);
                } else {
                	getItems().add(new DetailReportItem(testName, ResultType.differentResultsFailed, q, expectedTitles, actualTitles));
                    updateCounter(counters, ResultType.differentResultsFailed);
                }
            } else {
            	getItems().add(new DetailReportItem(testName, ResultType.noResultsFailed, q, expectedTitles, NONE));
                updateCounter(counters, ResultType.noResultsFailed);
            }
        } else
            updateCounter(counters, ResultType.successQ);
    }

    private void updateCounter(Map<ResultType, Integer> counters, ResultType type)
    {
        Integer failureCounter = counters.get(type);
        if (failureCounter == null)
            failureCounter = 0;
        counters.put(type, ++failureCounter);
    }

	private String getTitles(Set<String> ids, Map<String, String> titleIdToName) {
		String returnValue = "";
		Joiner joiner = Joiner.on(SEPARATOR);
		if (ids != null && titleIdToName != null && titleIdToName.keySet() != null) {
			Set<String> intersection = Sets.intersection(ids, titleIdToName.keySet());
			Map<String, String> copy = new LinkedHashMap<String, String>(titleIdToName);
			copy.keySet().retainAll(intersection);
			returnValue = joiner.join(copy.values());
		}
		return returnValue;
	}

    @Override
    protected ReportItem getDiffForReportItem(ReportItem previousItem, ReportItem currentItem)
    {
        ReportItem returnValue = null;
        if (previousItem != null) {
            if (currentItem != null) {
                // TODO: DO NOTHING, they are essentially the same, ignoring the lists for now
            } else {
                returnValue = new DetailReportItem(new LinkedHashMap<String, String>(previousItem.getNamedValues()));
                returnValue.setValue(DetailReportHeader.comments.toString(), FIXED);
            }
        } else {
            returnValue = new DetailReportItem(new LinkedHashMap<String, String>(currentItem.getNamedValues()));
            returnValue.setValue(DetailReportHeader.comments.toString(), NEW);
        }
        return returnValue;
    }

}
