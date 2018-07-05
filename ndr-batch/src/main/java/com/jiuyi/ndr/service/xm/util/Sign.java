package com.jiuyi.ndr.service.xm.util;

import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 存管通 签名/验签
 */
@Component
public class Sign {

	private static Logger log = LoggerFactory.getLogger(Sign.class);

	private final static String ENCODING = "utf-8";

	private static String privateKey;
	private static String publicKey;
	@Value("${xm.enviroment.privateKey}")
	private void setPrivateKey(String privateKey) {
		Sign.privateKey = privateKey;
	}
	@Value("${xm.enviroment.publicKey}")
	public static void setPublicKey(String publicKey) {
		Sign.publicKey = publicKey;
	}

	/**
	 * 签名
	 *
	 * @param reqData 请求报文
	 * @return
	 */
	public synchronized static String sign(String reqData) throws Exception {
		try {
			return new String(Base64.encode(SHA1withRSA.sign(reqData.getBytes(ENCODING), Base64.decode(privateKey))));
		} catch (Exception e) {
			log.error("存管通签名错误 \n {}", e);
			throw new Exception("存管通签名错误");
		}
	}

	/**
	 * 验签
	 *
	 * @param sign
	 * @param data
	 * @return
	 */
	public synchronized static boolean verify(String sign, String data) throws Exception {
		try {
			return SHA1withRSA.verify(Base64.decode(sign), data.getBytes(ENCODING), Base64.decode(publicKey));
		} catch (Exception e) {
			log.error("存管通验签错误 \n {}", e);
			throw new Exception("存管通验签错误");
		}
	}


}