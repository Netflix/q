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
package com.netflix.search.query.report.summary;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.netflix.search.query.report.Report;
import com.netflix.search.query.report.ReportItem;
import com.netflix.search.query.report.ReportType;
import com.netflix.search.query.report.ResultType;

public class SummaryReport extends Report {
    
    private static final String DROPPED = "dropped";
	public static final String PERCENT_SIGN = "%";

    public SummaryReport(List<ReportItem> items) {
        super();
        this.setItems(items);
    }

    public SummaryReport() {
        super();
    }

    @Override
	public ReportType getReportType()
	{
		return ReportType.summary;
	}
    
    @Override
    protected String getReportName()
    {
        return ReportType.summary.toString();
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
                returnValue = new SummaryReportItem(new LinkedHashMap<String, String>(currentItem.getNamedValues()));
        } else {
            for (String name : previousItem.getNamedValues().keySet()) {
                String previousValue = previousItem.getNamedValues().get(name);
                if (name.equals(SummaryReportHeader.comments.toString())) continue;
                if (name.equals(SummaryReportHeader.name.toString())) {
                    returnValue.getNamedValues().put(SummaryReportHeader.name.toString(), previousValue);
                } else {
                    if (currentItem == null) {
                        returnValue.getNamedValues().put(SummaryReportHeader.comments.toString(), DROPPED);
                    } else {
                        String currentValue = currentItem.getNamedValues().get(name);
                        if (previousValue.contains(PERCENT_SIGN)) {
                            previousValue = previousValue.replaceAll(PERCENT_SIGN, "");
                            currentValue = currentValue.replaceAll(PERCENT_SIGN, "");
                            Double previousNumeric = Double.valueOf(previousValue);
                            Double currentNumeric = Double.valueOf(currentValue);
                            Double difference = currentNumeric - previousNumeric;
                            returnValue.setValue(name, (String.format("%.2f", (difference)) + PERCENT_SIGN));
                        } else {
                            Integer previousNumeric = Integer.valueOf(previousValue);
                            Integer currentNumeric = Integer.valueOf(currentValue);
                            Integer difference = currentNumeric - previousNumeric;
                            returnValue.setValue(name, String.valueOf(difference));
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
    	getItems().add(new SummaryReportItem(testName, titlesTested, queriesTested, calculateAverage(precisionList), calculateAverage(recallList), calculateAverage(fMeasureList), counters));
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
