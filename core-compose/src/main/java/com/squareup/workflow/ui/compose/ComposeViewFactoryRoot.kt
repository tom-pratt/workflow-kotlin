/*
 * Copyright 2020 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.squareup.workflow.ui.compose

import androidx.compose.Composable
import androidx.compose.Direct
import com.squareup.workflow.ui.ViewEnvironment
import com.squareup.workflow.ui.ViewEnvironmentKey

/**
 * A `@Composable` function that is stored in a [ViewEnvironment] and will be used to wrap the first
 * [bindCompose] composition. This can be used to setup any ambients that all [bindCompose]
 * factories need access to, such as ambients that specify the UI theme.
 *
 * This function will called once, to wrap the _highest-level_ [bindCompose] in the tree. However,
 * ambients are propagated down to child [bindCompose] compositions, so any ambients provided here
 * will be available in _all_ [bindCompose] compositions.
 */
interface ComposeViewFactoryRoot {

  @Composable fun wrap(content: @Composable() () -> Unit)

  companion object : ViewEnvironmentKey<ComposeViewFactoryRoot>(ComposeViewFactoryRoot::class) {
    override val default: ComposeViewFactoryRoot get() = NoopComposeViewFactoryRoot
  }
}

/**
 * Adds a [ComposeViewFactoryRoot] to this [ViewEnvironment] that uses [wrapper] to wrap the first
 * [bindCompose] composition. See [ComposeViewFactoryRoot] for more information.
 */
fun ViewEnvironment.withComposeViewFactoryRoot(
  wrapper: @Composable() (content: @Composable() () -> Unit) -> Unit
): ViewEnvironment = this + (ComposeViewFactoryRoot to ComposeViewFactoryRoot(wrapper))

// This could be inline, but that makes the Compose compiler puke.
@Suppress("FunctionName")
fun ComposeViewFactoryRoot(
  wrapper: @Composable() (content: @Composable() () -> Unit) -> Unit
): ComposeViewFactoryRoot = object : ComposeViewFactoryRoot {
  @Composable override fun wrap(content: @Composable() () -> Unit) = wrapper(content)
}

private object NoopComposeViewFactoryRoot : ComposeViewFactoryRoot {
  @Direct @Composable override fun wrap(content: @Composable() () -> Unit) {
    content()
  }
}