package com.seven.ticket.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/8 10:45
 * @Version V1.0
 **/
public class ReUtil {
    public static List<String> findAll(String regex, CharSequence content, int group) {
        return (List)findAll((String)regex, content, group, new ArrayList());
    }

    public static <T extends Collection<String>> T findAll(String regex, CharSequence content, int group, T collection) {
        return null == regex ? collection : findAll(Pattern.compile(regex, 32), content, group, collection);
    }

    public static <T extends Collection<String>> T findAll(Pattern pattern, CharSequence content, int group, T collection) {
        if (null != pattern && null != content) {
            if (null == collection) {
                throw new NullPointerException("Null collection param provided!");
            } else {
                Matcher matcher = pattern.matcher(content);

                while(matcher.find()) {
                    collection.add(matcher.group(group));
                }

                return collection;
            }
        } else {
            return null;
        }
    }
}
