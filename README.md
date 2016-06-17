Query Testing Framework
=====
DESCRIPTION
-----------

Query testing framework is a library which allows one to test a dataset of queries against a search engine.  The focus is on
the handling the tokens specific to different languages (word delimiters, special characters, morphemes, etc...).  Different datasets
are maintained in google spreadsheets, which can be easily populated by the testers.  This library then reads the datasets, runs the 
tests against the search engine and publishes the results.


SETUP
-----------

To set up the google spreadsheets dataset, follow these steps:

1. Create a new google app https://console.developers.google.com
The app name goes into this property:
com.netflix.search.query.testing.googleAppName=CHANGE-ME

2. Create a new google email service account https://console.developers.google.com
The email address goes into this property:
com.netflix.search.query.testing.serviceAccountEmail=CHANGE-ME@appspot.gserviceaccount.com

3. Download p12 public key and specify its location in these properties:
com.netflix.search.query.testing.p12KeyFileName=CHANGE-ME.p12
com.netflix.search.query.testing.googleSheetsKeyDir=data/g_sheets/

4. Create a new google spreadsheet for the data input, add the created above account to it with VIEW access.
Specify the name of your new spreadsheet in this property:
com.netflix.search.query.testing.inputQueriesSheet=query-testing-framework-input

5. Copy this sheet as an example of the data input into your new spreadsheet: 
https://docs.google.com/spreadsheets/d/10c9zEo4cBWL_rJAFEqNbpu1vpcTkAwubOOZZAYpRzGE/edit?usp=sharing

6. Create two more spreadsheets for the results summary and details. Assign your created email to these spreadsheets with EDIT access.
Specify the names in these properties:
com.netflix.search.query.testing.sumReportSheet=query-testing-framework-results-sum
com.netflix.search.query.testing.detailReportSheet=query-testing-framework-results-details

7. The document type explicit field has to be maintained for search filtering.  The field name can be set by this property, it needs to exist in the configuration of the search engine:
com.netflix.search.query.testing.docTypeFieldName=query-testing-type


DATA MAINTENANCE
----------------

The deletion of any data was removed from the module by design.  When the dataset is updated (e.g. new tests are run), the search engine stale dataset removal is the developer responsibility.  


BUILDING
-----------

Query Testing Framework is built via Gradle (http://www.gradle.org). To build from the command line:
    ./gradlew build copyLibs

RUNNING 
-----------

Run:
java -cp .:build/libs/*:lib/* com.netflix.search.query.QueryTests

ARTIFACTS
-----------

Query testing framework binaries are published to Maven Central.


DEFAULT PROPERTIES OVERRIDING:
-----------

Override the default properties by adding this to command line:
-Darchaius.configurationSource.additionalUrls=file:///data/config-LOCAL_PROPERTIES.properties


AUTHOR
-----------

Ivan Provalov (mailto:iprovalov@netflix.com)

LICENSE
-----------

Copyright 2016 Netflix, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
