package group.bison.data.data_layer.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by diaobisong on 2020/7/5.
 */
public class MD5Util {
    private static final Logger log = LoggerFactory.getLogger(MD5Util.class);

    public MD5Util() {
    }

    public static String md5(String str) {
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException var5) {
            log.error("md5加密失败", var5);
        }

        byte[] bs = md.digest(str.getBytes());
        BASE64Encoder base = new BASE64Encoder();
        String digitFingerprint = base.encode(bs);
        return digitFingerprint;
    }
}

