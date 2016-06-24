package com.netflix.search.query.utils;

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DateUtilsTest {

	@Test
	public void dateTest()
	{
		DateUtil dateUtil = new DateUtil();
		Date dateFromString = dateUtil.getDateFromString("2016Jun23_07:56:47");
		Assert.assertEquals(dateFromString, new Date("Thu Jun 23 07:56:47 PDT 2016"));
	}
}
