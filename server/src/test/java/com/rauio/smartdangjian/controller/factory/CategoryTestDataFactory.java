package com.rauio.smartdangjian.controller.factory;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.server.content.pojo.request.CategoryRequest;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryArticle;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.response.CategoryResponse;

/**
 * Static factory for category test data — produces CategoryRequest, CategoryResponse,
 * CategoryCourse, CategoryArticle, and JSON helpers.
 * All IDs are deterministic strings so jsonPath assertions are predictable.
 */
public final class CategoryTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CategoryTestDataFactory() {}

    // ── CategoryRequest builders ───────────────────────────────────────

    public static CategoryRequest createRootCategoryRequest(String name) {
        return CategoryRequest.builder()
                .name(name)
                .description("根分类描述")
                .sortOrder(0)
                .build();
    }

    public static CategoryRequest createCategoryRequest(String name) {
        return CategoryRequest.builder()
                .name(name)
                .description("子分类描述")
                .sortOrder(1)
                .build();
    }

    public static CategoryRequest createCategoryRequest(String name, String description, Integer sortOrder) {
        return CategoryRequest.builder()
                .name(name)
                .description(description)
                .sortOrder(sortOrder)
                .build();
    }

    public static CategoryRequest createCategoryRequestWithChildren(String name, List<CategoryRequest> children) {
        return CategoryRequest.builder()
                .name(name)
                .description("带子节点的分类")
                .sortOrder(0)
                .childrenNode(children)
                .build();
    }

    public static List<CategoryRequest> createCategoryRequestList(int count) {
        List<CategoryRequest> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(createCategoryRequest("子分类" + i));
        }
        return list;
    }

    public static List<CategoryRequest> createSingleChildCategoryRequestList(String name) {
        return List.of(createCategoryRequest(name));
    }

    // ── CategoryResponse builders ────────────────────────────────────────

    public static CategoryResponse createCategoryResponse(String id, String name, String parentId) {
        CategoryResponse vo = new CategoryResponse();
        vo.setId(id);
        vo.setName(name);
        vo.setDescription("描述-" + name);
        vo.setParentId(parentId);
        vo.setSortOrder(0);
        vo.setUniversityId("uni1");
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    public static CategoryResponse createCategoryResponse(String id, String name, String parentId, List<CategoryResponse> children) {
        CategoryResponse vo = createCategoryResponse(id, name, parentId);
        vo.setChildren(children);
        return vo;
    }

    public static List<CategoryResponse> createCategoryResponseList(int count) {
        List<CategoryResponse> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(createCategoryResponse("cat-" + String.format("%03d", i), "分类" + i, null));
        }
        return list;
    }

    public static List<CategoryResponse> createCategoryResponseList(int count, String parentId) {
        List<CategoryResponse> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(createCategoryResponse("child-" + String.format("%03d", i), "子分类" + i, parentId));
        }
        return list;
    }

    public static CategoryResponse createCategoryWithChildren(String id, String name, int childCount) {
        CategoryResponse vo = createCategoryResponse(id, name, null);
        vo.setChildren(createCategoryResponseList(childCount, id));
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
