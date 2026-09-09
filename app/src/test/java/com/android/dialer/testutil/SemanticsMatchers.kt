package com.android.dialer.testutil

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher

internal fun hasClickLabel(label: String): SemanticsMatcher {
    return SemanticsMatcher(description = "click label is $label") { node ->
        node.config.getOrNull(SemanticsActions.OnClick)?.label == label
    }
}

internal fun hasCustomActionCount(count: Int): SemanticsMatcher {
    return SemanticsMatcher(description = "custom action count is $count") { node ->
        node.config.getOrNull(SemanticsActions.CustomActions).orEmpty().size == count
    }
}

internal fun hasCustomAction(label: String): SemanticsMatcher {
    return SemanticsMatcher(description = "has a custom action labelled $label") { node ->
        node.config.getOrNull(SemanticsActions.CustomActions).orEmpty().any { it.label == label }
    }
}

internal fun hasNoText(): SemanticsMatcher {
    return SemanticsMatcher(description = "has no text") { node ->
        node.config.getOrNull(SemanticsProperties.Text).isNullOrEmpty()
    }
}
