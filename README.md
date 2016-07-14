Query Testing Framework
=====
DESCRIPTION
-----------

Query testing framework is a library which allows one to test a dataset of queries against a search engine.  The focus is on
the handling the tokens specific to different languages (word delimiters, special characters, morphemes, etc...).  Different datasets
are maintained in google spreadsheets, which can be easily populated by the testers.  This library then reads the datasets, runs the 
tests against the search engine and publishes the results.  For more details, see the 

http://techblog.netflix.com/2016/07/global-languages-support-at-netflix.html
https://github.com/Netflix/q/wiki

SETUP
-----------

To set up the google spreadsheets dataset, as well as the instructions for Solr and Elasticsearch setup/configurations see this document:

https://github.com/Netflix/q/wiki/Setting-up-Google-Spreadsheets
https://github.com/Netflix/q/wiki/Setting-up-for-Solr
https://github.com/Netflix/q/wiki/Setting-up-for-Elasticsearch

DATA MAINTENANCE
----------------

The deletion of any data was removed from the module by design.  When the dataset is updated (e.g. new tests are run), the search engine stale dataset removal is the developer responsibility.  


BUILDING
-----------
	
https://github.com/Netflix/q/wiki/Building

RUNNING 
-----------

https://github.com/Netflix/q/wiki/Running

ARTIFACTS
-----------

Query testing framework binaries are published to Maven Central.

`compile 'com.netflix.search:q:latest.release'`


DEFAULT PROPERTIES OVERRIDING:
-----------

Override the default properties by adding this to command line:
`-Darchaius.configurationSource.additionalUrls=file:///data/config-LOCAL_PROPERTIES.properties`


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
