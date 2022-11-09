package com.squareup.workflow1.visual

import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.container.AndroidOverlay
import com.squareup.workflow1.ui.container.Overlay
import com.squareup.workflow1.ui.container.OverlayDialogFactory
import com.squareup.workflow1.ui.container.OverlayDialogFactoryFinder
import com.squareup.workflow1.ui.container.OverlayDialogHolder
import com.squareup.workflow1.ui.container.show

@WorkflowUiExperimentalApi
public typealias AndroidDialogFactory<R> = VisualFactory<Context, R, BoxedDialog<Dialog>>

/**
 * TODO detailed kdoc, see those on [AndroidViewFactoryKey] to get started
 */
@WorkflowUiExperimentalApi
public object AndroidDialogFactoryKey : VisualEnvironmentKey<AndroidDialogFactory<Any>>() {
  override val default: AndroidDialogFactory<Any>
    get() = object : AndroidDialogFactory<Any> {
      override fun createOrNull(
        rendering: Any,
        context: Context,
        environment: VisualEnvironment
      ): VisualHolder<Any, BoxedDialog<Dialog>>? {

        // TODO find and convert the actual OverlayDialogFactoryFinder instead,
        //  in case it's been customized. Or does that happen with the multi key?

        return (rendering as? AndroidOverlay<*>)?.let { overlay ->
          @Suppress("UNCHECKED_CAST")
          val oldHolder = (overlay.dialogFactory as OverlayDialogFactory<Overlay>)
            .buildDialog(overlay, environment, context)

          val glued = BoxedOverlayDialogHolder(oldHolder)

          VisualHolder(glued) {
            glued.oldHolder.runner.invoke(it as Overlay, environment)
          }
        }
          ?: (rendering as? Overlay)?.let { overlay ->
            val oldFactory = environment[OverlayDialogFactoryFinder].getDialogFactoryForRendering(
              environment, overlay
            )

            oldFactory.buildDialog(overlay, environment, context).let { oldHolder ->
              val glued = BoxedOverlayDialogHolder(oldHolder)

              @Suppress("UNCHECKED_CAST")
              VisualHolder<Overlay, BoxedDialog<Dialog>>(glued) {
                glued.oldHolder.show(it, environment)
              } as VisualHolder<Any, BoxedDialog<Dialog>>
            }
          }
      }
    }
}

@WorkflowUiExperimentalApi
private class BoxedOverlayDialogHolder<O : Overlay>(
  val oldHolder: OverlayDialogHolder<O>
) : BoxedDialog<Dialog> {
  init {
    oldHolder.onBackPressed?.let { onBackPressed ->
      val dialog = oldHolder.dialog
      val dispatcher: OnBackPressedDispatcher

      if (dialog is OnBackPressedDispatcherOwner) {
        dispatcher = dialog.onBackPressedDispatcher
      } else if (Build.VERSION.SDK_INT >= 33) {
        dispatcher = oldHolder.dialog.onBackInvokedDispatcher
      } else {
        error("")
      }
    }
  }

  override val dialog: Dialog get() = oldHolder.dialog

  override fun onUpdateBounds(bounds: Rect) {
    oldHolder.onUpdateBounds?.invoke(bounds)
  }

  override fun onBackPressed() {
    oldHolder.onBackPressed?.invoke() ?: oldHolder.dialog.onBackPressed()
  }
}

/**
 * Convenience to access any Android Dialog Holder's output as `androidDialog`.
 */
@WorkflowUiExperimentalApi
public val <DialogT : Dialog> VisualHolder<*, DialogT>.androidDialog: DialogT
  get() = visual
//
// // TODO: R should extend Screen I think. So Screen is enforced for leaf factories,
// //   but things like AndroidViewMultiRendering stay based on Any so that they
// //   can handle generic concerns like WithName() and WithEnvironment()
// @WorkflowUiExperimentalApi
// public fun <R, V : View> androidViewFactoryFromCode(
//   build: (context: Context, environment: VisualEnvironment) -> VisualHolder<R, V>
// ): VisualFactory<Context, R, V> = object : VisualFactory<Context, R, V> {
//   override fun createOrNull(
//     rendering: R,
//     context: Context,
//     environment: VisualEnvironment
//   ): VisualHolder<R, V> {
//     return build(context, environment)
//   }
// }
//
// @WorkflowUiExperimentalApi
// public fun <R, V : View> androidViewFactoryFromCode(
//   build: (rendering: R, context: Context, environment: VisualEnvironment) -> VisualHolder<R, V>
// ): VisualFactory<Context, R, V> = object : VisualFactory<Context, R, V> {
//   override fun createOrNull(
//     rendering: R,
//     context: Context,
//     environment: VisualEnvironment
//   ): VisualHolder<R, V> {
//     return build(rendering, context, environment)
//   }
// }
//
// @WorkflowUiExperimentalApi
// public inline fun <R, reified V : View> androidViewFactoryFromLayout(
//   @IdRes resId: Int,
//   crossinline constructor: (view: View, environment: VisualEnvironment) -> VisualHolder<R, V>
// ): VisualFactory<Context, R, V> = androidViewFactoryFromCode { c, e ->
//   val view = LayoutInflater.from(c).inflate(resId, null, false) as V
//   constructor(view, e)
// }
