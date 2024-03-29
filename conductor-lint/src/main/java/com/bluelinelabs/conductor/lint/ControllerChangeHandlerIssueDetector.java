package com.bluelinelabs.conductor.lint;

import com.android.tools.lint.client.api.JavaEvaluator;
import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;

import java.util.Collections;
import java.util.List;

public final class ControllerChangeHandlerIssueDetector extends Detector implements Detector.UastScanner {

    static final Issue ISSUE =
            Issue.create("ValidControllerChangeHandler", "ControllerChangeHandler not instantiatable",
                    "Non-abstract ControllerChangeHandler instances must have a default constructor for the"
                            + " system to re-create them in the case of the process being killed.",
                    Category.CORRECTNESS, 6, Severity.FATAL,
                    new Implementation(ControllerChangeHandlerIssueDetector.class, Scope.JAVA_FILE_SCOPE));

    private static final String CLASS_NAME = "com.bluelinelabs.conductor.ControllerChangeHandler";

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
                    String message = String.format("This ControllerChangeHandler class should be public (%1$s)", node.getQualifiedName());
                    context.report(ISSUE, node, context.getLocation(Identify.byName(node)), message);
                    return;
                }

                if (node.getContainingClass() != null && !evaluator.isStatic(node)) {
                    String message = String.format("This ControllerChangeHandler inner class should be static (%1$s)", node.getQualifiedName());
                    context.report(ISSUE, node, context.getLocation(Identify.byName(node)), message);
                    return;
                }

                UMethod constructor = null;
                boolean hasDefaultConstructor = false;
                for (UMethod method : node.getMethods()) {
                    if (method.isConstructor()) {
                        constructor = method;
                        if (evaluator.isPublic(method) && method.getUastParameters().isEmpty()) {
                            hasDefaultConstructor = true;
                            break;
                        }
                    }
                }

                if (constructor != null && !hasDefaultConstructor) {
                    String message = String.format(
                            "This ControllerChangeHandler needs to have a public default constructor (`%1$s`)", node.getQualifiedName());
                    context.report(ISSUE, node, context.getLocation(Identify.byName(constructor)), message);
                }
            }
        };
    }
}
