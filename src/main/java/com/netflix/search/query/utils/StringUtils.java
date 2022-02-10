/**
 * Copyright 2016 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.search.query.utils;

import com.google.common.collect.Lists;
import com.netflix.search.query.Properties;

import java.util.List;
import java.util.Map;

public class StringUtils {
    public static final String MAP_VALUE_DELIMITER = "\\|";
    public static final String SHEET_TAB_NAME_DELIMITER = "-";

    public static String createIdUsingTestName(String id, String testName) {
        return (id + "_" + testName).replaceAll("\\.|\\ ", "_");
    }

    public static List<String> getLanguageForTest(String testName) {
        List<String> languages = Lists.newArrayList();
        Map<String, String> languageExpansionBasedOnTestNames = Properties.languageExpansionBasedOnTestNames.getMap();
        String languagePartOfTestName = testName.split(SHEET_TAB_NAME_DELIMITER)[0];
        String lanuguagesAsAString = languageExpansionBasedOnTestNames.get(languagePartOfTestName);
        String[] languagesFromMap = lanuguagesAsAString.split(MAP_VALUE_DELIMITER);
        for (String lanuguage : languagesFromMap)
            languages.add(lanuguage);
        return languages;
    }

    public static String getDatasetId(String testId) {
        return testId.substring(0, testId.lastIndexOf('-'));
    }

}
