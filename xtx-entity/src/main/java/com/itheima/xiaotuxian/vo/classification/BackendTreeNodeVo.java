package com.itheima.xiaotuxian.vo.classification;

import lombok.Data;

import java.util.List;

@Data
public class BackendTreeNodeVo {
    private String id;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 状态，0为正常，1为停用
     */
    private Integer state;
    /**
     * 层级，从1开始
     */
    private Integer layer;
    /**
     * 父级类目信息
     */
    private BackendSimpleVo parent;
    /**
     * 子级类目集合
     */
    private List<BackendTreeNodeVo> children;
}
