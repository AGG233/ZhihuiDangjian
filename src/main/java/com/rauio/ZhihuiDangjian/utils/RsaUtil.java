package com.rauio.ZhihuiDangjian.utils;

import com.rauio.ZhihuiDangjian.constants.SecurityConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@Setter
@Getter
@NoArgsConstructor
public class RsaUtil {

    private static volatile RsaUtil rsaUtil;

    public KeyPair generateRsaKey() throws NoSuchAlgorithmException {
        synchronized(RsaUtil.class) {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(SecurityConstants.RSA_KEY_ALGORITHM);
            keyPairGen.initialize(SecurityConstants.RSA_KEY_SIZE);
            return keyPairGen.generateKeyPair();
        }
    }

}