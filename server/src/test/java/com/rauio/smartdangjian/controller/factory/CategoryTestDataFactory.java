package com.rauio.smartdangjian.controller.factory;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.server.content.pojo.dto.CategoryDto;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryArticle;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.vo.CategoryVO;

/**
 * Static factory for category test data — produces CategoryDto, CategoryVO,
 * CategoryCourse, CategoryArticle, and JSON helpers.
 * All IDs are deterministic strings so jsonPath assertions are predictable.
 */
public final class CategoryTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CategoryTestDataFactory() {}

    // ── CategoryDto builders ───────────────────────────────────────

    public static CategoryDto createRootCategoryDto(String name) {
        return CategoryDto.builder()
                .name(name)
                .description("根分类描述")
                .sortOrder(0)
                .build();
    }

    public static CategoryDto createCategoryDto(String name) {
        return CategoryDto.builder()
                .name(name)
                .description("子分类描述")
                .sortOrder(1)
                .build();
    }

    public static CategoryDto createCategoryDto(String name, String description, Integer sortOrder) {
        return CategoryDto.builder()
                .name(name)
                .description(description)
                .sortOrder(sortOrder)
                .build();
    }

    public static CategoryDto createCategoryDtoWithChildren(String name, List<CategoryDto> children) {
        return CategoryDto.builder()
                .name(name)
                .description("带子节点的分类")
                .sortOrder(0)
                .childrenNode(children)
                .build();
    }

    public static List<CategoryDto> createCategoryDtoList(int count) {
        List<CategoryDto> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(createCategoryDto("子分类" + i));
        }
        return list;
    }

    public static List<CategoryDto> createSingleChildCategoryDtoList(String name) {
        return List.of(createCategoryDto(name));
    }

    // ── CategoryVO builders ────────────────────────────────────────

    public static CategoryVO createCategoryVO(String id, String name, String parentId) {
        CategoryVO vo = new CategoryVO();
        vo.setId(id);
        vo.setName(name);
        vo.setDescription("描述-" + name);
        vo.setParentId(parentId);
        vo.setSortOrder(0);
        vo.setUniversityId("uni1");
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    public static CategoryVO createCategoryVO(String id, String name, String parentId, List<CategoryVO> children) {
        CategoryVO vo = createCategoryVO(id, name, parentId);
        vo.setChildren(children);
        return vo;
    }

    public static List<CategoryVO> createCategoryVOList(int count) {
        List<CategoryVO> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(createCategoryVO("cat-" + String.format("%03d", i), "分类" + i, null));
        }
        return list;
    }

    public static List<CategoryVO> createCategoryVOList(int count, String parentId) {
        List<CategoryVO> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(createCategoryVO("child-" + String.format("%03d", i), "子分类" + i, parentId));
        }
        return list;
    }

    public static CategoryVO createCategoryWithChildren(String id, String name, int childCount) {
        CategoryVO vo = createCategoryVO(id, name, null);
        vo.setChildren(createCategoryVOList(childCount, id));
        return vo;
    }

    // ── CategoryCourse builders ────────────────────────────────────

    public static CategoryCourse createCategoryCourse(String categoryId, String courseId) {
        return CategoryCourse.builder()
                .categoryId(categoryId)
                .courseId(courseId)
                .build();
    }

    public static List<CategoryCourse> createCategoryCourseList(String categoryId, int count) {
        List<CategoryCourse> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(createCategoryCourse(categoryId, "course-" + String.format("%03d", i)));
        }
        return list;
    }

    // ── CategoryArticle builders ───────────────────────────────────

    public static CategoryArticle createCategoryArticle(String categoryId, String articleId) {
        CategoryArticle ca = new CategoryArticle();
        ca.setCategoryId(categoryId);
        ca.setArticleId(articleId);
        return ca;
    }

    public static List<CategoryArticle> createCategoryArticleList(String categoryId, int count) {
        List<CategoryArticle> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(createCategoryArticle(categoryId, "article-" + String.format("%03d", i)));
        }
        return list;
    }

    // ── JSON helpers ───────────────────────────────────────────────

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }

    public static String listToJson(List<?> list) {
        try {
            return OBJECT_MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize list to JSON", e);
        }
    }
}
