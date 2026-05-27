package com.rauio.smartdangjian.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.rauio.smartdangjian.common.mapper.UniversitiesMapper;
import com.rauio.smartdangjian.common.pojo.Universities;
import com.rauio.smartdangjian.common.pojo.response.SchoolResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("java:S3011")
class UniversitiesServiceTest {

    @BeforeAll
    static void initMybatisPlus() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        TableInfoHelper.initTableInfo(assistant, Universities.class);
    }

    private UniversitiesMapper universitiesMapper;
    private UniversitiesService universitiesService;

    private static final String SCHOOL_ID = "1";
    private static final String SCHOOL_NAME = "清华大学";

    @BeforeEach
    void setUp() throws Exception {
        universitiesMapper = mock(UniversitiesMapper.class);
        universitiesService = spy(new UniversitiesService());
        var field = com.baomidou.mybatisplus.extension.repository.CrudRepository.class
                .getDeclaredField("baseMapper");
        field.setAccessible(true);
        field.set(universitiesService, universitiesMapper);
    }

    private Universities createUniversity() {
        Universities u = new Universities();
        u.setId(SCHOOL_ID);
        u.setName(SCHOOL_NAME);
        return u;
    }

    @Nested
    @DisplayName("NormalTests — 正常路径")
    class NormalTests {

        @Test
        @DisplayName("getList 返回学校列表")
        void getListSuccess() {
            Universities u1 = createUniversity();
            Universities u2 = new Universities();
            u2.setId("2");
            u2.setName("北京大学");
            doReturn(List.of(u1, u2)).when(universitiesService).list();

            List<SchoolResponse> result = universitiesService.getList();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(SchoolResponse::getId)
                    .containsExactly("1", "2");
            assertThat(result).extracting(SchoolResponse::getName)
                    .containsExactly("清华大学", "北京大学");
        }

        @Test
        @DisplayName("getNameById 返回学校名称")
        void getNameByIdFound() {
            doReturn(createUniversity()).when(universitiesService).getById(SCHOOL_ID);

            String name = universitiesService.getNameById(SCHOOL_ID);

            assertThat(name).isEqualTo(SCHOOL_NAME);
        }

        @Test
        @DisplayName("getIdByName 返回学校 ID")
        void getIdByNameFound() {
            var queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Universities>();
            doReturn(createUniversity()).when(universitiesService).getOne(any());

            String id = universitiesService.getIdByName(SCHOOL_NAME);

            assertThat(id).isEqualTo(SCHOOL_ID);
        }

        @Test
        @DisplayName("getIdByName 学校不存在返回 null")
        void getIdByNameNotFound() {
            doReturn(null).when(universitiesService).getOne(any());

            String id = universitiesService.getIdByName("不存在的学校");

            assertThat(id).isNull();
        }
    }

    @Nested
    @DisplayName("ErrorTests — 异常路径")
    class ErrorTests {

        @Test
        @DisplayName("getNameById 学校不存在返回 null")
        void getNameByIdNotFound() {
            doReturn(null).when(universitiesService).getById(SCHOOL_ID);

            String name = universitiesService.getNameById(SCHOOL_ID);

            assertThat(name).isNull();
        }
    }

    @Nested
    @DisplayName("BoundaryTests — 边界情况")
    class BoundaryTests {

        @Test
        @DisplayName("getList 返回空列表")
        void getListEmpty() {
            doReturn(List.of()).when(universitiesService).list();

            List<SchoolResponse> result = universitiesService.getList();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getList 当 list() 返回 null 时返回空列表")
        void getListNullFromParent() {
            doReturn(null).when(universitiesService).list();

            List<SchoolResponse> result = universitiesService.getList();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getList 只包含一个学校")
        void getListSingleItem() {
            doReturn(List.of(createUniversity())).when(universitiesService).list();

            List<SchoolResponse> result = universitiesService.getList();

            assertThat(result).hasSize(1);
            assertThat(result).singleElement()
                    .extracting(SchoolResponse::getId, SchoolResponse::getName)
                    .containsExactly(SCHOOL_ID, SCHOOL_NAME);
        }
    }
}
