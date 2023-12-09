package org.nektototam.easyblog.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.nektototam.easyblog.IntegrationTest;
import org.springframework.test.context.web.WebAppConfiguration;

@CucumberContextConfiguration
@IntegrationTest
@WebAppConfiguration
public class CucumberTestContextConfiguration {}
