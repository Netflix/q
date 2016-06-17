package com.netflix.search.query.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.netflix.search.query.Properties;

public class DateUtil {
    private final DateFormat DATE_FORMAT = new SimpleDateFormat(Properties.dateFormat.get(), Locale.ENGLISH);

    public String getStringFromDate(Date date)
    {
        return DATE_FORMAT.format(date);
    }

    public Date getDateFromCurrentTime()
    {
        return new Date(System.currentTimeMillis());
    }

    public Date getDateFromString(String dateString)
    {
        try {
            return DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
