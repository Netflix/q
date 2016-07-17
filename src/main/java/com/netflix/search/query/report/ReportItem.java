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
package com.netflix.search.query.report;

import java.util.Map;

import com.google.api.client.util.Maps;
import com.google.common.base.Joiner;
import com.netflix.search.query.Properties;

public abstract class ReportItem {
    private Map<String, String>  namedValues= Maps.newLinkedHashMap();
    private String key = null;

    public Map<String, String> getNamedValues()
    {
        return namedValues;
    }

    public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public ReportItem(Map<String, String> namedValues) {
        this.namedValues = namedValues;
        this.key = getKeyFromNamedValues();
    }

    public ReportItem() {
        super();
    }

    protected abstract String getKeyFromNamedValues();
    protected abstract void appendKeyFromNamedValues(String headerValue, String value);

    public void setValue(String headerValue, String value)
    {
        if (value != null && !value.isEmpty()) {
            namedValues.put(headerValue, value);
            appendKeyFromNamedValues(headerValue, value);
        }
    }
    
    @Override
    public String toString()
    {
        Joiner joiner = Joiner.on(Properties.inputDelimiter.get());
        return joiner.join(namedValues.values());
    }
    
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

}
