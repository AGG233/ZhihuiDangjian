package com.rauio.smartdangjian.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import com.rauio.smartdangjian.config.SensitiveWordProperties;
import com.rauio.smartdangjian.pojo.response.SensitiveWordMatchResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SensitiveWordService implements InitializingBean {

    private final SensitiveWordProperties properties;

    private volatile SensitiveWordBs sensitiveWordBs;

    private static final List<String> HARDCODED_WHITELIST = List.of(
            "毛泽东",
            "周恩来",
            "邓小平",
            "江泽民",
            "胡锦涛",
            "习近平",
            "共产党",
            "中国共产党",
            "社会主义",
            "无产阶级",
            "马列主义",
            "革命",
            "解放",
            "红色",
            "工农",
            "党支部",
            "党委书记",
            "组织生活",
            "民主集中制",
            "群众路线",
            "三个代表",
            "科学发展观",
            "新时代中国特色社会主义",
            "南昌起义",
            "秋收起义",
            "广州起义",
            "长征",
            "遵义会议",
            "抗日战争",
            "解放战争",
            "改革开放",
            "一国两制",
            "为人民服务",
            "不忘初心",
            "牢记使命");

    @Override
    public void afterPropertiesSet() {
        List<String> whitelist = new ArrayList<>(HARDCODED_WHITELIST);
        if (properties.isEnableWhitelist()) {
            whitelist.addAll(loadFileWhitelist());
        }

        List<String> customDeny = properties.getCustomDenyWords();

        this.sensitiveWordBs = SensitiveWordBs.newInstance()
                .wordDeny(WordDenys.chains(WordDenys.defaults(), () -> customDeny))
                .wordAllow(WordAllows.chains(WordAllows.defaults(), () -> whitelist))
                .ignoreCase(true)
                .ignoreWidth(true)
                .ignoreNumStyle(true)
                .ignoreChineseStyle(true)
                .ignoreEnglishStyle(true)
                .ignoreRepeat(true)
                .init();

        log.info("敏感词过滤器初始化完成，白名单 {} 条，自定义拒绝词 {} 条", whitelist.size(), customDeny.size());
    }

    public SensitiveWordMatchResult check(String text) {
        if (text == null || text.isBlank()) {
            return emptyResult(0);
        }
        String processed = preprocess(text);
        List<String> words = sensitiveWordBs.findAll(processed);
        if (words.isEmpty()) {
            return SensitiveWordMatchResult.builder()
                    .matched(false)
                    .originalLength(text.length())
                    .sanitizedText(text)
                    .build();
        }
        return SensitiveWordMatchResult.builder()
                .matched(true)
                .words(words)
                .originalLength(text.length())
                .sanitizedText(sensitiveWordBs.replace(processed))
                .build();
    }

    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        return sensitiveWordBs.contains(preprocess(text));
    }

    public SensitiveWordMatchResult sanitize(String text) {
        if (text == null || text.isBlank()) {
            return emptyResult(0);
        }
        String processed = preprocess(text);
        List<String> words = sensitiveWordBs.findAll(processed);
        return SensitiveWordMatchResult.builder()
                .matched(!words.isEmpty())
                .words(words)
                .originalLength(text.length())
                .sanitizedText(sensitiveWordBs.replace(processed))
                .build();
    }

    public List<String> findWords(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return sensitiveWordBs.findAll(preprocess(text));
    }

    private String preprocess(String text) {
        if (text.length() > properties.getMaxLength()) {
            return text.substring(0, properties.getMaxLength());
        }
        return text;
    }

    private SensitiveWordMatchResult emptyResult(int length) {
        return SensitiveWordMatchResult.builder()
                .matched(false)
                .originalLength(length)
                .build();
    }

    private List<String> loadFileWhitelist() {
        String location = properties.getWhitelistLocation();
        String path = location.startsWith("classpath:") ? location.substring("classpath:".length()) : location;

        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            log.warn("敏感词白名单文件不存在: {}", path);
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                result.add(line);
            }
        } catch (IOException e) {
            log.error("读取敏感词白名单文件失败: {}", path, e);
            return Collections.emptyList();
        }
        return result;
    }
}
