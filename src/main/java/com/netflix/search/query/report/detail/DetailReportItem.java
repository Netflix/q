package com.netflix.search.query.report.detail;

import java.util.Map;

import com.netflix.search.query.report.ReportItem;
import com.netflix.search.query.report.ResultType;

public class DetailReportItem extends ReportItem {

    public DetailReportItem(String name, ResultType failure, String query, String expected, String actual) {
        super();
        setValue(DetailReportHeader.name.toString(), name);
        setValue(DetailReportHeader.failure.toString(), failure.toString());
        setValue(DetailReportHeader.query.toString(), query);
        setValue(DetailReportHeader.expected.toString(), expected);
        setValue(DetailReportHeader.actual.toString(), actual);
        setKey(name + "_" + failure.toString() + "_" + query);
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
        if (headerValue.equals(DetailReportHeader.name.toString()))
        	setKey(value);
        else if (headerValue.equals(DetailReportHeader.failure.toString()) || headerValue.equals(DetailReportHeader.query.toString()))
        	setKey(getKey() + "_" + value);
    }

    @Override
    protected String getKeyFromNamedValues()
    {
        return getNamedValues().get(DetailReportHeader.name.toString()) + "_" + getNamedValues().get(DetailReportHeader.failure.toString()) + "_" + getNamedValues().get(DetailReportHeader.query.toString());
    }

    @Override
    public String toString()
    {
        return getNamedValues().get(DetailReportHeader.name.toString()) + "\t" + getNamedValues().get(DetailReportHeader.failure.toString()) + "\t" + getNamedValues().get(DetailReportHeader.query.toString()) + "\t" + (getNamedValues().get(DetailReportHeader.expected.toString())==null?"":getNamedValues().get(DetailReportHeader.expected.toString())) + "\t" + getNamedValues().get(DetailReportHeader.actual.toString());
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
        if (getKey() == null) {
            if (other.getKey() != null)
                return false;
        } else if (!getKey().equals(other.getKey()))
            return false;
        return true;
    }

}
