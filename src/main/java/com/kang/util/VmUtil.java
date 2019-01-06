package com.kang.util;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author kang
 */
public class VmUtil {

    public static int size(Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }

    public static boolean empty(Collection<?> collection) {
        return size(collection) == 0;
    }

    public static <T> T get(List<T> list, int index) {
        return size(list) > index ? list.get(index) : null;
    }

    public static String toString(Object object) {
        return object == null ? null : object.toString();
    }

    public static Date parseDate(String date, String pattern) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        try {
            return new SimpleDateFormat(pattern).parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatDate(Date date, String pattern) {
        if (Objects.isNull(date)) {
            return null;
        }
        return new SimpleDateFormat(pattern).format(date);
    }

}
