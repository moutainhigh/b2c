package com.framework.loippi.param.user;

import com.framework.loippi.mybatis.ext.annotation.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Param - 收货地址-新增
 *
 * @author Loippi team
 * @version 2.0
 * @description 收货地址-新增
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddrsAddParam {

    /**
     * 收货人
     */
    @NotEmpty
    private String name;

    /**
     * 手机
     */
    @NotEmpty
    private String mobile;

    /**
     * 详细地址
     */
    @NotEmpty
    private String addr;

    /**
     * 是否为默认地址1:是0:否
     */
    @NotNull
    private Integer isDefault;

    /**
     * 省
     */
    @NotNull
    private String addProvinceCode;

    /**
     * 市
     */
    @NotNull
    private String addCityCode;

    /**
     * 区县
     */
    @NotNull
    private String addCountryCode;



}
