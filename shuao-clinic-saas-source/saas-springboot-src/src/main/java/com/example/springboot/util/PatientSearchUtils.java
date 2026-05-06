package com.example.springboot.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.util.StringUtils;

import java.util.Locale;

public final class PatientSearchUtils {

    private static final HanyuPinyinOutputFormat PINYIN_FORMAT = buildFormat();

    private PatientSearchUtils() {
    }

    public static String normalizeKeyword(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    public static String toInitials(String value) {
        return toPinyinToken(value, true);
    }

    public static String toPinyin(String value) {
        return toPinyinToken(value, false);
    }

    private static String toPinyinToken(String value, boolean initialsOnly) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (char current : value.trim().toCharArray()) {
            if (Character.isWhitespace(current)) {
                continue;
            }
            if (Character.isLetterOrDigit(current) && current < 128) {
                builder.append(Character.toLowerCase(current));
                continue;
            }
            String[] pinyinArray;
            try {
                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(current, PINYIN_FORMAT);
            } catch (BadHanyuPinyinOutputFormatCombination exception) {
                continue;
            }
            if (pinyinArray == null || pinyinArray.length == 0 || !StringUtils.hasText(pinyinArray[0])) {
                continue;
            }
            String token = pinyinArray[0].toLowerCase(Locale.ROOT);
            builder.append(initialsOnly ? token.charAt(0) : token);
        }
        return builder.toString();
    }

    private static HanyuPinyinOutputFormat buildFormat() {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        return format;
    }
}
