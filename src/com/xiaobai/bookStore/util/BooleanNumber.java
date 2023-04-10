package com.xiaobai.bookStore.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 判断传入的值是不是纯数字
 */
public class BooleanNumber {

    public static boolean isNumeric(String str)
    {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        } else {
            return true;
        }
    }
}
