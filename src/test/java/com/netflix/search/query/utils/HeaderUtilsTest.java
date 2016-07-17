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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.netflix.search.query.report.ReportType;

public class HeaderUtilsTest {

	@Test
	public void testSummary(){
		String[] header = HeaderUtils.getHeader(ReportType.summary);
		Assert.assertEquals(header, new String[]{"name", "titles", "queries", "supersetResultsFailed", "differentResultsFailed", "noResultsFailed", "successQ", "precision", "recall", "fmeasure", "comments"});
	}

	@Test
	public void testDetail(){
		String[] header = HeaderUtils.getHeader(ReportType.details);
		Assert.assertEquals(header, new String[]{"name", "failure", "query", "expected", "actual", "comments"});
	}
}
