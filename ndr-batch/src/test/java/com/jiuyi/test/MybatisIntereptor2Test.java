package com.jiuyi.test;

import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by zhangyibo on 2017/8/2.
 */
@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class MybatisIntereptor2Test {

    @Autowired
    private SubjectDao subjectDao;

    @Test
    public void testInsertSubject(){
        subjectDao.insert(new Subject());
    }


}
