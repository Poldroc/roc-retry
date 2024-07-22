package com.poldroc.retry.test.springboot;

import com.poldroc.retry.test.RocRetryApplication;
import com.poldroc.retry.test.service.SpringRecoverService;
import com.poldroc.retry.test.service.SpringService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RocRetryApplication.class)
public class RocRetryApplicationTest {

    @Autowired
    private SpringService springService;

    @Autowired
    private SpringRecoverService springRecoverService;

    @Test(expected = RuntimeException.class)
    public void queryTest() {
        springService.query();
    }

    @Test(expected = RuntimeException.class)
    public void queryRecoverTest() {
        String email = "poldroc";
        springRecoverService.query(email);
    }


}
