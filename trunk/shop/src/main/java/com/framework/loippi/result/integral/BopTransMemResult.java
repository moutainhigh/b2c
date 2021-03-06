package com.framework.loippi.result.integral;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.framework.loippi.entity.user.RdMmBasicInfo;
import com.framework.loippi.entity.user.RdMmRelation;

/**
 * @author zc
 * @date 2020-09-04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BopTransMemResult{
    private static final long serialVersionUID = 5081846432919091193L;

    private String mmCode;//会员编号

    private String mmNickName;//会员昵称

    private String mmAvatar;//会员头像

    private String mobile;//手机号码

    private String rankName;//级别名称

    private Integer rank;//级别

    public static List<BopTransMemResult> build(ArrayList<RdMmBasicInfo> infos, HashMap<String, RdMmRelation> relationMap, HashMap<Integer, String> rankMap, Map<String,String> remarkMap) {
        ArrayList<BopTransMemResult> results = new ArrayList<>();
        for (RdMmBasicInfo info : infos) {
            BopTransMemResult result = new BopTransMemResult();
            result.setMmCode(info.getMmCode());
            if (!remarkMap.isEmpty()&&remarkMap.containsKey(info.getMmCode())){
                result.setMmNickName(Optional.ofNullable(remarkMap.get(info.getMmCode())).orElse(""));
            }else {
                result.setMmNickName(Optional.ofNullable(info.getMmNickName()).orElse(""));
            }
            result.setMmAvatar(info.getMmAvatar());
            result.setMobile(info.getMobile());
            RdMmRelation rdMmRelation = relationMap.get(info.getMmCode());
            Integer rank = rdMmRelation.getRank();
            String rankName = "";
            if(rank==1){
                rankName=rankMap.get(2);
            }else {
                rankName=rankMap.get(rank);
            }
            result.setRank(rank);
            result.setRankName(rankName);
            results.add(result);
        }
        return results;
    }
}
