package com.dev.msclientes;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the dependency rule of the hexagonal architecture:
 * presentation -> application -> domain <- infrastructure.
 * <p>
 * {@code domain.port.in} is the only part of the domain allowed to see {@code application}: the input
 * ports are typed with the commands and results (records) that live in {@code application.dto}.
 */
@AnalyzeClasses(packages = "com.dev.msclientes", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String[] PURE_DOMAIN = {
            "..domain.model..", "..domain.exception..", "..domain.event..", "..domain.port.out.."};

    @ArchTest
    static final ArchRule domain_does_not_depend_on_frameworks =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..", "jakarta.persistence..", "jakarta.validation..",
                            "com.fasterxml.jackson..", "tools.jackson..", "lombok..")
                    .because("the domain must be plain Java");

    @ArchTest
    static final ArchRule pure_domain_does_not_depend_on_outer_layers =
            noClasses().that().resideInAnyPackage(PURE_DOMAIN)
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..application..", "..infrastructure..", "..presentation..");

    @ArchTest
    static final ArchRule input_ports_do_not_depend_on_infrastructure_or_presentation =
            noClasses().that().resideInAPackage("..domain.port.in..")
                    .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..presentation..");

    @ArchTest
    static final ArchRule application_does_not_depend_on_infrastructure_or_presentation =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..presentation..");

    @ArchTest
    static final ArchRule application_only_uses_the_transactional_annotation_from_spring =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat(
                            resideInAPackage("org.springframework..")
                                    .and(not(resideInAPackage("org.springframework.transaction.annotation.."))))
                    .because("use cases are wired in infrastructure.config, only @Transactional is allowed");

    @ArchTest
    static final ArchRule presentation_does_not_depend_on_infrastructure =
            noClasses().that().resideInAPackage("..presentation..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule infrastructure_does_not_depend_on_presentation =
            noClasses().that().resideInAPackage("..infrastructure..")
                    .should().dependOnClassesThat().resideInAPackage("..presentation..");

    @ArchTest
    static final ArchRule ports_are_interfaces =
            classes().that().resideInAnyPackage("..domain.port.in..", "..domain.port.out..")
                    .should().beInterfaces();

    @ArchTest
    static final ArchRule jpa_entities_live_in_persistence =
            classes().that().areAnnotatedWith(Entity.class)
                    .should().resideInAPackage("..infrastructure.persistence..");

    @ArchTest
    static final ArchRule controllers_live_in_presentation =
            classes().that().areAnnotatedWith(RestController.class)
                    .should().resideInAPackage("..presentation.controller..");

    @ArchTest
    static final ArchRule controllers_depend_on_use_case_interfaces_not_implementations =
            noClasses().that().resideInAPackage("..presentation.controller..")
                    .should().dependOnClassesThat().resideInAPackage("..application.usecase..");
}
