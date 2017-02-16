package com.bluelinelabs.conductor.lint;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;

import org.intellij.lang.annotations.Language;

import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class ControllerChangeHandlerDetectorTest extends LintDetectorTest {

    private static final String NO_WARNINGS = "No warnings.";
    private static final String CONSTRUCTOR =
            "src/test/SampleHandler.java:2: Error: This ControllerChangeHandler needs to have a public default constructor (test.SampleHandler) [ValidControllerChangeHandler]\n"
            + "public class SampleHandler extends com.bluelinelabs.conductor.ControllerChangeHandler {\n"
            + "^\n"
            + "1 errors, 0 warnings\n";
    private static final String PRIVATE_CLASS_ERROR =
            "src/test/SampleHandler.java:2: Error: This ControllerChangeHandler class should be public (test.SampleHandler) [ValidControllerChangeHandler]\n"
                    + "private class SampleHandler extends com.bluelinelabs.conductor.ControllerChangeHandler {\n"
                    + "^\n"
                    + "1 errors, 0 warnings\n";

    public void testWithNoConstructor() throws Exception {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleHandler extends com.bluelinelabs.conductor.ControllerChangeHandler {\n"
                + "}";
        assertThat(lintProject(java(source))).isEqualTo(NO_WARNINGS);
    }

    public void testWithEmptyConstructor() throws Exception {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleHandler extends com.bluelinelabs.conductor.ControllerChangeHandler {\n"
                + "    public SampleHandler() { }\n"
                + "}";
        assertThat(lintProject(java(source))).isEqualTo(NO_WARNINGS);
    }

    public void testWithInvalidConstructor() throws Exception {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleHandler extends com.bluelinelabs.conductor.ControllerChangeHandler {\n"
                + "    public SampleHandler(int number) { }\n"
                + "}";
        assertThat(lintProject(java(source))).isEqualTo(CONSTRUCTOR);
    }

    public void testWithEmptyAndInvalidConstructor() throws Exception {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleHandler extends com.bluelinelabs.conductor.ControllerChangeHandler {\n"
                + "    public SampleHandler() { }\n"
                + "    public SampleHandler(int number) { }\n"
                + "}";
        assertThat(lintProject(java(source))).isEqualTo(NO_WARNINGS);
    }

    public void testWithPrivateConstructor() throws Exception {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleHandler extends com.bluelinelabs.conductor.ControllerChangeHandler {\n"
                + "    private SampleHandler() { }\n"
                + "}";
        assertThat(lintProject(java(source))).isEqualTo(CONSTRUCTOR);
    }

    public void testWithPrivateClass() throws Exception {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "private class SampleHandler extends com.bluelinelabs.conductor.ControllerChangeHandler {\n"
                + "    public SampleHandler() { }\n"
                + "}";
        assertThat(lintProject(java(source))).isEqualTo(PRIVATE_CLASS_ERROR);
    }

    @Override
    protected Detector getDetector() {
        return new ControllerChangeHandlerIssueDetector();
    }

    @Override
    protected List<Issue> getIssues() {
        return Collections.singletonList(ControllerChangeHandlerIssueDetector.ISSUE);
    }

    @Override
    protected boolean allowCompilationErrors() {
        return true;
    }
}
