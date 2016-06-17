package com.netflix.search.query.utils;

public class StringUtils {

	public static String createIdUsingTestName(String id, String testName)
	{
		return (id + "_" + testName).replaceAll("\\.|\\ ", "_");
	}
}
