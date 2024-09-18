/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.ui

import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.MarqueeDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

@OptIn(ExperimentalLayoutApi::class)
@Composable public fun LazyDogGrid(
  dogs: ImmutableList<Dog>,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  eventSink: (event: BrowseDogScreen.Event) -> Unit,
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

  LazyVerticalStaggeredGrid(
    modifier = modifier,
    columns = StaggeredGridCells.Adaptive(160.dp),
    contentPadding = contentPadding,
    verticalItemSpacing = 2.dp,
    horizontalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    items(items = dogs, key = Dog::image) { dog ->
      DogImage(
        modifier = modifier.wrapContentSize(),
        dog = dog,
        eventSink = eventSink,
      )
    }
  }
}

private val dogImageShape = RoundedCornerShape(size = 2.dp)

@Composable private fun DogImage(
  dog: Dog,
  modifier: Modifier = Modifier,
  eventSink: (event: BrowseDogScreen.Event) -> Unit,
) {
  val scope = rememberCoroutineScope()
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val isSystemInDarkTheme = isSystemInDarkTheme()

  var dogSwatch by rememberRetained { mutableStateOf<Palette.Swatch?>(null) }
  val dogFavoriteTint = rememberRetained { ColorProducer { dogSwatch?.rgb?.let(::Color) ?: tertiaryColor } }

  Box(
    modifier = modifier
      .clip(dogImageShape)
      .clickable { eventSink(BrowseDogScreen.Event.GoToViewer(dog)) },
  ) {
    AsyncImage(
      modifier = Modifier.matchParentSize(),
      model = dog.image,
      filterQuality = FilterQuality.High,
      onSuccess = { (_, result) ->
        scope.launch(Dispatchers.IO) {
          Palette.from(result.drawable.toBitmap()).generate { palette ->
            palette
              ?.let { if (isSystemInDarkTheme) palette.darkVibrantSwatch else palette.lightVibrantSwatch }
              ?.let { swatch -> dogSwatch = swatch }
          }
        }
      },
      contentDescription = dog.breed,
    )
    Row(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .matchParentSize()
        .wrapContentHeight(align = Alignment.Bottom),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (dog.breed != null) {
        Text(
          modifier = Modifier
            .background(
              color = dogSwatch?.rgb?.let(::Color) ?: MaterialTheme.colorScheme.primaryContainer,
              shape = dogImageShape,
            )
            .padding(all = 2.dp)
            .basicMarquee(
              animationMode = MarqueeAnimationMode.WhileFocused,
              initialDelayMillis = MarqueeDefaults.RepeatDelayMillis,
            ),
          text = dog.breed,
          maxLines = 1,
          style = LocalTextStyle.current.copy(
            color = dogSwatch?.titleTextColor?.let(::Color) ?: MaterialTheme.colorScheme.onPrimaryContainer,
          ),
        )
      }
      Spacer(Modifier.weight(1f))
      IconButton(
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
          tint = dogFavoriteTint,
          contentDescription = if (dog.favorite) "favorite" else "not favorite",
        )
      }
    }
  }
}
