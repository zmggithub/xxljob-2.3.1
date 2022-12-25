package com.xxl.job.executor.test;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: zmg
 */
public class SimpleTest {

    @Test
    public void testSimpleDateFormat() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = simpleDateFormat.parse("2022-12-24");
        System.out.println(parse);
    }

}
