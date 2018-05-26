package com.bluelinelabs.conductor.lint;

import com.android.SdkConstants;
import com.android.tools.lint.client.api.JavaEvaluator;
import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;

import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;

import java.util.Collections;
import java.util.List;

public final class ControllerIssueDetector extends Detector implements Detector.UastScanner {

    public static final Issue ISSUE =
            Issue.create("ValidController", "Controller not instantiatable",
                    "Non-abstract Controller instances must have a default or single-argument constructor"
                            + " that takes a Bundle in order for the system to re-create them in the"
                            + " case of the process being killed.", Category.CORRECTNESS, 6, Severity.FATAL,
                    new Implementation(ControllerIssueDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override
    public List<String> applicableSuperClasses() {
        return Collections.singletonList("com.bluelinelabs.conductor.Controller");
    }

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.<Class<? extends UElement>>singletonList(UClass.class);
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {
        return new UElementHandler() {
            @Override
            public void visitClass(UClass node) {
                final JavaEvaluator evaluator = context.getEvaluator();
                if (evaluator.isAbstract(node)) {
                    return;
                }

                if (!evaluator.isPublic(node)) {
                    String message = String.format("This Controller class should be public (%1$s)", node.getQualifiedName());
                    context.report(ISSUE, node, context.getLocation((UElement) node), message);
                    return;
                }

                if (node.getContainingClass() != null && !evaluator.isStatic(node)) {
                    String message = String.format("This Controller inner class should be static (%1$s)", node.getQualifiedName());
                    context.report(ISSUE, node, context.getLocation((UElement) node), message);
                    return;
                }


                boolean hasDefaultConstructor = false;
                boolean hasBundleConstructor = false;
                PsiMethod[] constructors = node.getConstructors();
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
                            node.getQualifiedName());
                    context.report(ISSUE, node, context.getLocation((UElement) node), message);
                }
            }
        };
    }
}
