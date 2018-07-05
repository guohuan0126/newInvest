package com.jiuyi.ndr.service.xm.util;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * 加密、翻译
 */
public class SHA1withRSA {

    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    private static final int KEY_SIZE = 1024;

    /**
     * 生成签名
     */
    public static byte[] sign(byte[] data, byte[] privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException
    {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);

        KeyFactory kf = KeyFactory.getInstance("RSA");

        PrivateKey pk = kf.generatePrivate(keySpec);

        Signature signature = Signature.getInstance("SHA1withRSA");

        signature.initSign(pk);

        signature.update(data);

        return signature.sign();
    }

    /**
     * 验证
     */
    public static boolean verify(byte[] sign, byte[] data, byte[] publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException
    {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);

        KeyFactory kf = KeyFactory.getInstance("RSA");

        PublicKey pk = kf.generatePublic(keySpec);

        Signature signature = Signature.getInstance("SHA1withRSA");

        signature.initVerify(pk);

        signature.update(data);

        return signature.verify(sign);
    }

    public static Map<String, byte[]> initKeyMap()
            throws NoSuchAlgorithmException
    {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

        kpg.initialize(1024);

        KeyPair kp = kpg.generateKeyPair();

        RSAPrivateKey privateKey = (RSAPrivateKey)kp.getPrivate();

        RSAPublicKey publicKey = (RSAPublicKey)kp.getPublic();
        Map keyMap = new HashMap();
        keyMap.put("private_key", privateKey.getEncoded());
        keyMap.put("public_key", publicKey.getEncoded());
        return keyMap;
    }
}
