package com.poldroc.retry.test.service.impl;


import com.poldroc.retry.annotation.annotation.Retry;
import com.poldroc.retry.test.service.SpringRecoverService;
import com.poldroc.retry.test.support.recover.MyRecover;
import org.springframework.stereotype.Service;

@Service
public class SpringRecoverServiceImpl implements SpringRecoverService {

    @Override
    @Retry(recover = MyRecover.class)
    public String query(String email) {
        System.out.println("spring service query..." + email);
        throw new RuntimeException();
    }

}
