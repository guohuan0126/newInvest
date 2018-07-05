package com.jiuyi.ndr;

import org.jasypt.encryption.StringEncryptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by zhangyibo on 2017/6/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class StringEncryptTest {

    @Autowired
    private StringEncryptor stringEncryptor;

    @Test
    public void encryptPassword(){
        System.out.println(stringEncryptor.encrypt("Dev_D@t@RW_2O17"));
    }

}
