package com.rauio.smartdangjian.server.quiz.constants;

/**
 * 题目模块错误码常量（范围 6000-6999）
 */
public class QuizErrorConstants {

    public static final int QUIZ_NOT_FOUND = 6001;
    public static final int QUIZ_OPTION_NOT_FOUND = 6002;
    public static final int CHAPTER_NOT_FOUND = 6003;
    public static final int COURSE_NOT_FOUND = 6004;

    /** SCORM 学习包格式非法（扩展名非 .zip） */
    public static final int SCORM_PACKAGE_INVALID = 6005;
    /** SCORM 学习包解析失败 */
    public static final int SCORM_PARSE_FAILED = 6006;
    /** SCORM 学习包不存在 */
    public static final int SCORM_PACKAGE_NOT_FOUND = 6007;
    /** SCORM 学习包保存失败 */
    public static final int SCORM_PACKAGE_SAVE_FAILED = 6008;
    /** SCORM 成绩上报保存失败 */
    public static final int SCORM_REGISTRATION_SAVE_FAILED = 6009;
}
