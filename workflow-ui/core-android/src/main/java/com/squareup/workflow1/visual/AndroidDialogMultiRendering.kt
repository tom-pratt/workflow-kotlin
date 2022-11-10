package com.squareup.workflow1.visual

import android.app.Dialog
import android.content.Context
import com.squareup.workflow1.ui.Compatible
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.container.CoveredByModal
import com.squareup.workflow1.ui.container.dispatchCancelEvent

/**
 * Replaces DialogSession I think?
 */
@OptIn(WorkflowUiExperimentalApi::class)
public class AndroidDialogMultiRendering : MultiRendering<Context, BoundedDialog<Dialog>>() {
  // Note similar code in LayeredDialogSessions
  private var allowEvents = true
    set(value) {
      val was = field
      field = value
      visualOrNull?.dialog?.window?.takeIf { value != was }?.let { window ->
        // https://stackoverflow.com/questions/2886407/dealing-with-rapid-tapping-on-buttons
        // If any motion events were enqueued on the main thread, cancel them.
        dispatchCancelEvent { window.superDispatchTouchEvent(it) }
        // When we cancel, have to warn things like RecyclerView that handle streams
        // of motion events and eventually dispatch input events (click, key pressed, etc.)
        // based on them.
        window.peekDecorView()?.cancelPendingInputEvents()
      }
    }

  override fun create(
    rendering: Any,
    context: Context,
    environment: VisualEnvironment
  ): VisualHolder<Any, BoundedDialog<Dialog>> {
    // TODO probably want to pull implementation from environment the way
    //  AndroidViewMultiRendering does.

    // TODO withName? withEnvironment? Just leave those for views like today?

    val rawHolder = requireNotNull(
      environment[AndroidDialogFactoryKey].createOrNull(rendering, context, environment)
    ) {
      "A VisualFactory must be registered to create an Android Dialog for $rendering, " +
        "or it must implement AndroidOverlay."
    }

    val wrapper = VisualHolder<Any, BoundedDialog<Dialog>>(rawHolder.visual) { newRendering ->
      // TODO This won't actually work until this lambda is getting a new env on every update

      allowEvents = !environment[CoveredByModal]
      rawHolder.update(newRendering)
    }

    // TODO I've dropped the index: Int from DialogSession. It's so nasty should
    //   delete it in real life too. Instead be consistent with BackStackContainer
    //   and rely exclusively on Compatible. Means we must address NamedOverlay
    //   or something.
    val savedStateRegistryKey = Compatible.keyFor(rendering)

  }
}
