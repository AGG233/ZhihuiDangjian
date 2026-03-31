package com.rauio.smartdangjian.server.resource.aop;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResolver;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaCreateRequest;
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ResourceMetaAccessAspect implements DataScopeResolver {

    private final ResourceMetaService resourceMetaService;
    private final UserMapper userMapper;

    @Override
    public boolean supports(String resource) {
        return DataScopeResources.RESOURCE_META_ADMIN.equals(resource);
    }

    @Override
    public void before(DataScopeContext context) {
        CurrentUserPrincipal currentUser = context.getCurrentUser();
        if (currentUser.getUserType() == UserType.MANAGER) {
            return;
        }
        if (currentUser.getUserType() != UserType.SCHOOL) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权管理资源");
        }
        requireUniversityId(currentUser);

        switch (context.getAccess().action()) {
            case CREATE -> {
                ResourceMetaCreateRequest request = context.require(context.getAccess().body(), ResourceMetaCreateRequest.class, "资源信息不能为空");
                request.setUploaderId(currentUser.getId());
            }
            case READ, UPDATE -> assertMetaInSameUniversity(currentUser, resolveMeta(context));
            case DELETE -> assertDeleteAllowed(currentUser, context);
            default -> {
            }
        }
    }

    @Override
    public Object after(DataScopeContext context, Object result) {
        CurrentUserPrincipal currentUser = context.getCurrentUser();
        if (currentUser.getUserType() != UserType.SCHOOL || context.getAccess().action() != DataScopeAction.SEARCH) {
            return result;
        }
        if (!(result instanceof Result<?> wrapped) || !(wrapped.getData() instanceof List<?> data)) {
            return result;
        }
        List<ResourceMeta> filtered = data.stream()
                .filter(ResourceMeta.class::isInstance)
                .map(ResourceMeta.class::cast)
                .filter(meta -> belongsToCurrentSchool(currentUser, meta))
                .toList();
        setResultData(wrapped, filtered);
        return wrapped;
    }

    private ResourceMeta resolveMeta(DataScopeContext context) {
        if (StringUtils.isNotBlank(context.getAccess().id())) {
            String id = context.require(context.getAccess().id(), String.class, "资源ID不能为空");
            return resourceMetaService.get(id);
        }
        String hash = context.require(context.getAccess().query(), String.class, "资源hash不能为空");
        return resourceMetaService.getByHash(hash);
    }

    private void assertDeleteAllowed(CurrentUserPrincipal currentUser, DataScopeContext context) {
        if (StringUtils.isNotBlank(context.getAccess().id())) {
            assertMetaInSameUniversity(currentUser, resolveMeta(context));
            return;
        }
        Object queryValue = context.resolve(context.getAccess().query(), Object.class);
        if (queryValue instanceof String hash) {
            assertMetaInSameUniversity(currentUser, resourceMetaService.getByHash(hash));
            return;
        }
        if (queryValue instanceof String[] hashes) {
            for (String hash : hashes) {
                assertMetaInSameUniversity(currentUser, resourceMetaService.getByHash(hash));
            }
            return;
        }
        throw new BusinessException(ErrorConstants.ARGS_ERROR, "资源删除参数不能为空");
    }

    private void assertMetaInSameUniversity(CurrentUserPrincipal currentUser, ResourceMeta meta) {
        if (!belongsToCurrentSchool(currentUser, meta)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权管理本校外资源");
        }
    }

    private boolean belongsToCurrentSchool(CurrentUserPrincipal currentUser, ResourceMeta meta) {
        if (meta == null || StringUtils.isBlank(meta.getUploaderId())) {
            return false;
        }
        User uploader = userMapper.selectById(meta.getUploaderId());
        return uploader != null && Objects.equals(currentUser.getUniversityId(), uploader.getUniversityId());
    }

    private void requireUniversityId(CurrentUserPrincipal currentUser) {
        if (StringUtils.isBlank(currentUser.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "当前高校管理员未绑定学校");
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setResultData(Result<?> result, Object data) {
        ((Result) result).setData(data);
    }
}
