package com.rauio.smartdangjian.server.graph.constants;

/**
 * 知识图谱领域共享常量：Neo4j 节点标签与关系类型。
 *
 * <p>集中管理图谱中使用的标签/边类型字面量，供 {@code KnowledgeGraphService}、
 * {@code GraphEvaluationTool} 等使用，避免重复硬编码与漂移。
 */
public final class GraphConstants {

    private GraphConstants() {}

    /** 用户节点标签 */
    public static final String LABEL_USER = "User";

    /** 课程节点标签 */
    public static final String LABEL_COURSE = "Course";

    /** 章节节点标签 */
    public static final String LABEL_CHAPTER = "Chapter";

    /** 用户学习课程关系类型 */
    public static final String EDGE_LEARNED = "LEARNED";

    /** 课程包含章节关系类型 */
    public static final String EDGE_HAS_CHAPTER = "HAS_CHAPTER";

    /** 用户学习章节关系类型 */
    public static final String EDGE_LEARNED_CHAPTER = "LEARNED_CHAPTER";
}
