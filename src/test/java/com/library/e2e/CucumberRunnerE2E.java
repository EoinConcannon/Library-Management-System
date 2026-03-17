package com.library.e2e;

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty")
@ConfigurationParameter(key = "cucumber.glue", value = "com.library.e2e.steps")
public class CucumberRunnerE2E {
}