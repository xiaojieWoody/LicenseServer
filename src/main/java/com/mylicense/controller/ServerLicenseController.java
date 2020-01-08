package com.mylicense.controller;

import com.alibaba.fastjson.JSON;
import com.mylicense.common.ResMsg;
import com.mylicense.config.LicenseConfig;
import com.mylicense.license.LicenseCreator;
import com.mylicense.license.model.LicenseCheckModel;
import com.mylicense.license.param.LicenseCreatorParam;
import com.mylicense.license.param.LicenseParamBean;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("/server")
public class ServerLicenseController {

    @Autowired
    private LicenseConfig licenseConfig;

    /**
     * 生成证书
     * @param param
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/generateLicense",produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResMsg generateLicense(@RequestBody(required = true) LicenseParamBean param) throws IOException {

        LicenseCreatorParam licenseCreatorParam = new LicenseCreatorParam();
        String machineCode = param.getMachineCode();
        // 转Base64
        byte[] decode = Base64.getUrlDecoder().decode(machineCode);
        LicenseCheckModel licenseCheckModel = (LicenseCheckModel)JSON.parseObject(decode, LicenseCheckModel.class);
        // 限制访问次数
        licenseCheckModel.setTotalCount(param.getTotalCount());
        // 客户服务器硬件信息
        licenseCreatorParam.setLicenseCheckModel(licenseCheckModel);
        BeanUtils.copyProperties(licenseConfig, licenseCreatorParam);
        licenseCreatorParam.setIssuedTime(new Date());
        licenseCreatorParam.setExpiryTime(param.getExpiryTime());
        licenseCreatorParam.setConsumerType(param.getConsumerType());
        licenseCreatorParam.setConsumerAmount(param.getConsumerAmount());
        licenseCreatorParam.setDescription(param.getDescription());

        LicenseCreator licenseCreator = new LicenseCreator(licenseCreatorParam);
        boolean result = licenseCreator.generateLicense();

        if(result){
            return new ResMsg(200, "success","",null);
        }else{
            return new ResMsg(-1, "fail","证书文件生成失败！",null);
        }
    }
}
