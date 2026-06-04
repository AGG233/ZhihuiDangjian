package com.rauio.smartdangjian.server.search.support;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.ibatis.builder.MapperBuilderAssistant;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.rauio.smartdangjian.server.chapter.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.course.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.course.pojo.entity.Course;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.user.pojo.entity.UserSimilarity;

/**
 * 搜索模块测试中 MyBatis-Plus 表元信息的集中初始化工具。
 *
 * <p>在单元测试（MockitoExtension）中，{@link TableInfoHelper#initTableInfo} 必须在使用
 * {@code LambdaQueryWrapper} 前被调用，否则 lambda 到列名的解析会失败。
 * 本工具将搜索模块所有测试涉及的表元信息初始化收敛到一处，避免各测试类重复创建
 * {@link MybatisConfiguration} 和 {@link MapperBuilderAssistant}，并确保
 * {@link TableInfoHelper} 的静态缓存只被填充一次。</p>
 *
 * <p>用法：在各测试类的 {@code @BeforeAll} 中调用
 * {@code TestMybatisPlusConfig.ensureInitialized()}。</p>
 */
public final class TestMybatisPlusConfig {

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private TestMybatisPlusConfig() {}

    /** 确保 MyBatis-Plus 表元信息已初始化，仅在首次调用时真正执行初始化。 */
    public static void ensureInitialized() {
        if (INITIALIZED.compareAndSet(false, true)) {
            doInit();
        }
    }

    private static void doInit() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        initEntity(assistant, Course.class);
        initEntity(assistant, CategoryCourse.class);
        initEntity(assistant, Chapter.class);
        initEntity(assistant, UserSimilarity.class);
        initEntity(assistant, UserLearningRecord.class);
        initEntity(assistant, UserChapterProgress.class);
        initEntity(assistant, UserQuizAnswer.class);
        initEntity(assistant, Quiz.class);
    }

    private static void initEntity(MapperBuilderAssistant assistant, Class<?> entityClass) {
        TableInfoHelper.initTableInfo(assistant, entityClass);
    }
}
