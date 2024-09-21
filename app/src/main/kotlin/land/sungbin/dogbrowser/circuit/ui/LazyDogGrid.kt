/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.ui

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.slack.circuit.retained.rememberRetained
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import land.sungbin.dogbrowser.circuit.R
import land.sungbin.dogbrowser.circuit.presenter.Dog
import land.sungbin.dogbrowser.circuit.screen.BrowseDogScreen

@Composable public fun LazyDogGrid(
  dogs: ImmutableList<Dog>,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  eventSink: (event: BrowseDogScreen.Event.RestrictedFavorites) -> Unit,
) {
  val context = LocalContext.current

  LaunchedEffect(dogs) {
    val imageLoader = context.imageLoader
    withContext(Dispatchers.IO) {
      dogs.fastForEach { dog ->
        val request = ImageRequest.Builder(context)
          .data(dog.image)
          .memoryCacheKey(dog.image)
          .diskCacheKey(dog.image)
          .build()

        launch {
          imageLoader.execute(request)
        }
      }
    }
  }

  if (dogs.isEmpty()) {
    DogsEmpty(modifier = modifier.wrapContentSize())
  } else {
    LazyVerticalStaggeredGrid(
      modifier = modifier,
      columns = StaggeredGridCells.Adaptive(minSize = 250.dp),
      contentPadding = contentPadding,
      verticalItemSpacing = 4.dp,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      items(items = dogs, key = Dog::image) { dog ->
        DogImage(
          modifier = modifier.fillMaxWidth(),
          dog = dog,
          eventSink = eventSink,
        )
      }
    }
  }
}

private val dogImageShape = RoundedCornerShape(size = 8.dp)

@Composable private fun DogImage(
  dog: Dog,
  modifier: Modifier = Modifier,
  eventSink: (event: BrowseDogScreen.Event.RestrictedFavorites) -> Unit,
) {
  val scope = rememberCoroutineScope()
  val isSystemInDarkTheme = isSystemInDarkTheme()

  var dogSwatch by rememberRetained { mutableStateOf<Palette.Swatch?>(null) }

  Box(
    modifier = modifier
      .clip(dogImageShape)
      .clickable { eventSink(BrowseDogScreen.Event.GoToViewer(dog)) },
  ) {
    // TODO Modifier.heightIn(max = 650.dp)
    AsyncImage(
      modifier = Modifier.fillMaxWidth(),
      model = dog.image,
      onSuccess = { (_, result) ->
        scope.launch(Dispatchers.IO) {
          Palette.from(
            result.drawable
              .toBitmap()
              .copy(Bitmap.Config.ARGB_8888, /* isMutable = */ false),
          ).generate { palette ->
            palette
              ?.let { (if (isSystemInDarkTheme) palette.darkVibrantSwatch else palette.lightVibrantSwatch) ?: palette.dominantSwatch }
              ?.let { swatch -> dogSwatch = swatch }
          }
        }
      },
      contentScale = ContentScale.Crop,
      contentDescription = dog.breed,
      filterQuality = FilterQuality.High,
    )
    Row(
      modifier = Modifier
        .padding(all = 4.dp)
        .align(Alignment.BottomStart)
        .matchParentSize()
        .wrapContentHeight(align = Alignment.Bottom),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (dog.breed != null) {
        Text(
          modifier = Modifier
            .padding(start = 4.dp)
            .widthIn(max = 150.dp)
            .background(
              color = dogSwatch?.rgb?.let(::Color) ?: MaterialTheme.colorScheme.primaryContainer,
              shape = dogImageShape,
            )
            .padding(vertical = 2.dp, horizontal = 6.dp),
          text = dog.breed,
          maxLines = 1,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = dogSwatch?.bodyTextColor?.let(::Color) ?: MaterialTheme.colorScheme.onPrimaryContainer,
          ),
          overflow = TextOverflow.Ellipsis,
        )
      }
      Spacer(Modifier.weight(1f))
      FilledTonalIconButton(
        colors = IconButtonDefaults.filledTonalIconButtonColors(
          containerColor = dogSwatch?.rgb?.let(::Color) ?: MaterialTheme.colorScheme.tertiaryContainer,
          contentColor = dogSwatch?.bodyTextColor?.let(::Color) ?: MaterialTheme.colorScheme.tertiary,
        ),
        onClick = {
          val event = when (dog.favorite) {
            true -> BrowseDogScreen.Event.RemoveFavorite(dog)
            else -> BrowseDogScreen.Event.AddFavorite(dog)
          }
          eventSink(event)
        },
      ) {
        Icon(
          painter = painterResource(
            when (dog.favorite) {
              true -> R.drawable.ic_round_favorite_24
              else -> R.drawable.ic_round_favorite_border_24
            },
          ),
          contentDescription = if (dog.favorite) "favorite" else "not favorite",
        )
      }
    }
  }
}

@Composable private fun DogsEmpty(modifier: Modifier = Modifier) {
  Text(
    modifier = modifier,
    text = "No dogs found.",
    style = MaterialTheme.typography.headlineLarge,
  )
}
