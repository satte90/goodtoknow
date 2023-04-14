package com.teliacompany.tiberius.base.mongodb.config;

import com.teliacompany.tiberius.base.mongodb.controller.MongodbDevOpsController;
import com.teliacompany.tiberius.base.mongodb.service.MongodbDevOpsService;
import com.teliacompany.tiberius.base.mongodb.testsupport.MongodbTestSupportController;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackageClasses = {MongodbDevOpsService.class, MongodbDevOpsController.class, MongodbTestSupportController.class})
@Import(MongodbDevopsConfig.class)
public class TiberiusBaseMongodbAutoConfiguration {

}
