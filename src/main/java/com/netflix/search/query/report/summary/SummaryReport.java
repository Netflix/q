package com.netflix.search.query.report.summary;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.netflix.search.query.report.Report;
import com.netflix.search.query.report.ReportItem;
import com.netflix.search.query.report.ResultType;

public class SummaryReport extends Report {
    
    public static final String[] SUMMARY_REPORT_HEADER = { "name", "titles", "queries", "supersetResultsFailed", "differentResultsFailed", "noResultsFailed", "successQ", "precision", "recall", "fmeasure", "comments" };
    
    public static final String PERCENT_SIGN = "%";

    public SummaryReport(List<ReportItem> items) {
        super();
        this.items = items;
    }

    public SummaryReport() {
        super();
    }

    @Override
    protected String[] getHeader()
    {
        return SUMMARY_REPORT_HEADER;
    }

    @Override
    protected String getReportName()
    {
        return "summary";
    }
    
    @Override
	protected Report newReport(List<ReportItem> items)
    {
        return new SummaryReportDiff(items);
    }
    
    @Override
    protected ReportItem getDiffForReportItem(ReportItem previousItem, ReportItem currentItem)
    {
        ReportItem returnValue = new SummaryReportItem();
        if (previousItem == null) {
            if (currentItem != null)
                returnValue = new SummaryReportItem(new LinkedHashMap<String, String>(currentItem.namedValues));
        } else {
            for (String name : previousItem.namedValues.keySet()) {
                String previousValue = previousItem.namedValues.get(name);
                if (name.equals("comments")) continue;
                if (name.equals("name")) {
                    returnValue.namedValues.put("name", previousValue);
                } else {
                    if (currentItem == null) {
                        returnValue.namedValues.put("comments", "dropped");
                    } else {
                        String currentValue = currentItem.namedValues.get(name);
                        if (previousValue.contains(PERCENT_SIGN)) {
                            previousValue = previousValue.replaceAll(PERCENT_SIGN, "");
                            currentValue = currentValue.replaceAll(PERCENT_SIGN, "");
                            Double previousNumeric = Double.valueOf(previousValue);
                            Double currentNumeric = Double.valueOf(currentValue);
                            Double difference = currentNumeric - previousNumeric;
                            returnValue.namedValues.put(name, (String.format("%.2f", (difference)) + PERCENT_SIGN));
                        } else {
                            Integer previousNumeric = Integer.valueOf(previousValue);
                            Integer currentNumeric = Integer.valueOf(currentValue);
                            Integer difference = currentNumeric - previousNumeric;
                            returnValue.namedValues.put(name, String.valueOf(difference));
                        }
                    }
                }
            }
        }
        return returnValue;
    }

    
    public void updateStatistic(Set<String> relevantDocuments, Set<String> results, List<Double> precisionList, List<Double> recallList, List<Double> fMeasureList)
    {
        Double precision = 0d;
        Double recall = 0d;
        Double fMeasure = 0d;

		if (results != null && relevantDocuments != null) {
			Set<String> relevantRetrievedResults = Sets.intersection(results, relevantDocuments);
			if (results.size() != 0)
				precision = (double) relevantRetrievedResults.size() / (double) results.size();
			if (relevantDocuments.size() != 0)
				recall = (double) relevantRetrievedResults.size() / (double) relevantDocuments.size();
			if (precision != 0 || recall != 0)
				fMeasure = 2 * ((precision * recall) / (precision + recall));
		}

		precisionList.add(precision);
        recallList.add(recall);
        fMeasureList.add(fMeasure);
    }

    public void updateSummaryReport(String testName, int titlesTested, int queriesTested, List<Double> precisionList, List<Double> recallList, List<Double> fMeasureList,
            Map<ResultType, Integer> counters)
    {
        items.add(new SummaryReportItem(testName, titlesTested, queriesTested, calculateAverage(precisionList), calculateAverage(recallList), calculateAverage(fMeasureList), counters));
    }

    private static double calculateAverage(List<Double> scores)
    {
        Double sum = 0d;
        if (!scores.isEmpty()) {
            for (Double mark : scores) {
                sum += mark;
            }
            return sum.doubleValue() / scores.size();
        }
        return sum;
    }
}
