package com.bluelinelabs.conductor.lint;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;

import org.intellij.lang.annotations.Language;

import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class ControllerDetectorTest extends LintDetectorTest {

    private static final String NO_WARNINGS = "No warnings.";
    private static final String CONSTRUCTOR_ERROR =
            "src/test/SampleController.java:2: Error: This Controller needs to have either a public default constructor or a public single-argument constructor that takes a Bundle. (test.SampleController) [ValidController]\n"
            + "public class SampleController extends com.bluelinelabs.conductor.Controller {\n"
            + "^\n"
            + "1 errors, 0 warnings\n";
    private static final String CLASS_ERROR =
            "src/test/SampleController.java:2: Error: This Controller class should be public (test.SampleController) [ValidController]\n"
                    + "private class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                    + "^\n"
                    + "1 errors, 0 warnings\n";

    public void testWithNoConstructor() throws Exception {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                + "}";
        assertThat(lintProject(java(source))).isEqualTo(NO_WARNINGS);
    }

    public void testWithEmptyConstructor() throws Exception {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                + "    public SampleController() { }\n"
                + "}";
        assertThat(lintProject(java(source))).isEqualTo(NO_WARNINGS);
    }

    public void testWithInvalidConstructor() throws Exception {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                + "    public SampleController(int number) { }\n"
                + "}";
        assertThat(lintProject(java(source))).isEqualTo(CONSTRUCTOR_ERROR);
    }

    public void testWithEmptyAndInvalidConstructor() throws Exception {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                + "    public SampleController() { }\n"
                + "    public SampleController(int number) { }\n"
                + "}";
        assertThat(lintProject(java(source))).isEqualTo(NO_WARNINGS);
    }

    public void testWithPrivateConstructor() throws Exception {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                + "    private SampleController() { }\n"
                + "}";
        assertThat(lintProject(java(source))).isEqualTo(CONSTRUCTOR_ERROR);
    }

    public void testWithPrivateClass() throws Exception {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "private class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                + "    public SampleController() { }\n"
                + "}";
        assertThat(lintProject(java(source))).isEqualTo(CLASS_ERROR);
    }

    @Override
    protected Detector getDetector() {
        return new ControllerIssueDetector();
    }

    @Override
    protected List<Issue> getIssues() {
        return Collections.singletonList(ControllerIssueDetector.ISSUE);
    }

    @Override
    protected boolean allowCompilationErrors() {
        return true;
    }
}
