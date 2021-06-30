package com.bluelinelabs.conductor.lint

import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.client.api.IssueRegistry as LintIssueRegistry

@Suppress("UnstableApiUsage", "unused")
class IssueRegistry : LintIssueRegistry() {

  override val issues = listOf(
    ControllerIssueDetector.ISSUE,
    ControllerChangeHandlerIssueDetector.ISSUE
  )

  override val api: Int = CURRENT_API

  private val githubIssueLink = "https://github.com/bluelinelabs/Conductor/issues/new"

  override val vendor = Vendor(
    vendorName = "Conductor",
    feedbackUrl = githubIssueLink,
    contact = githubIssueLink
  )
}