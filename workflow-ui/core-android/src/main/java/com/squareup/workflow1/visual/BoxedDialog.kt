package com.squareup.workflow1.visual

import android.app.Dialog
import android.graphics.Rect
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.container.setBounds
import com.squareup.workflow1.ui.onBackPressedDispatcherOwnerOrNull

/**
 * Pairs an Android [dialog] with a function that can be called to report the bounds
 * to which it should restrict itself.
 */
@WorkflowUiExperimentalApi
public interface BoxedDialog<D: Dialog> {
  public val dialog: D

  public fun onUpdateBounds(bounds: Rect) {
    dialog.setBounds(bounds)
  }
}
