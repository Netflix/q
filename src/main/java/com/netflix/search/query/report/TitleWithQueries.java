package com.netflix.search.query.report;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netflix.search.query.Properties;

public class TitleWithQueries {

    private static final String SHEET_NAME_DELIMITER = "-";
	private static final Joiner JOINER_QUERIES = Joiner.on("~~~");
    private static final Joiner JOINER_CATEGORIES = Joiner.on("=");

    private String id;
    private String titleEn;
    private String titleLocale;
    private String titleAlt;
    private final String language;
    private final String entityType;
    private String sheetId;

    private Map<String, Set<String>> queriesByCategory = Maps.newLinkedHashMap();

    public TitleWithQueries(String sheetId) {
        String[] id = sheetId.split(SHEET_NAME_DELIMITER);
        this.language = id[0];
        this.entityType = id[1];
        this.sheetId = sheetId;
    }

    public String getId()
    {
		return (id + "_" + sheetId).replaceAll("\\.|\\ ", "_");
    }

    public String getTitleEn()
    {
        return titleEn;
    }

    public String getTitleLocale()
    {
        return titleLocale;
    }

    public String getTitleAlt()
    {
        return titleAlt;
    }

    public String getLanguage()
    {
        return language;
    }

    public String getEntityType()
    {
        return entityType;
    }

    public Map<String, Set<String>> getQueriesByCategory()
    {
        return queriesByCategory;
    }

    public void setValue(String headerValue, String value)
    {
        if(headerValue==null){
            System.err.println("Header is missing for this value: " + value);
            return;
        }
        if (value != null && !value.isEmpty()) {
            if (headerValue.equalsIgnoreCase("id"))
                this.id = value;

            else if (headerValue.equalsIgnoreCase("title_en"))
                this.titleEn = value;

            else if (headerValue.equalsIgnoreCase("title_locale"))
                this.titleLocale = value;

            else if (headerValue.equalsIgnoreCase("title_alt"))
                this.titleAlt = value;

            else if (headerValue.startsWith("q_")) {
                String cleanedHeader = headerValue.substring(2);
                Set<String> queriesForThisCategory = queriesByCategory.get(cleanedHeader);
                if (queriesForThisCategory == null)
                    queriesForThisCategory = Sets.newLinkedHashSet();
                queriesForThisCategory.add(value);
                queriesByCategory.put(cleanedHeader, queriesForThisCategory);
            }
        }
    }

    @Override
    public String toString()
    {
        List<String> mapToList = mapToList(queriesByCategory);
        return getId() + Properties.inputDelimiter.get() + titleEn + Properties.inputDelimiter.get() + titleLocale + Properties.inputDelimiter.get() + (titleAlt==null?"":titleAlt) + Properties.inputDelimiter.get()+"q=" + mapToList;
    }

    private List<String> mapToList(final Map<String, Set<String>> input)
    {
        return Lists.newArrayList(Iterables.transform(input.entrySet(), new Function<Map.Entry<String, Set<String>>, String>() {
            public String apply(final Map.Entry<String, Set<String>> input)
            {
                return JOINER_CATEGORIES.join(input.getKey(), JOINER_QUERIES.join(input.getValue()));
            }
        }));
    }

}
