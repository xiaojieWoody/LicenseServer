package com.mylicense.license;

import com.mylicense.license.model.LicenseCheckModel;
import com.mylicense.license.param.CustomKeyStoreParam;
import com.mylicense.license.param.LicenseCreatorParam;
import de.schlichtherle.license.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.prefs.Preferences;

@Slf4j
public class LicenseCreator {

    private final static X500Principal DEFAULT_HOLDER_AND_ISSUER = new X500Principal("CN=localhost, OU=localhost, O=localhost, L=SH, ST=SH, C=CN");
    private final static String licensePrefix = "license";
    private final static String licenseSuffix = ".dat";

    private LicenseCreatorParam param;

    public LicenseCreator(LicenseCreatorParam param) {
        this.param = param;
    }

    public boolean generateLicense(LicenseCheckModel licenseCheckModel) throws IOException {
        // 临时文件
        File f = null;
        OutputStream toClient = null;

        try {
            LicenseManager licenseManager = new CustomLicenseManager(initLicenseParam(),licenseCheckModel);
            LicenseContent licenseContent = initLicenseContent();

            // 浏览器下载
            // 创建临时文件
            f = File.createTempFile(licensePrefix, licenseSuffix);
            licenseManager.store(licenseContent,f);
            byte[] bytes = Files.readAllBytes(f.toPath());
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            response.addHeader("Content-Disposition", "attachment;filename="  + licensePrefix + licenseSuffix);
            toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("*/*");
            toClient.write(bytes);
            toClient.flush();

            return true;
        } catch (Exception e) {
            log.error(MessageFormat.format("证书生成失败：{0}",param),e);
            return false;
        } finally {
            if(f != null) {
                f.deleteOnExit();
            }
            if(toClient != null) {
                toClient.close();
            }
        }
    }

    /**
     * 初始化证书生成参数
     */
    private LicenseParam initLicenseParam() {

        Preferences preferences = Preferences.userNodeForPackage(LicenseCreator.class);
        //设置对证书内容加密的秘钥
        CipherParam cipherParam = new DefaultCipherParam(param.getStorePass());
        KeyStoreParam privateStoreParam = new CustomKeyStoreParam(LicenseCreator.class
                ,param.getPrivateKeysStorePath()
                ,param.getPrivateAlias()
                ,param.getStorePass()
                ,param.getKeyPass());
        LicenseParam licenseParam = new DefaultLicenseParam(param.getSubject()
                ,preferences
                ,privateStoreParam
                ,cipherParam);
        return licenseParam;
    }

    /**
     * 设置证书生成正文信息
     */
    private LicenseContent initLicenseContent() {
        LicenseContent licenseContent = new LicenseContent();
        licenseContent.setHolder(DEFAULT_HOLDER_AND_ISSUER);
        licenseContent.setIssuer(DEFAULT_HOLDER_AND_ISSUER);

        licenseContent.setSubject(param.getSubject());
        licenseContent.setIssued(param.getIssuedTime());
        licenseContent.setNotBefore(param.getIssuedTime());
        licenseContent.setNotAfter(param.getExpiryTime());
        licenseContent.setConsumerType(param.getConsumerType());
        licenseContent.setConsumerAmount(param.getConsumerAmount());
        licenseContent.setInfo(param.getDescription());
        //扩展校验服务器硬件信息
        licenseContent.setExtra(param.getLicenseCheckModel());

        return licenseContent;
    }
}
