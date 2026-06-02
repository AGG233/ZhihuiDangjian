package com.rauio.smartdangjian.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

@AnalyzeClasses(packages = "com.rauio.smartdangjian", importOptions = ImportOption.DoNotIncludeTests.class)
public class ModuleDependencyRulesTest {

    @ArchTest
    static final ArchRule no_cycles_between_server_modules = SlicesRuleDefinition.slices()
            .matching("com.rauio.smartdangjian.server.(*)..")
            .should()
            .beFreeOfCycles();

    @ArchTest
    static final ArchRule social_service_should_not_depend_on_content_persistence = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("..server.social.service..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..server.content.mapper..", "..server.content.pojo.entity..");

    @ArchTest
    static final ArchRule search_should_not_depend_on_other_modules_persistence = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("..server.search..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "..server.content.mapper..",
                    "..server.content.pojo.entity..",
                    "..server.learning.mapper..",
                    "..server.learning.pojo.entity..",
                    "..server.quiz.mapper..",
                    "..server.quiz.pojo.entity..",
                    "..server.user.mapper..",
                    "..server.user.pojo.entity..");

    @ArchTest
    static final ArchRule graph_admin_controller_should_not_accept_raw_maps = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("..server.graph.controller.admin..")
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("java.util.Map");

    @ArchTest
    static final ArchRule resource_controllers_should_not_expose_resource_meta_entity = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("..server.resource.controller..")
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta");

    @ArchTest
    static final ArchRule controllers_should_not_depend_on_persistence_entities = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("..controller..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..pojo.entity..");
}
