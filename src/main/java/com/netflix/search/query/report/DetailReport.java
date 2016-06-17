package com.netflix.search.query.report;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class DetailReport extends Report {
    private static final String SEPARATOR = "~~~";
	public static final String[] DETAIL_REPORT_HEADER = { "name", "failure", "query", "expected", "actual", "comments" };

    public DetailReport(List<ReportItem> items) {
        super();
        this.items=items;
    }

    public DetailReport() {
        super();
    }

    @Override
    protected String[] getHeader()
    {
        return DETAIL_REPORT_HEADER;
    }
    
    @Override
    protected String getReportName()
    {
        return "details";
    }
    
    @Override
    Report newReport(List<ReportItem> items)
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
                    items.add(new DetailReportItem(testName, ResultType.supersetResultsFailed, q, expectedTitles, actualTitles));
                    updateCounter(counters, ResultType.supersetResultsFailed);
                } else {
                    items.add(new DetailReportItem(testName, ResultType.differentResultsFailed, q, expectedTitles, actualTitles));
                    updateCounter(counters, ResultType.differentResultsFailed);
                }
            } else {
                items.add(new DetailReportItem(testName, ResultType.noResultsFailed, q, expectedTitles, "NONE"));
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
                returnValue = new DetailReportItem(new LinkedHashMap<String, String>(previousItem.namedValues));
                returnValue.namedValues.put("comments", "FIXED");
            }
        } else {
            returnValue = new DetailReportItem(new LinkedHashMap<String, String>(currentItem.namedValues));
            returnValue.namedValues.put("comments", "NEW");
        }
        return returnValue;
    }

}
