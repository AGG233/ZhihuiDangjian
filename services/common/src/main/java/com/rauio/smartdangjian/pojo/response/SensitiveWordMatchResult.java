package com.rauio.smartdangjian.pojo.response;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWordMatchResult {

    @Builder.Default
    private boolean matched = false;

    @Builder.Default
    private List<String> words = Collections.emptyList();

    private String sanitizedText;

    private int originalLength;
}
