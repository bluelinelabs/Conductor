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
import com.intellij.psi.PsiType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UParameter;

import java.util.Collections;
import java.util.List;

public final class ControllerIssueDetector extends Detector implements Detector.UastScanner {

    static final Issue ISSUE =
            Issue.create("ValidController", "Controller not instantiatable",
                    "Non-abstract Controller instances must have a default or single-argument constructor"
                            + " that takes a Bundle in order for the system to re-create them in the"
                            + " case of the process being killed.", Category.CORRECTNESS, 6, Severity.FATAL,
                    new Implementation(ControllerIssueDetector.class, Scope.JAVA_FILE_SCOPE));

    private static final String CLASS_NAME = "com.bluelinelabs.conductor.Controller";

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.singletonList(UClass.class);
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {
        final JavaEvaluator evaluator = context.getEvaluator();

        return new UElementHandler() {
            @Override
            public void visitClass(@NotNull UClass node) {
                if (evaluator.isAbstract(node)) {
                    return;
                }

                final boolean hasSuperType = evaluator.extendsClass(node.getJavaPsi(), CLASS_NAME, true);
                if (!hasSuperType) {
                    return;
                }

                if (!evaluator.isPublic(node)) {
                    String message = String.format("This Controller class should be public (%1$s)", node.getQualifiedName());
                    context.report(ISSUE, node, context.getLocation(Identify.byName(node)), message);
                    return;
                }

                if (node.getContainingClass() != null && !evaluator.isStatic(node)) {
                    String message = String.format("This Controller inner class should be static (%1$s)", node.getQualifiedName());
                    context.report(ISSUE, node, context.getLocation(Identify.byName(node)), message);
                    return;
                }

                UMethod constructor = null;
                boolean hasDefaultConstructor = false;
                boolean hasBundleConstructor = false;
                for (UMethod method : node.getMethods()) {
                    if (method.isConstructor()) {
                        constructor = method;
                        if (evaluator.isPublic(method)) {
                            List<UParameter> parameters = method.getUastParameters();
                            if (parameters.isEmpty()) {
                                hasDefaultConstructor = true;
                                break;
                            } else if (parameters.size() == 1) {
                                PsiType type = parameters.get(0).getType();
                                if (type.equalsToText(SdkConstants.CLASS_BUNDLE) || type.equalsToText("Bundle")) {
                                    hasBundleConstructor = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (constructor != null && !hasDefaultConstructor && !hasBundleConstructor) {
                    String message = String.format(
                            "This Controller needs to have either a public default constructor or a" +
                                    " public single-argument constructor that takes a Bundle. (`%1$s`)",
                            node.getQualifiedName());
                    context.report(ISSUE, node, context.getLocation(Identify.byName(constructor)), message);
                }
            }
        };
    }
}
