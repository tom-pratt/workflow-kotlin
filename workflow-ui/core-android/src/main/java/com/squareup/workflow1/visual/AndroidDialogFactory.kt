package com.squareup.workflow1.visual

import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.container.AndroidOverlay
import com.squareup.workflow1.ui.container.Overlay
import com.squareup.workflow1.ui.container.OverlayDialogFactory
import com.squareup.workflow1.ui.container.OverlayDialogFactoryFinder
import com.squareup.workflow1.ui.container.OverlayDialogHolder
import com.squareup.workflow1.ui.container.show

@WorkflowUiExperimentalApi
public typealias AndroidDialogFactory<R> = VisualFactory<Context, R, BoundedDialog<Dialog>>

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
      ): VisualHolder<Any, BoundedDialog<Dialog>>? {

        // TODO find and convert the actual OverlayDialogFactoryFinder instead,
        //  in case it's been customized.

        return (rendering as? AndroidOverlay<*>)?.let { overlay ->
          @Suppress("UNCHECKED_CAST")
          val oldHolder = (overlay.dialogFactory as OverlayDialogFactory<Overlay>)
            .buildDialog(overlay, environment, context)

          val glued = BoundedOverlayDialogHolder(oldHolder)

          VisualHolder(glued) {
            glued.oldHolder.runner.invoke(it as Overlay, environment)
          }
        }
          ?: (rendering as? Overlay)?.let { overlay ->
            val oldFactory = environment[OverlayDialogFactoryFinder].getDialogFactoryForRendering(
              environment, overlay
            )

            oldFactory.buildDialog(overlay, environment, context).let { oldHolder ->
              val glued = BoundedOverlayDialogHolder(oldHolder)

              @Suppress("UNCHECKED_CAST")
              VisualHolder<Overlay, BoundedDialog<Dialog>>(glued) {
                glued.oldHolder.show(it, environment)
              } as VisualHolder<Any, BoundedDialog<Dialog>>
            }
          }
      }
    }
}

@WorkflowUiExperimentalApi
private class BoundedOverlayDialogHolder<O : Overlay>(
  val oldHolder: OverlayDialogHolder<O>
) : BoundedDialog<Dialog> {
  init {
    require(oldHolder.onBackPressed == null) {
      "We can't roll this out for real until we have forced the migration to ComponentDialog and " +
        "OnBackPressedDispatcher, and therefore eliminated this onBackPressed call. Or else " +
        "we just punt on backward compatibility."
    }
  }

  override val dialog: Dialog get() = oldHolder.dialog

  override fun onUpdateBounds(bounds: Rect) {
    oldHolder.onUpdateBounds?.invoke(bounds)
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
