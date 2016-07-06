package com.netflix.search.query.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.search.query.Properties;

public class DateUtil {
    public static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

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
        } catch (Throwable e) {
            logger.error("Error trying to create Date from String", e);
        }
        return null;
    }

}
