package com.netflix.search.query.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DateUtilsTest {

	@Test
	public void dateTest() throws ParseException
	{
		DateUtil dateUtil = new DateUtil();
		Date dateFromString = dateUtil.getDateFromString("2016Jun23_07:56:47");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss zzz");
		Date expected = sdf.parse("23/06/2016 07:56:47 PDT");
		Assert.assertEquals(dateFromString, expected);
	}
}
