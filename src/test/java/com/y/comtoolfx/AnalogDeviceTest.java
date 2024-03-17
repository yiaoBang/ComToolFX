package com.y.comtoolfx;

import org.apache.commons.text.StringEscapeUtils;

/**
 * @author Y
 * @version 1.0
 * @date 2024/3/15 11:50
 */
class AnalogDeviceTest {


    public static void main(String[] args) {
        // 包含转义字符的字符串
        String stringWithEscapes = "This is a string with \\n new line and \\t tab characters.";

        // 解码字符串中的转义字符
        String originalString = StringEscapeUtils.unescapeJava(stringWithEscapes);

        // 打印还原后的字符串
        System.out.println("Original string: " + originalString);
    }
}