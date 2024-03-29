package com.bluelinelabs.conductor.lint;

import static com.android.tools.lint.checks.infrastructure.TestFiles.java;
import static com.android.tools.lint.checks.infrastructure.TestLintTask.lint;

import com.android.tools.lint.checks.infrastructure.TestFile;

import org.intellij.lang.annotations.Language;
import org.junit.Test;

public class ControllerDetectorTest {

    private static final String CLASS_ERROR =
            "src/test/SampleController.java:2: Error: This Controller class should be public (test.SampleController) [ValidController]\n"
                    + "private class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                    + "              ~~~~~~~~~~~~~~~~\n"
                    + "1 errors, 0 warnings\n";

    private final TestFile controllerStub = java(
            "package com.bluelinelabs.conductor;\n"
                    + "abstract class Controller {}"
    );


    @Test
    public void testWithNoConstructor() {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                + "}";

        lint()
                .files(controllerStub, java(source))
                .issues(ControllerIssueDetector.ISSUE, ControllerChangeHandlerIssueDetector.ISSUE)
                .run()
                .expectClean();
    }

    @Test
    public void testWithEmptyConstructor() {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                + "    public SampleController() { }\n"
                + "}";

        lint()
                .files(controllerStub, java(source))
                .issues(ControllerIssueDetector.ISSUE, ControllerChangeHandlerIssueDetector.ISSUE)
                .run()
                .expectClean();
    }

    @Test
    public void testWithInvalidConstructor() {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                + "    public SampleController(int number) { }\n"
                + "}";

        lint()
                .files(controllerStub, java(source))
                .issues(ControllerIssueDetector.ISSUE, ControllerChangeHandlerIssueDetector.ISSUE)
                .run()
                .expect(""
                        + "src/test/SampleController.java:3: Error: This Controller needs to have either a public default constructor or a public single-argument constructor that takes a Bundle. (test.SampleController) [ValidController]\n"
                        + "    public SampleController(int number) { }\n"
                        + "           ~~~~~~~~~~~~~~~~\n"
                        + "1 errors, 0 warnings\n"
                );
    }

    @Test
    public void testWithEmptyAndInvalidConstructor() {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                + "    public SampleController() { }\n"
                + "    public SampleController(int number) { }\n"
                + "}";

        lint()
                .files(controllerStub, java(source))
                .issues(ControllerIssueDetector.ISSUE, ControllerChangeHandlerIssueDetector.ISSUE)
                .run()
                .expectClean();
    }

    @Test
    public void testWithBaseClassAndPrivateConstructor() {
        @Language("JAVA")
        String baseClass = ""
                + "package test;\n"
                + "public class BaseController extends com.bluelinelabs.conductor.Controller {}";

        @Language("JAVA")
        String source = ""
                + "package test;\n"
                + "public class SampleController extends BaseController {\n"
                + "    private SampleController() { }\n"
                + "}";

        lint()
                .files(controllerStub, java(baseClass), java(source))
                .issues(ControllerIssueDetector.ISSUE, ControllerChangeHandlerIssueDetector.ISSUE)
                .run()
                .expect(""
                        + "src/test/SampleController.java:3: Error: This Controller needs to have either a public default constructor or a public single-argument constructor that takes a Bundle. (test.SampleController) [ValidController]\n"
                        + "    private SampleController() { }\n"
                        + "            ~~~~~~~~~~~~~~~~\n"
                        + "1 errors, 0 warnings"
                );
    }

    @Test
    public void testWithPrivateConstructor() {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "public class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                + "    private SampleController() { }\n"
                + "}";

        lint()
                .files(controllerStub, java(source))
                .issues(ControllerIssueDetector.ISSUE, ControllerChangeHandlerIssueDetector.ISSUE)
                .run()
                .expect(""
                        + "src/test/SampleController.java:3: Error: This Controller needs to have either a public default constructor or a public single-argument constructor that takes a Bundle. (test.SampleController) [ValidController]\n"
                        + "    private SampleController() { }\n"
                        + "            ~~~~~~~~~~~~~~~~\n"
                        + "1 errors, 0 warnings\n"
                );
    }

    @Test
    public void testWithPrivateClass() {
        @Language("JAVA") String source = ""
                + "package test;\n"
                + "private class SampleController extends com.bluelinelabs.conductor.Controller {\n"
                + "    public SampleController() { }\n"
                + "}";

        lint()
                .files(controllerStub, java(source))
                .issues(ControllerIssueDetector.ISSUE, ControllerChangeHandlerIssueDetector.ISSUE)
                .run()
                .expect(CLASS_ERROR);
    }

}
