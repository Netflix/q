package com.netflix.search.query.report.summary;

import java.util.Map;

import com.netflix.search.query.report.ReportItem;
import com.netflix.search.query.report.ResultType;

public class SummaryReportItem extends ReportItem {
    public SummaryReportItem(String name, Integer titles, Integer queries, Double precision, Double recall, Double fmeasure, Map<ResultType, Integer> counters) {
        super();

        namedValues.put("name", name);
        namedValues.put("titles", String.valueOf(titles));
        namedValues.put("queries", String.valueOf(queries));
        for (ResultType type : ResultType.values()) {
            Integer counter = counters.get(type);
            if (counter == null)
                counter = 0;
            namedValues.put(type.toString(), String.valueOf(counter));
        }
        namedValues.put("precision", (String.format("%.2f", (precision * 100)) + SummaryReport.PERCENT_SIGN));
        namedValues.put("recall", (String.format("%.2f", (recall * 100)) + SummaryReport.PERCENT_SIGN));
        namedValues.put("fmeasure", (String.format("%.2f", (fmeasure * 100)) + SummaryReport.PERCENT_SIGN));
        key = getKeyFromNamedValues();
    }

    public SummaryReportItem(Map<String, String> namedValues) {
        super(namedValues);
    }

    public SummaryReportItem() {
        super();
    }

    @Override
    protected void appendKeyFromNamedValues(String headerValue, String value)
    {
        if (headerValue.equals("name"))
            key = value;
    }

    @Override
    protected String getKeyFromNamedValues()
    {
        return namedValues.get("name");
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SummaryReportItem other = (SummaryReportItem) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

}
