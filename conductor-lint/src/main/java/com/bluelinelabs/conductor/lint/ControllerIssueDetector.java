package com.bluelinelabs.conductor.lint;

import com.android.SdkConstants;
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
import com.intellij.psi.PsiParameter;

import java.util.Collections;
import java.util.List;

public final class ControllerIssueDetector extends Detector implements Detector.JavaPsiScanner {

    public static final Issue ISSUE =
            Issue.create("ValidController", "Controller not instantiatable",
                    "Non-abstract Controller instances must have a default or single-argument constructor"
                            + " that takes a Bundle in order for the system to re-create them in the"
                            + " case of the process being killed.", Category.CORRECTNESS, 6, Severity.FATAL,
                    new Implementation(ControllerIssueDetector.class, Scope.JAVA_FILE_SCOPE));

    public ControllerIssueDetector() { }

    @Override
    public List<String> applicableSuperClasses() {
        return Collections.singletonList("com.bluelinelabs.conductor.Controller");
    }

    @Override
    public void checkClass(JavaContext context, PsiClass declaration) {
        final JavaEvaluator evaluator = context.getEvaluator();
        if (evaluator.isAbstract(declaration)) {
            return;
        }

        if (!evaluator.isPublic(declaration)) {
            String message = String.format("This Controller class should be public (%1$s)", declaration.getQualifiedName());
            context.report(ISSUE, declaration, context.getLocation(declaration), message);
            return;
        }

        if (declaration.getContainingClass() != null && !evaluator.isStatic(declaration)) {
            String message = String.format("This Controller inner class should be static (%1$s)", declaration.getQualifiedName());
            context.report(ISSUE, declaration, context.getLocation(declaration), message);
            return;
        }


        boolean hasDefaultConstructor = false;
        boolean hasBundleConstructor = false;
        PsiMethod[] constructors = declaration.getConstructors();
        for (PsiMethod constructor : constructors) {
            if (evaluator.isPublic(constructor)) {
                PsiParameter[] parameters = constructor.getParameterList().getParameters();

                if (parameters.length == 0) {
                    hasDefaultConstructor = true;
                    break;
                } else if (parameters.length == 1 &&
                        parameters[0].getType().equalsToText(SdkConstants.CLASS_BUNDLE) ||
                        parameters[0].getType().equalsToText("Bundle")) {
                    hasBundleConstructor = true;
                    break;
                }
            }
        }

        if (constructors.length > 0 && !hasDefaultConstructor && !hasBundleConstructor) {
            String message = String.format(
                    "This Controller needs to have either a public default constructor or a" +
                            " public single-argument constructor that takes a Bundle. (`%1$s`)",
                    declaration.getQualifiedName());
            context.report(ISSUE, declaration, context.getLocation(declaration), message);
        }
    }
}
