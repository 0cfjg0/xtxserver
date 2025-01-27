package com.itheima.xiaotuxian.service.member;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.xiaotuxian.entity.member.UserMemberCart;
import com.itheima.xiaotuxian.vo.member.BatchDeleteCartVo;
import com.itheima.xiaotuxian.vo.member.CartSaveVo;
import com.itheima.xiaotuxian.vo.member.CartSelectedVo;
import com.itheima.xiaotuxian.vo.member.CartVo;

import java.util.List;

public interface UserMemberCartService extends IService<UserMemberCart> {

    /**
     * 1. 保存购物车商品信息
     *
     * @param cartSaveVo
     * @return 购物车商品信息
     */
    public CartVo saveCart(CartSaveVo cartSaveVo,String userId);




    /**
     * 批量删除用户购物车商品
     *
     * @param batchDeleteCartVo    sku id集合
     * @param memberId 用户Id
     * @return 操作结果
     */
    /**
     * 清空/删除购物车商品
     *
     * @param batchDeleteCartVo
     * @param userId
     * @return
     */
    void deleteUserCart(BatchDeleteCartVo batchDeleteCartVo, String userId);

    /**
     * 2. 获取用户购物车列表
     *
     //* @param memberId 用户Id
     * @return 购物车列表
     */
    public List<CartVo> getCarts(String userId);

    /**
     * 获取用户购物车数量
     *
     * @return
     * @param userId
     */
    Integer getCartCount(String userId);

    /**
     * 合并购物车
     *
     * @param cartSaveVoList
     * @param userId
     */
    void mergeCartCout(List<CartSaveVo> cartSaveVoList, String userId);

    /**
     * 修改购物车商品
     *
     * @param cartSaveVo
     * @param userId
     * @return
     */
    CartVo updateUserCart(CartSaveVo cartSaveVo, String userId);

    /**
     * 3. 购物车全选/全不选
     * @param cartSelectedVo
     */
    public void selectAllCarts(CartSelectedVo cartSelectedVo);
}
