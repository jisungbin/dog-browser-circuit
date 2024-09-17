/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

@Suppress("WRONG_INVOCATION_KIND") // false-positive
public inline fun <T> runSuspendCatching(block: () -> T): Result<T> {
  contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
  return try {
    Result.success(block())
  } catch (rethrown: CancellationException) {
    throw rethrown
  } catch (exception: Throwable) {
    Result.failure(exception)
  }
}
