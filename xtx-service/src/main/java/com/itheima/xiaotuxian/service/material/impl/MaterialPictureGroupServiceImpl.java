package com.itheima.xiaotuxian.service.material.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.xiaotuxian.constant.enums.ErrorMessageEnum;
import com.itheima.xiaotuxian.constant.statics.MaterialStatic;
import com.itheima.xiaotuxian.entity.material.MaterialPictureGroup;
import com.itheima.xiaotuxian.exception.BusinessException;
import com.itheima.xiaotuxian.mapper.material.MaterialPictureGroupMapper;
import com.itheima.xiaotuxian.service.material.MaterialPictureGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author: itheima
 * @Date: 2023/7/11 3:34 下午
 * @Description:
 */
@Service
@Transactional
public class MaterialPictureGroupServiceImpl extends ServiceImpl<MaterialPictureGroupMapper, MaterialPictureGroup> implements MaterialPictureGroupService {
    @Override
    public Boolean saveGroup(MaterialPictureGroup pictureGroup, String opUser) {
        var pid = StrUtil.isEmpty(pictureGroup.getPid()) ? "0" : pictureGroup.getPid();
        if (StrUtil.isEmpty(pictureGroup.getId())) {
            pictureGroup.setCreator(opUser);
        } else {
            var source = getById(pictureGroup.getId());
            if (source == null) {
                throw new BusinessException(ErrorMessageEnum.OBJECT_DOES_NOT_EXIST);
            }
        }
        //校验数据有效性
        // 1.同层不允许重名
        checkName(pictureGroup.getId(), pid, pictureGroup.getName());
        // 2.校验层级并填充
        pictureGroup.setLayer(getLayer(pid));
        return saveOrUpdate(pictureGroup);
    }

    @Override
    public List<MaterialPictureGroup> findAll(Integer state) {
        return list(Wrappers
                .<MaterialPictureGroup>lambdaQuery()
                .eq(state != null, MaterialPictureGroup::getState, state)
        );
    }

    /**
     * 检查名称是否可用
     *
     * @param id   当前操作对象id
     * @param pid  当前操作对象父id
     * @param name 待检查名称
     */
    private void checkName(String id, String pid, String name) {
        Optional.ofNullable(getOne(Wrappers
                .<MaterialPictureGroup>lambdaQuery()
                .eq(MaterialPictureGroup::getPid, pid)
                .eq(MaterialPictureGroup::getName, name)
                .ne(StrUtil.isNotEmpty(id), MaterialPictureGroup::getId, id)))
                .ifPresent(sameEntity -> {
                    Stream.of(sameEntity.getState()).filter(state -> MaterialStatic.STATE_MATERIAL_NORMAL == state).forEach(state -> {
                        throw new BusinessException(ErrorMessageEnum.MATERIAL_GROUP_DUPLICATE);
                    });
                    Stream.of(sameEntity.getState()).filter(state -> MaterialStatic.STATE_MATERIAL_TRASH == state).forEach(state -> {
                        throw new BusinessException(ErrorMessageEnum.MATERIAL_GROUP_DUPLICATE_TRASH);
                    });
                });
    }

    /**
     * 检验层级并获取可用层级
     *
     * @param pid 父级id
     * @return 可用层级
     */
    private Integer getLayer(String pid) {
        var layer = 1;
        if (!"0".equals(pid)) {
            var parent = getById(pid);
            if (parent == null) {
                throw new BusinessException(ErrorMessageEnum.MATERIAL_GROUP_PARENT_INVALID);
            }
            layer = parent.getLayer() + 1;
        }
        if (layer > MaterialStatic.GROUP_MAX_LAYER) {
            throw new BusinessException(ErrorMessageEnum.MATERIAL_GROUP_OUT_OF_LAYER);
        }
        return layer;
    }
}
