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

import com.google.common.collect.Maps;
import com.netflix.search.query.Properties;

import java.io.*;
import java.util.*;

public class TitleIdUtils {

    public static final String ENCODING = "UTF-8";
    private static final int BUFFER_SIZE = 1 << 16; // 64K

    public Map<String, String> getTitleToIds(String inputFileName, String testName) throws IOException {
        Map<String, String> titleIdToName = Maps.newHashMap();

        InputStream is = new BufferedInputStream(new FileInputStream(inputFileName), BUFFER_SIZE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, ENCODING), BUFFER_SIZE);
        String lineString = null;
        while ((lineString = reader.readLine()) != null) {
            String[] line = lineString.split(Properties.inputDelimiter.get());
            String id = line[0];
            titleIdToName.put(StringUtils.createIdUsingTestName(id, testName), line[2]);
        }
        reader.close();
        is.close();
        return titleIdToName;
    }
}