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
package com.netflix.search.query;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicStringListProperty;
import com.netflix.config.DynamicStringMapProperty;
import com.netflix.config.DynamicStringProperty;
import com.netflix.config.DynamicStringSetProperty;

public class Properties {
	
    public static final DynamicStringProperty engineHost = new DynamicStringProperty("search.query.testing.engineHost", "localhost");
    public static final DynamicStringProperty enginePort = new DynamicStringProperty("search.query.testing.enginePort", "8983");
    public static final DynamicStringProperty engineServlet = new DynamicStringProperty("search.query.testing.engineServlet", "solr");
    public static final DynamicStringProperty engineIndexName = new DynamicStringProperty("search.query.testing.engineIndexName", "qtest");
    public static final DynamicStringProperty engineType = new DynamicStringProperty("search.query.testing.engineType", "solr");
    
    public static final DynamicStringProperty esDocType = new DynamicStringProperty("search.query.testing.esDocType", "test_doc");
    
    public static final DynamicStringListProperty validDataSetsId = new DynamicStringListProperty("search.query.testing.validDataSetsId", "swedish-video");
    public static final DynamicStringListProperty queryCategories = new DynamicStringListProperty("search.query.testing.queryCategories", "regular,misspelled");
    public static final DynamicStringSetProperty languagesRequiringAdditionalField = new DynamicStringSetProperty("search.query.testing.languagesRequiringAdditionalField", "");
    public static final DynamicStringSetProperty languagesRequiringTransliterationFromEnglish = new DynamicStringSetProperty("search.query.testing.languagesRequiringTransliterationFromEnglish", "");
    public static final DynamicStringProperty transliterationFieldName = new DynamicStringProperty("search.query.testing.transliterationFieldName", "");

    public static final DynamicStringMapProperty languageExpansionBasedOnTestNames = new DynamicStringMapProperty("search.query.testing.languageExpansionBasedOnTestNames", "swedish=sv");
    
    public static final DynamicBooleanProperty isLocalTest = new DynamicBooleanProperty("search.query.testing.isLocalTest",false);
    
    public static final DynamicBooleanProperty isDevOnlyTest = new DynamicBooleanProperty("search.query.testing.isDevOnlyTest",false);

    public static final DynamicStringProperty dataDir = new DynamicStringProperty("search.query.testing.dataDir", "data/q_tests/");
		
    public static final DynamicBooleanProperty isPrintUrl = new DynamicBooleanProperty("search.query.testing.isPrintUrl",false);

    public static final DynamicStringProperty idField = new DynamicStringProperty("search.query.testing.idField", "id");
    public static final DynamicStringListProperty titleFields = new DynamicStringListProperty("search.query.testing.titleFields", "title");
    public static final DynamicStringListProperty titleAkaFields = new DynamicStringListProperty("search.query.testing.titleAkaFields", "title_aka");

    public static final DynamicStringListProperty requiredNumericFields = new DynamicStringListProperty("search.query.testing.requiredNumericFields", "");
    public static final DynamicStringListProperty requiredStringFields = new DynamicStringListProperty("search.query.testing.requiredStringFields", "");

    public static final DynamicStringProperty docTypeFieldName = new DynamicStringProperty("search.query.testing.docTypeFieldName", "query_testing_type");
    public static final DynamicStringProperty inputDelimiter = new DynamicStringProperty("search.query.testing.inputDelimiter", "\t");
    public static final DynamicStringProperty dateFormat = new DynamicStringProperty("search.query.testing.dateFormat", "yyyyMMMdd_HH:mm:ss");
    
    public static final DynamicStringProperty serviceAccountEmail = new DynamicStringProperty("search.query.testing.serviceAccountEmail", "CHANGE-ME@appspot.gserviceaccount.com");
    public static final DynamicStringProperty googleAppName = new DynamicStringProperty("search.query.testing.googleAppName", "CHANGE-ME");
    public static final DynamicStringProperty p12KeyFileName = new DynamicStringProperty("search.query.testing.p12KeyFileName", "CHANGE-ME.p12");
    public static final DynamicStringProperty googleSheetsKeyDir = new DynamicStringProperty("search.query.testing.googleSheetsKeyDir", "data/g_sheets");
    public static final DynamicIntProperty googleApiThrottlePause = new DynamicIntProperty("search.query.testing.googleApiThrottlePause", 1500);

    public static final DynamicStringProperty inputQueriesSheet = new DynamicStringProperty("search.query.testing.inputQueriesSheet", "query-testing-framework-input");
    public static final DynamicStringProperty sumReportSheet = new DynamicStringProperty("search.query.testing.sumReportSheet", "query-testing-framework-results-sum");
    public static final DynamicStringProperty detailReportSheet = new DynamicStringProperty("search.query.testing.detailReportSheet", "query-testing-framework-results-details");
    public static final DynamicStringProperty sumReportSheetDev = new DynamicStringProperty("search.query.testing.sumReportSheetDev", "query-testing-framework-results-sum-dev");
    public static final DynamicStringProperty detailReportSheetDev = new DynamicStringProperty("search.query.testing.detailReportSheetDev", "query-testing-framework-results-details-dev");
    
}
