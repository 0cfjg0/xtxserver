package com.itheima.xiaotuxian.service.member.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.xiaotuxian.constant.statics.CommonStatic;
import com.itheima.xiaotuxian.constant.statics.GoodsStatic;
import com.itheima.xiaotuxian.constant.statics.UserMemberStatic;
import com.itheima.xiaotuxian.entity.goods.GoodsSku;
import com.itheima.xiaotuxian.entity.goods.GoodsSpu;
import com.itheima.xiaotuxian.entity.member.UserMemberCart;
import com.itheima.xiaotuxian.mapper.goods.GoodsSkuMapper;
import com.itheima.xiaotuxian.mapper.goods.GoodsSpuMapper;
import com.itheima.xiaotuxian.mapper.member.UserMemberCartMapper;
import com.itheima.xiaotuxian.service.goods.GoodsService;
import com.itheima.xiaotuxian.service.goods.GoodsSpuService;
import com.itheima.xiaotuxian.service.marketing.MarketingRecommendService;
import com.itheima.xiaotuxian.service.member.UserMemberCartService;
import com.itheima.xiaotuxian.service.member.UserMemberCollectService;
import com.itheima.xiaotuxian.util.HighLevelUtil;
import com.itheima.xiaotuxian.vo.goods.goods.SkuSpecVo;
import com.itheima.xiaotuxian.vo.member.BatchDeleteCartVo;
import com.itheima.xiaotuxian.vo.member.CartSaveVo;
import com.itheima.xiaotuxian.vo.member.CartSelectedVo;
import com.itheima.xiaotuxian.vo.member.CartVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class UserMemberCartServiceImpl extends ServiceImpl<UserMemberCartMapper, UserMemberCart> implements UserMemberCartService {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsSpuService goodsSpuService;
    @Autowired
    private MarketingRecommendService recommendService;
    @Autowired
    private UserMemberCollectService collectService;

    @Autowired
    private UserMemberCartMapper userMemberCartMapper;
    @Autowired
    private HighLevelUtil highLevelUtil;

    @Autowired
    private GoodsSkuMapper goodsSkuMapper;
    @Autowired
    private GoodsSpuMapper goodsSpuMapper;

    //下面是用户id和用户名,之后要改成从令牌获取
//    private String memberId = "1663375385531781122";
//    private String client = "xiaotuxian001";

    /**
     * @param {UserMemberCart} cart
     * @param {String}         memberId
     * @param {String}         client
     * @return {*}
     * @description: 填充购物车的信息
     * @author: lbc
     */
    private CartVo fillCart(UserMemberCart cart, String memberId, String client) {
        var cartVo = new CartVo();
        cartVo.setId(cart.getSpuId());
        cartVo.setSkuId(cart.getSkuId());

        var goodsStateValid = new AtomicBoolean(false);
        Optional.ofNullable(goodsSpuService.getById(cartVo.getId())).ifPresent(spu -> {
            cartVo.setName(spu.getName());
            cartVo.setPicture(goodsService.getSpuPicture(spu.getId(), CommonStatic.REQUEST_CLIENT_PC.equals(client) ? CommonStatic.MATERIAL_SHOW_PC : CommonStatic.MATERIAL_SHOW_APP));
            goodsStateValid.set((GoodsStatic.STATE_STORING == spu.getState() || GoodsStatic.STATE_SELLING == spu.getState())
                    && GoodsStatic.AUDIT_STATE_PASS == spu.getAuditState()
                    && GoodsStatic.EDIT_STATE_SUBMIT == spu.getEditState());
            cartVo.setPostFee(spu.getTransportCost());
        });
        Optional.ofNullable(goodsService.findSkuById(cartVo.getSkuId())).ifPresent(sku -> {
            cartVo.setStock(sku.getSaleableInventory());
            //手机版的因为自己更改样式（只是值得拼接）,并且返回规格的集合
            var specs = new ArrayList<SkuSpecVo>();
            cartVo.setAttrsText(goodsService.getGoodsAttrsText(sku.getId(), client, specs));
            cartVo.setSpecs(specs);
            //当前价格
            cartVo.setNowOriginalPrice(sku.getSellingPrice());
            cartVo.setNowPrice(sku.getSellingPrice());
            Optional.ofNullable(recommendService.findOne(sku.getSpuId(), 1)).ifPresent(recommend -> {
                cartVo.setNowPrice((cartVo.getNowPrice().multiply(recommend.getDiscount())));
                cartVo.setNowPrice(cartVo.getNowPrice().divide(new BigDecimal(10), 2, RoundingMode.HALF_UP));
            });
        });
        cartVo.setIsEffective(goodsStateValid.get() && cartVo.getStock() > 0);
        //处理折扣
        Optional.ofNullable(recommendService.findOne(cart.getSpuId(), 1)).ifPresent(recommend -> cartVo.setDiscount(recommend.getDiscount()));
        //处理收藏
        cartVo.setIsCollect(false);
        Optional.ofNullable(memberId).filter(StrUtil::isNotEmpty).ifPresent(uid ->
                Optional.ofNullable(collectService.countCollect(uid, cart.getSpuId(), UserMemberStatic.COLLECT_TYPE_GOODS)).ifPresent(count -> cartVo.setIsCollect(count > 0)));
        //处理其他信息
        cartVo.setPrice(cart.getPrice());
        cartVo.setSelected(cart.getSeleted());
        cartVo.setCount(cart.getQuantity());
        return cartVo;
    }

    /**
     * 1. 新增购物车信息
     *
     * @param cartSaveVo
     * @return CartVo
     */
    @Override
    @Transactional
    public CartVo saveCart(CartSaveVo cartSaveVo, String userId) {
        // 1.创建 用户购物车 对象
        UserMemberCart userMemberCart = null;
        try {
            userMemberCart = userMemberCartMapper.getCart(userId, cartSaveVo.getSkuId());
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("-----456--------------");
        }
        // 判断 userMemberCart1 是否为空,为空 说明数据库中没有,直接新增保存. 不为空 说明该商品已经存在,需要修改数量
        if (userMemberCart != null) {
            // 计算该商品的数量,根据商品id 来修改
            userMemberCart.setQuantity(cartSaveVo.getCount() + userMemberCart.getQuantity());
            userMemberCartMapper.updateById(userMemberCart);
        } else {
            userMemberCart = new UserMemberCart();
            // 1.1设置 购物车对象的 相关信息
            userMemberCart.setCreateTime(LocalDateTime.now());
            userMemberCart.setMemberId(userId);
            userMemberCart.setSkuId(cartSaveVo.getSkuId());//商品ID
            userMemberCart.setQuantity(cartSaveVo.getCount());
            //通过 商品id (skuId),从数据库中找到spuId  selectSpuIdBySkuId()
            String spuId = userMemberCartMapper.selectCart(cartSaveVo.getSkuId());
            userMemberCart.setSpuId(spuId);
            //通过商品id (spuId),从数据库中找到spuId对应的price
            BigDecimal price = userMemberCartMapper.selectCartPriceById(spuId);
            userMemberCart.setPrice(price);
            userMemberCartMapper.insert(userMemberCart);
        }

        //3.调用上面的fillCart()方法,将cartVo对象响应到前端
        // 创建 CartVo对象,并对该对象的每个属性 设置值(也可一个个set)
        //log.info("购物车添加成功后,返回给前端的商品信息:{}", cartVo);
        return this.fillCart(userMemberCart, userId, "pc");
    }

    /**
     * 2. 获取用户购物车列表
     * //* @param memberId 用户Id
     *
     * @return 购物车列表
     */
    @Override
    @Transactional
    public List<CartVo> getCarts(String userId) {
        // 1.在购物车表中,通过用户id(memberId)查询购物车中所有商品
        List<UserMemberCart> userMemberList = userMemberCartMapper.getCarts(userId);
        // 2.把 UserMemberCart 对象  转换为 CartVo 对象
        List<CartVo> list = new ArrayList<>();
        for (UserMemberCart userMemberCart : userMemberList) {
            CartVo cartVo = this.fillCart(userMemberCart, userId, "pc");
            list.add(cartVo);
        }
        // 3.返回给前端
        return list;
    }


    /**
     * 3. 购物车全选/全不选
     *
     * @param cartSelectedVo
     */
    @Override
    public void selectAllCarts(CartSelectedVo cartSelectedVo) {
        // 1.获取所有需要修改的商品id [skuId]
        List<String> skuIds = cartSelectedVo.getIds();
        // 2.根据每一个商品id,修改该商品的选中状态
        for (String skuId : skuIds) {
            userMemberCartMapper.updateSeletedBySkuId(cartSelectedVo.getSelected(), skuId);
        }
    }

    /**
     * 获取用户购物车数量
     *
     * @param userId
     * @return
     */
    @Override
    public Integer getCartCount(String memberId) {
        Integer count = userMemberCartMapper.sumQuntity(memberId);
        return count;
    }

    /**
     * 合并购物车
     *
     * @param cartSaveVoList
     * @param userId
     */
    @Override
    public void mergeCartCout(List<CartSaveVo> cartSaveVoList, String memberId) {
        //判断重复添加的商品
        for (CartSaveVo cartSaveVo : cartSaveVoList) {
            saveCart(cartSaveVo, memberId);
        }
    }

    /**
     * 修改购物车商品
     *
     * @param cartSaveVo
     * @param userId
     * @return
     */
    @Override
    public CartVo updateUserCart(CartSaveVo cartSaveVo, String memberId) {
        UserMemberCart cart = userMemberCartMapper.getCart(memberId, cartSaveVo.getSkuId());
        System.out.println("------------" + cartSaveVo);
        cart.setQuantity(cartSaveVo.getCount());
        cart.setSeleted(cartSaveVo.getSelected());
        int i = userMemberCartMapper.updateById(cart);
        System.out.println("==========================" + i);
        CartVo cartVo = fillCart(cart, memberId, "pc");
        return cartVo;
    }

    /**
     * 清空/删除购物车商品
     *
     * @param batchDeleteCartVo
     * @param userId
     * @return
     */
    @Override
    public void deleteUserCart(BatchDeleteCartVo batchDeleteCartVo, String memberId) {
        List<String> ids = batchDeleteCartVo.getIds();
        for (String id : ids) {
            userMemberCartMapper.deleteUserCart(memberId, id);
        }
    }


}
