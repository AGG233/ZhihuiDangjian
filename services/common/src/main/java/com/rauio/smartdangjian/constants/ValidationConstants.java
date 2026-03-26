package com.rauio.smartdangjian.constants;

import java.util.regex.Pattern;

public final class ValidationConstants {

    private ValidationConstants() {
    }

    public static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public static final Pattern PHONE_PATTERN =
            Pattern.compile("^1[3-9]\\d{9}$");

    public static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-zA-Z0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$");
}
