package com.rauio.smartdangjian.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

@AnalyzeClasses(packages = "com.rauio.smartdangjian", importOptions = ImportOption.DoNotIncludeTests.class)
public class ModuleDependencyRulesTest {

    @ArchTest
    static final ArchRule no_cycles_between_server_modules = SlicesRuleDefinition.slices()
            .matching("com.rauio.smartdangjian.server.(*)..")
            .should()
            .beFreeOfCycles();
}
