package com.framework.loippi.result.common.activity;

import com.framework.loippi.entity.product.ShopGoodsEvaluate;
import com.framework.loippi.mybatis.ext.annotation.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 用户已经评价的商品列表返回app结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEvaluateGoodsResult {

    public static final String POSITIVE_COMMENT = "已好评";

    public static final String MODERATE_COMMENT = "已中评";

    public static final String NEGATIVE_COMMENT = "已差评";

    // 好评标准分
    public static final int STANDARD_OF_POSITIVE_COMMENT_SCORE = 3;

    // 中评标准
    public static final int STANDARD_OF_MODERATE_COMMENT_SCORE = 2;

    // 差评标准
    public static final int STANDARD_OF_NEGATIVE_COMMENT_SCORE = 1;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 评价id
     */
    private String evalGoodsId;

    /**
     * 图片
     */
    private String goodsImage;


    /**
     * 评价类型 好评 差评 中评
     */
    private String commentType;

    /**
     * 评价时间
     */
    private Date evaluateTime;

    /**
     * 评价内容
     */
    private String comment;


    /**
     * 评价人编号
     */
    private Long gevalFrommemberid;

    /**
     * 评价人名称
     */
    private String gevalFrommembername;

    /**
     * 评价人头像
     */
    private String gevalFrommemberAvatar;

    /**
     * 购买规格信息
     */
    private String specInfo;
    /**
     * 商家回复
     */
    private String gevalRemark;
    /**
     * 晒单图片
     */
    private String gevalImage;


    /**
     * 1-5分
     */
    private Integer gevalScore;

    public void setCommentType(int score) {

        if (score >= STANDARD_OF_POSITIVE_COMMENT_SCORE) {
            commentType = POSITIVE_COMMENT;

        } else if (score >= STANDARD_OF_MODERATE_COMMENT_SCORE) {
            commentType = MODERATE_COMMENT;

        } else {
            commentType = NEGATIVE_COMMENT;
        }
    }

    public static List<ActivityEvaluateGoodsResult> build(List<ShopGoodsEvaluate> evaluateGoodsList) {
        List<ActivityEvaluateGoodsResult> results = new ArrayList<>();
        for (ShopGoodsEvaluate evaluateGoods : evaluateGoodsList) {
            ActivityEvaluateGoodsResult result = new ActivityEvaluateGoodsResult();
            result.setEvalGoodsId(Optional.ofNullable(evaluateGoods.getId().toString()).orElse(""));
            result.setGoodsName(Optional.ofNullable(evaluateGoods.getGevalGoodsname()).orElse(""));
            // 香港直邮 深圳保税区
            Integer gevalScores = evaluateGoods.getGevalScores();
            if (gevalScores != null) {
                result.setCommentType(evaluateGoods.getGevalScores()); // 评价类型 好评 差评 中评
            }
            result.setEvaluateTime(Optional.ofNullable(evaluateGoods.getCreateTime()).orElse(new Date()));
            result.setComment(Optional.ofNullable(evaluateGoods.getGevalContent()).orElse(""));
            result.setGevalFrommemberAvatar(Optional.ofNullable(evaluateGoods.getGevalFrommemberAvatar()).orElse(""));
            result.setGevalFrommembername(Optional.ofNullable(evaluateGoods.getNick()).orElse(""));
            result.setGevalFrommemberid(Optional.ofNullable(evaluateGoods.getGevalFrommemberid()).orElse(-1L));
            result.setGevalScore(Optional.ofNullable(evaluateGoods.getGevalScores()).orElse(0));
            result.setGoodsImage(Optional.ofNullable(evaluateGoods.getGoodsImage()).orElse(""));
            result.setGevalImage(Optional.ofNullable(evaluateGoods.getGevalImage()).orElse(""));
            result.setSpecInfo(Optional.ofNullable(evaluateGoods.getSpecInfo()).orElse(""));
            result.setGevalRemark(Optional.ofNullable(evaluateGoods.getGevalRemark()).orElse(""));
            results.add(result);
        }
        return results;
    }
}
