package com.netflix.search.query.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StringUtilsTest {
	@Test
	public void testSummary(){
		String id = StringUtils.createIdUsingTestName("test", "Some Name Containing Spaces");
		Assert.assertEquals(id, "test_Some_Name_Containing_Spaces");
	}
}
