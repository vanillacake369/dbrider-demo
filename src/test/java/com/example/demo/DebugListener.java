package com.example.demo;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class DebugListener extends AbstractTestExecutionListener {
    @Override
    public void beforeTestMethod(TestContext testContext) {
        System.out.println("➡️ beforeTestMethod");
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        System.out.println("⬅️ afterTestMethod");
    }
}
