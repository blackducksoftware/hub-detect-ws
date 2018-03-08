package com.blackducksoftware.integration.hub.detectws.controller;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.blackducksoftware.integration.exception.IntegrationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppConfig.class })
@TestPropertySource(properties = { "current.linux.distro=ubuntu", })
public class ImageInspectorActionWrongOsTest {

    @Autowired
    private DetectServiceAction imageInspectorAction;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    // Testing must happen in containers
    // On correct OS, will try to overwrite files under /lib
    @Ignore
    @Test
    public void test() throws IntegrationException, IOException, InterruptedException {

    }

}
