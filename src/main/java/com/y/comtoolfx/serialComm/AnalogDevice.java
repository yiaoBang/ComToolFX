package com.y.comtoolfx.serialComm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Y
 * @version 1.0
 * @date 2024/3/15 11:41
 */

public class AnalogDevice {
    public static final Gson JSON = new Gson();
    private static final Type type = new TypeToken<Map<String, String>>() {
    }.getType();

    public static Map<String, byte[]> config(File file) {
        try {
            List<String> strings = Files.readAllLines(file.toPath());
            StringBuilder stringBuilder = new StringBuilder();
            strings.forEach(stringBuilder::append);
            String string = stringBuilder.toString();
            //log.debug("文件内容 {}", string);
            String s = StringEscapeUtils.unescapeJava(string);
            //log.debug("文件内容解析 {}", s);
            Map<String, String> maps = JSON.fromJson(s, type);

            //maps.forEach((k, v) -> log.debug("解析后键值对{}={}", k, v));
            Map<String, byte[]> replay = new HashMap<>();
            maps.forEach((k, v) -> replay.put(k, v.getBytes(StandardCharsets.UTF_8)));
            //replay.forEach((k, v) -> log.debug("转换后键值对{}{}", k, Arrays.toString(v)));
            return replay;
        } catch (IOException e) {
            return null;
        }
    }
}
