package com.rauio.smartdangjian.server.ai.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.constants.RedisConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.constants.AiErrorConstants;
import com.rauio.smartdangjian.server.ai.mapper.AiFaqMapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiFaq;
import com.rauio.smartdangjian.server.ai.pojo.request.FaqCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.FaqUpdateRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiFaqResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FaqService extends ServiceImpl<AiFaqMapper, AiFaq> {

    /**
     * 匹配用户输入：规范化 → 遍历启用FAQ → 子串匹配关键词 → 按sort取最佳
     *
     * @param input 用户输入文本（已通过安全过滤）
     * @return 匹配到的FAQ，无匹配返回Optional.empty()
     */
    @Transactional(transactionManager = "dataSourceTransactionManager", readOnly = true)
    public Optional<AiFaq> match(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        String normalized = normalize(input);
        if (normalized.length() < 2) {
            return Optional.empty();
        }
        return getAllEnabledFaqs().stream()
                .filter(faq -> matchesKeywords(faq.getKeywords(), normalized))
                .min(Comparator.comparingInt(AiFaq::getSort));
    }

    private String normalize(String input) {
        return input.trim().toLowerCase().replaceAll("[\\p{Punct}\\p{Space}]+", "");
    }

    private boolean matchesKeywords(String keywords, String normalized) {
        return Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(k -> k.length() >= 2)
                .anyMatch(k -> normalized.contains(normalize(k)));
    }

    @Transactional(transactionManager = "dataSourceTransactionManager", readOnly = true)
    @Cacheable(value = RedisConstants.AI_FAQ_CACHE_PREFIX, unless = "#result.isEmpty()")
    public List<AiFaq> getAllEnabledFaqs() {
        return lambdaQuery()
                .eq(AiFaq::getEnabled, true)
                .orderByAsc(AiFaq::getSort)
                .list();
    }

    @Transactional(transactionManager = "dataSourceTransactionManager", rollbackFor = Exception.class)
    @CacheEvict(value = RedisConstants.AI_FAQ_CACHE_PREFIX, key = "#result.id")
    public AiFaqResponse createFaq(FaqCreateRequest request) {
        AiFaq faq = AiFaq.builder()
                .keywords(request.getKeywords())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .enabled(Boolean.TRUE.equals(request.getEnabled()))
                .sort(request.getSort() == null ? 0 : request.getSort())
                .build();
        this.save(faq);
        log.info("FAQ创建成功 id={}", faq.getId());
        return toResponse(faq);
    }

    @Transactional(transactionManager = "dataSourceTransactionManager", rollbackFor = Exception.class)
    @CacheEvict(value = RedisConstants.AI_FAQ_CACHE_PREFIX, key = "#request.id")
    public AiFaqResponse updateFaq(FaqUpdateRequest request) {
        AiFaq faq = this.getById(request.getId());
        if (faq == null) {
            throw new BusinessException(AiErrorConstants.FAQ_NOT_FOUND, "FAQ不存在: " + request.getId());
        }
        if (request.getKeywords() != null) {
            faq.setKeywords(request.getKeywords());
        }
        if (request.getQuestion() != null) {
            faq.setQuestion(request.getQuestion());
        }
        if (request.getAnswer() != null) {
            faq.setAnswer(request.getAnswer());
        }
        if (request.getEnabled() != null) {
            faq.setEnabled(request.getEnabled());
        }
        if (request.getSort() != null) {
            faq.setSort(request.getSort());
        }
        this.updateById(faq);
        log.info("FAQ更新成功 id={}", faq.getId());
        return toResponse(faq);
    }

    @Transactional(transactionManager = "dataSourceTransactionManager", rollbackFor = Exception.class)
    @CacheEvict(value = RedisConstants.AI_FAQ_CACHE_PREFIX, key = "#id")
    public void deleteFaq(Long id) {
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new BusinessException(AiErrorConstants.FAQ_NOT_FOUND, "FAQ不存在: " + id);
        }
        log.info("FAQ删除成功 id={}", id);
    }

    @Transactional(transactionManager = "dataSourceTransactionManager", readOnly = true)
    public AiFaqResponse getFaqResponse(Long id) {
        AiFaq faq = this.getById(id);
        if (faq == null) {
            throw new BusinessException(AiErrorConstants.FAQ_NOT_FOUND, "FAQ不存在: " + id);
        }
        return toResponse(faq);
    }

    @Transactional(transactionManager = "dataSourceTransactionManager", readOnly = true)
    public IPage<AiFaqResponse> pageFaqs(int pageNum, int pageSize) {
        Page<AiFaq> page = new Page<>(pageNum, pageSize);
        IPage<AiFaq> result = this.page(page, new LambdaQueryWrapper<AiFaq>().orderByAsc(AiFaq::getSort));
        return result.convert(FaqService::toResponse);
    }

    private static AiFaqResponse toResponse(AiFaq faq) {
        return AiFaqResponse.builder()
                .id(faq.getId())
                .keywords(faq.getKeywords())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .enabled(faq.getEnabled())
                .sort(faq.getSort())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}
