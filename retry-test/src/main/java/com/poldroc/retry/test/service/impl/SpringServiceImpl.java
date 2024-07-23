package com.poldroc.retry.test.service.impl;

import com.poldroc.retry.annotation.annotation.Retry;
import com.poldroc.retry.test.service.SpringService;
import org.springframework.stereotype.Service;

/**
 * @author Poldroc
 * @date 2024/7/22
 */

@Service
public class SpringServiceImpl implements SpringService {

    @Override
    @Retry
    public String query() {

        System.out.println("spring service query...");
        throw new RuntimeException();
    }

}
