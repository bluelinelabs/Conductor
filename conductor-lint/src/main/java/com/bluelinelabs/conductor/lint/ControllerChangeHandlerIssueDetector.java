package com.bluelinelabs.conductor.lint;

import com.android.tools.lint.client.api.JavaEvaluator;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.Collections;
import java.util.List;

public final class ControllerChangeHandlerIssueDetector extends Detector implements Detector.JavaPsiScanner {

    public static final Issue ISSUE =
            Issue.create("ValidControllerChangeHandler", "ControllerChangeHandler not instantiatable",
                    "Non-abstract ControllerChangeHandler instances must have a default constructor for the"
                            + " system to re-create them in the case of the process being killed.",
                    Category.CORRECTNESS, 6, Severity.FATAL,
                    new Implementation(ControllerChangeHandlerIssueDetector.class, Scope.JAVA_FILE_SCOPE));

    public ControllerChangeHandlerIssueDetector() { }

    @Override
    public List<String> applicableSuperClasses() {
        return Collections.singletonList("com.bluelinelabs.conductor.ControllerChangeHandler");
    }

    @Override
    public void checkClass(JavaContext context, PsiClass declaration) {
        final JavaEvaluator evaluator = context.getEvaluator();
        if (evaluator.isAbstract(declaration)) {
            return;
        }

        if (!evaluator.isPublic(declaration)) {
            String message = String.format("This ControllerChangeHandler class should be public (%1$s)", declaration.getQualifiedName());
            context.report(ISSUE, declaration, context.getLocation(declaration), message);
            return;
        }

        if (declaration.getContainingClass() != null && !evaluator.isStatic(declaration)) {
            String message = String.format("This ControllerChangeHandler inner class should be static (%1$s)", declaration.getQualifiedName());
            context.report(ISSUE, declaration, context.getLocation(declaration), message);
            return;
        }

        boolean hasDefaultConstructor = false;
        PsiMethod[] constructors = declaration.getConstructors();
        for (PsiMethod constructor : constructors) {
            if (evaluator.isPublic(constructor)) {
                if (constructor.getParameterList().getParametersCount() == 0) {
                    hasDefaultConstructor = true;
                    break;
                }
            }
        }

        if (constructors.length > 0 && !hasDefaultConstructor) {
            String message = String.format(
                    "This ControllerChangeHandler needs to have a public default constructor (`%1$s`)", declaration.getQualifiedName());
            context.report(ISSUE, declaration, context.getLocation(declaration), message);
        }
    }
}
