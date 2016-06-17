package com.netflix.search.query.report.detail;

import java.util.Map;

import com.netflix.search.query.report.ReportItem;
import com.netflix.search.query.report.ResultType;

public class DetailReportItem extends ReportItem {

    public DetailReportItem(String name, ResultType failure, String query, String expected, String actual) {
        super();
        namedValues.put("name", name);
        namedValues.put("failure", failure.toString());
        namedValues.put("query", query);
        namedValues.put("expected", expected);
        namedValues.put("actual", actual);
        key = name + "_" + failure.toString() + "_" + query;
    }

    public DetailReportItem(Map<String, String> namedValues) {
        super(namedValues);
    }

    public DetailReportItem() {
        super();
    }

    @Override
	protected void appendKeyFromNamedValues(String headerValue, String value)
    {
        if (headerValue.equals("name"))
            key = value;
        else if (headerValue.equals("failure"))
            key += "_" + value;
        else if (headerValue.equals("query"))
            key += "_" + value;
    }

    @Override
    protected String getKeyFromNamedValues()
    {
        return namedValues.get("name") + "_" + namedValues.get("failure") + "_" + namedValues.get("query");
    }

    @Override
    public String toString()
    {
        return namedValues.get("name") + "\t" + namedValues.get("failure") + "\t" + namedValues.get("query") + "\t" + (namedValues.get("expected")==null?"":namedValues.get("expected")) + "\t" + namedValues.get("actual");
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
        DetailReportItem other = (DetailReportItem) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

}
