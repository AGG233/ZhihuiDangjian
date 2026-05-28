package com.rauio.smartdangjian.server.ai.util;

import java.util.List;
import java.util.regex.Pattern;

public class PromptSanitizer {

    private static final int MAX_LENGTH = 2000;

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("ignore\\s+(all|previous|above|the\\s+above)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("system\\s+(instruction|prompt|command)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("role\\s*play", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bDAN\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("jailbreak", Pattern.CASE_INSENSITIVE),
            Pattern.compile("bypass", Pattern.CASE_INSENSITIVE),
            Pattern.compile("override", Pattern.CASE_INSENSITIVE),
            Pattern.compile("forget\\s+(everything|all|previous)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("new\\s+(instruction|command|prompt)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("you\\s+are\\s+now", Pattern.CASE_INSENSITIVE),
            Pattern.compile("pretend\\s+to\\s+be", Pattern.CASE_INSENSITIVE));

    private static final List<Pattern> DANGEROUS_TAG_PATTERNS = List.of(
            Pattern.compile("<\\s*/?\\s*system\\s*\\u003e", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*/?\\s*instruction\\s*\\u003e", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*/?\\s*script\\s*\\u003e", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*/?\\s*prompt\\s*\\u003e", Pattern.CASE_INSENSITIVE));

    private PromptSanitizer() {}

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        String result = input.trim();
        if (result.length() > MAX_LENGTH) {
            result = result.substring(0, MAX_LENGTH);
        }
        for (Pattern pattern : INJECTION_PATTERNS) {
            result = pattern.matcher(result).replaceAll("[FILTERED]");
        }
        for (Pattern pattern : DANGEROUS_TAG_PATTERNS) {
            result = pattern.matcher(result).replaceAll("");
        }
        return result;
    }

    public static boolean isSafe(String input) {
        if (input == null || input.isBlank()) {
            return true;
        }
        String sanitized = sanitize(input);
        return sanitized.equals(input.trim());
    }
}
