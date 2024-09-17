/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.overlay

import com.slack.circuit.overlay.OverlayHost
import com.slack.circuit.runtime.internal.StableCoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

public class OverlayExceptionHandler(
  private val scope: StableCoroutineScope,
  private val host: OverlayHost,
) : CoroutineExceptionHandler {
  override fun handleException(context: CoroutineContext, exception: Throwable) {
    scope.launch {
      host.show(ExceptionOverlay(exception))
    }
  }

  override val key: CoroutineContext.Key<*> get() = Key

  public companion object Key : CoroutineContext.Key<OverlayExceptionHandler>
}
