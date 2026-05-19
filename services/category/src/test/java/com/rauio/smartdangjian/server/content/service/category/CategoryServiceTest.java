package com.rauio.smartdangjian.server.content.service.category;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.content.constants.CategoryErrorConstants;
import com.rauio.smartdangjian.server.content.mapper.CategoryMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.CategoryConvertor;
import com.rauio.smartdangjian.server.content.pojo.dto.CategoryDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Category;
import com.rauio.smartdangjian.server.content.pojo.vo.CategoryVO;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper mapper;

    @Mock
    private CategoryConvertor convertor;

    @Spy
    @InjectMocks
    private CategoryService categoryService;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    // ==================== get ====================

    @Test
    @DisplayName("get 查询不存在的目录抛出 BusinessException 3001")
    void getWhenCategoryNotFoundThrowsBusinessException() {
        doReturn(null).when(categoryService).getById("nonexistent");

        assertThatThrownBy(() -> categoryService.get("nonexistent"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_NOT_FOUND);
                    assertThat(be.getMessage()).isEqualTo("目录不存在");
                });
    }

    @Test
    @DisplayName("get 查询存在的目录且无子节点时返回 VO")
    void getWhenCategoryExistsAndNoChildrenReturnsVO() {
        Category category = createCategory("1", "根目录", 0, null);
        CategoryVO vo = createCategoryVO("1", "根目录", null, null);

        doReturn(category).when(categoryService).getById("1");
        doReturn(vo).when(convertor).toVO(category);

        CategoryVO result = categoryService.get("1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getName()).isEqualTo("根目录");
        verify(convertor).toVO(category);
    }

    @Test
    @DisplayName("get 查询目录存在且有子节点时不发生子节点递归（子节点无更深层子节点）")
    void getWhenCategoryHasChildrenWithoutGrandchildren() {
        Category category = createCategory("1", "根目录", 0, null);
        CategoryVO childVO1 = createCategoryVO("2", "子目录1", "1", Collections.emptyList());
        CategoryVO childVO2 = createCategoryVO("3", "子目录2", "1", Collections.emptyList());
        CategoryVO parentVO = createCategoryVO("1", "根目录", null, List.of(childVO1, childVO2));

        doReturn(category).when(categoryService).getById("1");
        doReturn(parentVO).when(convertor).toVO(category);

        CategoryVO result = categoryService.get("1");

        assertThat(result).isNotNull();
        assertThat(result.getChildren()).hasSize(2);
        // 子节点没有 children，所以不会递归调用 get
        verify(categoryService, times(1)).getById("1");
    }

    @Test
    @DisplayName("get 查询目录存在且有孙子节点时递归调用 get")
    void getWhenCategoryHasNestedGrandchildrenRecursivelyProcesses() {
        Category category = createCategory("1", "根目录", 0, null);
        Category childCategory = createCategory("2", "子目录", 1, "1");

        CategoryVO grandchildVO = createCategoryVO("4", "孙子目录", "2", Collections.emptyList());
        CategoryVO childVO = createCategoryVO("2", "子目录", "1", List.of(grandchildVO));
        CategoryVO leafChildVO = createCategoryVO("3", "叶子子目录", "1", Collections.emptyList());
        CategoryVO parentVO = createCategoryVO("1", "根目录", null, List.of(childVO, leafChildVO));

        CategoryVO retrievedChildVO = createCategoryVO("2", "子目录", "1", List.of(grandchildVO));

        doReturn(category).when(categoryService).getById("1");
        doReturn(parentVO).when(convertor).toVO(category);
        // 递归调用 get("2") 时
        doReturn(childCategory).when(categoryService).getById("2");
        doReturn(retrievedChildVO).when(convertor).toVO(childCategory);

        CategoryVO result = categoryService.get("1");

        assertThat(result).isNotNull();
        verify(categoryService, times(1)).getById("1");
        verify(categoryService, times(1)).getById("2");
    }

    // ==================== getRootList ====================

    @Test
    @DisplayName("getRootList 返回所有顶级目录的 VO 列表")
    void getRootListReturnsVOList() {
        List<Category> rootCategories = List.of(
                createCategory("1", "根目录1", 0, null),
                createCategory("2", "根目录2", 0, null)
        );
        List<CategoryVO> rootVOs = List.of(
                createCategoryVO("1", "根目录1", null, Collections.emptyList()),
                createCategoryVO("2", "根目录2", null, Collections.emptyList())
        );

        doReturn(rootCategories).when(categoryService).list(any(LambdaQueryWrapper.class));
        doReturn(rootVOs).when(convertor).toVOList(rootCategories);

        List<CategoryVO> result = categoryService.getRootList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("根目录1");
        assertThat(result.get(1).getName()).isEqualTo("根目录2");
    }

    // ==================== getByParentId ====================

    @Test
    @DisplayName("getByParentId 根据父目录 ID 返回直接子目录的 VO 列表")
    void getByParentIdReturnsVOList() {
        List<Category> children = List.of(
                createCategory("2", "子目录1", 1, "1"),
                createCategory("3", "子目录2", 1, "1")
        );
        List<CategoryVO> childVOs = List.of(
                createCategoryVO("2", "子目录1", "1", Collections.emptyList()),
                createCategoryVO("3", "子目录2", "1", Collections.emptyList())
        );

        doReturn(children).when(categoryService).list(any(LambdaQueryWrapper.class));
        doReturn(childVOs).when(convertor).toVOList(children);

        List<CategoryVO> result = categoryService.getByParentId("1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getParentId()).isEqualTo("1");
        assertThat(result.get(1).getParentId()).isEqualTo("1");
    }

    // ==================== create ====================

    @Test
    @DisplayName("create dto 为 null 时抛出 BusinessException 3002")
    void createWhenDtoIsNullThrowsBusinessException() {
        assertThatThrownBy(() -> categoryService.create(null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_ARGS_ERROR);
                    assertThat(be.getMessage()).isEqualTo("参数错误");
                });
    }

    @Test
    @DisplayName("create 当前用户为 null 时抛出 BusinessException 3006")
    void createWhenCurrentUserIsNullThrowsBusinessException() {
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(null);
        CategoryDto dto = CategoryDto.builder().name("根目录").childrenNode(Collections.emptyList()).build();
        Category category = createCategory(null, "根目录", 0, null);
        doReturn(category).when(convertor).toEntity(dto);

        assertThatThrownBy(() -> categoryService.create(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_USER_NOT_FOUND);
                    assertThat(be.getMessage()).isEqualTo("当前用户不存在");
                });
    }

    @Test
    @DisplayName("create 当前用户为 MANAGER 时抛出 BusinessException 3002（无法获取学校 ID）")
    void createWhenUserIsManagerThrowsBusinessException() {
        CurrentUserPrincipal managerUser = createMockUser(UserType.MANAGER, null);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(managerUser);
        CategoryDto dto = CategoryDto.builder().name("根目录").childrenNode(Collections.emptyList()).build();
        Category category = createCategory(null, "根目录", 0, null);
        doReturn(category).when(convertor).toEntity(dto);

        assertThatThrownBy(() -> categoryService.create(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_ARGS_ERROR);
                    assertThat(be.getMessage()).isEqualTo("不能获取当前用户所属学校");
                });
    }

    @Test
    @DisplayName("create SCHOOL 用户无子节点时创建根目录成功")
    void createWhenSchoolUserAndNoChildrenCreatesSuccessfully() {
        CurrentUserPrincipal schoolUser = createMockUser(UserType.SCHOOL, "uni123");
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(schoolUser);
        CategoryDto dto = CategoryDto.builder().name("根目录").childrenNode(Collections.emptyList()).build();
        Category category = createCategory(null, "根目录", null, null);
        doReturn(category).when(convertor).toEntity(dto);
        doReturn(true).when(categoryService).save(any(Category.class));

        Boolean result = categoryService.create(dto);

        assertThat(result).isTrue();
        assertThat(category.getLevel()).isEqualTo(0);
        assertThat(category.getParentId()).isNull();
        assertThat(category.getUniversityId()).isEqualTo("uni123");
        verify(categoryService).save(category);
    }

    @Test
    @DisplayName("create STUDENT 用户无子节点时创建根目录成功")
    void createWhenStudentUserAndNoChildrenCreatesSuccessfully() {
        CurrentUserPrincipal studentUser = createMockUser(UserType.STUDENT, "uni456");
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(studentUser);
        CategoryDto dto = CategoryDto.builder().name("根目录").childrenNode(new ArrayList<>()).build();
        Category category = createCategory(null, "根目录", null, null);
        doReturn(category).when(convertor).toEntity(dto);
        doReturn(true).when(categoryService).save(any(Category.class));

        Boolean result = categoryService.create(dto);

        assertThat(result).isTrue();
        assertThat(category.getUniversityId()).isEqualTo("uni456");
    }

    @Test
    @DisplayName("create 有子节点时委托给 createByParentId")
    void createWithChildrenDelegatesToCreateByParentId() {
        CurrentUserPrincipal schoolUser = createMockUser(UserType.SCHOOL, "uni123");
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(schoolUser);
        CategoryDto childDto = CategoryDto.builder().name("子目录").childrenNode(Collections.emptyList()).build();
        List<CategoryDto> children = List.of(childDto);
        CategoryDto dto = CategoryDto.builder().name("根目录").childrenNode(children).build();
        Category category = createCategory("parent1", "根目录", null, null);
        doReturn(category).when(convertor).toEntity(dto);
        doReturn(true).when(categoryService).save(any(Category.class));
        // createByParentId 会查询父目录以创建子目录
        Category parent = createCategory("parent1", "根目录", 0, null);
        parent.setUniversityId("uni123");
        doReturn(parent).when(categoryService).getById("parent1");
        doReturn(createCategory(null, "子目录", null, null)).when(convertor).toEntity(childDto);

        Boolean result = categoryService.create(dto);

        assertThat(result).isTrue();
        // 验证保存了根目录
        verify(categoryService).save(category);
        // 验证 createByParentId 查询了父目录
        verify(categoryService).getById("parent1");
    }

    // ==================== createByParentId ====================

    @Test
    @DisplayName("createByParentId 父目录不存在时抛出 BusinessException 3003")
    void createByParentIdWhenParentNotFoundThrowsBusinessException() {
        doReturn(null).when(categoryService).getById("parent1");

        List<CategoryDto> children = List.of(
                CategoryDto.builder().name("子目录").childrenNode(Collections.emptyList()).build()
        );

        assertThatThrownBy(() -> categoryService.createByParentId(children, "parent1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_OR_CHILD_NOT_FOUND);
                    assertThat(be.getMessage()).isEqualTo("目录或子目录不存在");
                });
    }

    @Test
    @DisplayName("createByParentId children 为 null 时抛出 BusinessException 3003")
    void createByParentIdWhenChildrenIsNullThrowsBusinessException() {
        Category parent = createCategory("parent1", "父目录", 0, null);
        doReturn(parent).when(categoryService).getById("parent1");

        assertThatThrownBy(() -> categoryService.createByParentId(null, "parent1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_OR_CHILD_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("createByParentId 父目录层级已达最大时抛出 BusinessException 3004")
    void createByParentIdWhenParentLevelMaxThrowsBusinessException() {
        Category parent = createCategory("parent1", "父目录", 3, null);
        doReturn(parent).when(categoryService).getById("parent1");

        List<CategoryDto> children = List.of(
                CategoryDto.builder().name("子目录").childrenNode(Collections.emptyList()).build()
        );

        assertThatThrownBy(() -> categoryService.createByParentId(children, "parent1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_MAX_LEVEL);
                    assertThat(be.getMessage()).isEqualTo("目录层级不能超过3级");
                });
    }

    @Test
    @DisplayName("createByParentId 子节点层级将超过最大值时抛出 BusinessException 3004")
    void createByParentIdWhenChildLevelWouldExceedMaxThrowsBusinessException() {
        // 父目录 level=2，子目录 level=3，3 < MAX_LEVEL(3) 为 false
        Category parent = createCategory("parent1", "父目录", 2, null);
        parent.setUniversityId("uni123");
        doReturn(parent).when(categoryService).getById("parent1");

        CategoryDto childDto = CategoryDto.builder().name("超限子目录").childrenNode(Collections.emptyList()).build();
        List<CategoryDto> children = List.of(childDto);
        Category childEntity = createCategory(null, "超限子目录", null, null);
        doReturn(childEntity).when(convertor).toEntity(childDto);

        assertThatThrownBy(() -> categoryService.createByParentId(children, "parent1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_MAX_LEVEL);
                });
    }

    @Test
    @DisplayName("createByParentId 无孙子节点时成功创建子目录")
    void createByParentIdCreatesSingleChildSuccessfully() {
        Category parent = createCategory("parent1", "父目录", 0, null);
        parent.setUniversityId("uni123");
        doReturn(parent).when(categoryService).getById("parent1");

        CategoryDto childDto = CategoryDto.builder().name("子目录").description("子目录描述").childrenNode(Collections.emptyList()).build();
        List<CategoryDto> children = List.of(childDto);
        Category childEntity = createCategory(null, "子目录", null, null);
        doReturn(childEntity).when(convertor).toEntity(childDto);
        doReturn(true).when(categoryService).save(any(Category.class));

        Boolean result = categoryService.createByParentId(children, "parent1");

        assertThat(result).isTrue();
        assertThat(childEntity.getLevel()).isEqualTo(1);
        assertThat(childEntity.getParentId()).isEqualTo("parent1");
        assertThat(childEntity.getUniversityId()).isEqualTo("uni123");
        verify(categoryService).save(childEntity);
    }

    @Test
    @DisplayName("createByParentId 创建多个子目录成功")
    void createByParentIdCreatesMultipleChildrenSuccessfully() {
        Category parent = createCategory("parent1", "父目录", 0, null);
        parent.setUniversityId("uni123");
        doReturn(parent).when(categoryService).getById("parent1");

        CategoryDto child1 = CategoryDto.builder().name("子目录1").childrenNode(Collections.emptyList()).build();
        CategoryDto child2 = CategoryDto.builder().name("子目录2").childrenNode(Collections.emptyList()).build();
        List<CategoryDto> children = List.of(child1, child2);

        Category entity1 = createCategory(null, "子目录1", null, null);
        Category entity2 = createCategory(null, "子目录2", null, null);
        doReturn(entity1).when(convertor).toEntity(child1);
        doReturn(entity2).when(convertor).toEntity(child2);
        doReturn(true).when(categoryService).save(any(Category.class));

        Boolean result = categoryService.createByParentId(children, "parent1");

        assertThat(result).isTrue();
        verify(categoryService).save(entity1);
        verify(categoryService).save(entity2);
    }

    @Test
    @DisplayName("createByParentId 有孙子节点时递归创建")
    void createByParentIdWithGrandchildrenRecursivelyCreates() {
        Category parent = createCategory("parent1", "父目录", 0, null);
        parent.setUniversityId("uni123");
        doReturn(parent).when(categoryService).getById("parent1");

        CategoryDto grandchildDto = CategoryDto.builder().name("孙子目录").childrenNode(Collections.emptyList()).build();
        CategoryDto childDto = CategoryDto.builder().name("子目录").childrenNode(List.of(grandchildDto)).build();
        List<CategoryDto> children = List.of(childDto);

        Category childEntity = createCategory("child1", "子目录", null, null);
        Category grandchildEntity = createCategory(null, "孙子目录", null, null);
        doReturn(childEntity).when(convertor).toEntity(childDto);
        doReturn(grandchildEntity).when(convertor).toEntity(grandchildDto);
        doReturn(true).when(categoryService).save(any(Category.class));
        // 递归调用 createByParentId 时也需要查询子目录作为父目录
        doReturn(childEntity).when(categoryService).getById("child1");

        Boolean result = categoryService.createByParentId(children, "parent1");

        assertThat(result).isTrue();
        verify(categoryService).save(childEntity);
        verify(categoryService).save(grandchildEntity);
        verify(categoryService).getById("child1");
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete 目录有子目录时抛出 BusinessException 3005")
    void deleteWhenHasChildrenThrowsBusinessException() {
        doReturn(List.of(createCategory("child1", "子目录", 1, "parent1")))
                .when(categoryService).list(any(LambdaQueryWrapper.class));

        assertThatThrownBy(() -> categoryService.delete("parent1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_HAS_CHILDREN);
                    assertThat(be.getMessage()).isEqualTo("该目录有子目录，请先删除子目录");
                });
    }

    @Test
    @DisplayName("delete 目录无子目录时删除成功")
    void deleteWhenNoChildrenRemovesSuccessfully() {
        doReturn(Collections.emptyList()).when(categoryService).list(any(LambdaQueryWrapper.class));
        doReturn(true).when(categoryService).removeById("parent1");

        Boolean result = categoryService.delete("parent1");

        assertThat(result).isTrue();
        verify(categoryService).removeById("parent1");
    }

    // ==================== deleteByIdWithChildren ====================

    @Test
    @DisplayName("deleteByIdWithChildren 目录不存在时抛出 BusinessException 3001")
    void deleteByIdWithChildrenWhenCategoryNotFoundThrowsBusinessException() {
        doReturn(null).when(categoryService).getById("nonexistent");

        assertThatThrownBy(() -> categoryService.deleteByIdWithChildren("nonexistent"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_NOT_FOUND);
                    assertThat(be.getMessage()).isEqualTo("目录不存在");
                });
    }

    @Test
    @DisplayName("deleteByIdWithChildren 目录无子目录时直接删除")
    void deleteByIdWithChildrenWhenNoChildrenRemovesDirectly() {
        Category category = createCategory("parent1", "目录", 0, null);
        doReturn(category).when(categoryService).getById("parent1");
        doReturn(Collections.emptyList()).when(categoryService).list(any(LambdaQueryWrapper.class));
        doReturn(true).when(categoryService).removeById("parent1");

        Boolean result = categoryService.deleteByIdWithChildren("parent1");

        assertThat(result).isTrue();
        verify(categoryService).removeById("parent1");
    }

    // ==================== update ====================

    @Test
    @DisplayName("update dto 为 null 时抛出 BusinessException 3002")
    void updateWhenDtoIsNullThrowsBusinessException() {
        assertThatThrownBy(() -> categoryService.update(null, "1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_ARGS_ERROR);
                    assertThat(be.getMessage()).isEqualTo("参数错误");
                });
    }

    @Test
    @DisplayName("update 目录不存在时抛出 BusinessException 3001")
    void updateWhenCategoryNotFoundThrowsBusinessException() {
        doReturn(null).when(categoryService).getById("nonexistent");
        CategoryDto dto = CategoryDto.builder().name("更新名称").build();

        assertThatThrownBy(() -> categoryService.update(dto, "nonexistent"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_NOT_FOUND);
                    assertThat(be.getMessage()).isEqualTo("目录不存在");
                });
    }

    @Test
    @DisplayName("update 正常更新目录信息成功")
    void updateUpdatesSuccessfully() {
        Category existing = createCategory("1", "旧名称", 0, null);
        existing.setUniversityId("uni123");
        doReturn(existing).when(categoryService).getById("1");

        CategoryDto dto = CategoryDto.builder().name("新名称").description("新描述").build();
        Category updatedEntity = createCategory(null, "新名称", null, null);
        doReturn(updatedEntity).when(convertor).toEntity(dto);
        doReturn(true).when(categoryService).updateById(any(Category.class));

        Boolean result = categoryService.update(dto, "1");

        assertThat(result).isTrue();
        assertThat(updatedEntity.getId()).isEqualTo("1");
        assertThat(updatedEntity.getUniversityId()).isEqualTo("uni123");
        assertThat(updatedEntity.getLevel()).isEqualTo(0);
        assertThat(updatedEntity.getParentId()).isNull();
        verify(categoryService).updateById(updatedEntity);
    }

    // ==================== helper methods ====================

    private Category createCategory(String id, String name, Integer level, String parentId) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setLevel(level);
        category.setParentId(parentId);
        return category;
    }

    private CategoryVO createCategoryVO(String id, String name, String parentId, List<CategoryVO> children) {
        CategoryVO vo = new CategoryVO();
        vo.setId(id);
        vo.setName(name);
        vo.setParentId(parentId);
        vo.setChildren(children);
        return vo;
    }

    private CurrentUserPrincipal createMockUser(UserType userType, String universityId) {
        CurrentUserPrincipal user = mock(CurrentUserPrincipal.class);
        lenient().when(user.getUserType()).thenReturn(userType);
        lenient().when(user.getUniversityId()).thenReturn(universityId);
        return user;
    }
}
