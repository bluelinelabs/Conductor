package com.bluelinelabs.conductor.lint;

import com.android.tools.lint.detector.api.Issue;

import java.util.Arrays;
import java.util.List;

import static com.android.tools.lint.detector.api.ApiKt.CURRENT_API;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public final class IssueRegistry extends com.android.tools.lint.client.api.IssueRegistry {

    @Override
    public List<Issue> getIssues() {
        return Arrays.asList(
                ControllerIssueDetector.ISSUE,
                ControllerChangeHandlerIssueDetector.ISSUE
        );
    }

    @Override
    public int getApi() {
        return CURRENT_API;
    }
}
