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
