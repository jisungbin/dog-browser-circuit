/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import kotlinx.collections.immutable.ImmutableList
import land.sungbin.dogbrowser.circuit.R

@Immutable
public data class BottomNavigationTab(
  public val title: String,
  @DrawableRes public val icon: Int,
  public val color: Color,
) {
  public companion object {
    public val Browse: BottomNavigationTab = BottomNavigationTab(
      title = "Browses",
      icon = R.drawable.ic_round_space_dashboard_24,
      color = Color(0xFF3F51B5),
    )

    public val Favorites: BottomNavigationTab = BottomNavigationTab(
      title = "Favorites",
      icon = R.drawable.ic_round_favorite_24,
      color = Color(0xFFE91E63),
    )
  }
}

// https://www.sinasamaki.com/glassmorphic-bottom-navigation-in-jetpack-compose/
@Composable public fun GlassmorphicBottomNavigation(
  tabs: ImmutableList<BottomNavigationTab>,
  selectedTabIndex: Int,
  hazeState: HazeState,
  modifier: Modifier = Modifier,
  onTabSelected: (index: Int, tab: BottomNavigationTab) -> Unit,
) {
  val selectedTabIndex by rememberUpdatedState(selectedTabIndex)

  Box(
    modifier = Modifier
      .padding(vertical = 24.dp, horizontal = 64.dp)
      .fillMaxWidth()
      .height(64.dp)
      .clip(CircleShape)
      .hazeChild(hazeState)
      .border(
        width = Dp.Hairline,
        brush = Brush.verticalGradient(
          colors = listOf(
            Color.White.copy(alpha = .8f),
            Color.White.copy(alpha = .2f),
          ),
        ),
        shape = CircleShape,
      ),
  ) {
    val animatedSelectedTabIndex by animateFloatAsState(
      targetValue = selectedTabIndex.toFloat(),
      label = "AnimatedSelectedTabIndex",
      animationSpec = spring(
        stiffness = Spring.StiffnessLow,
        dampingRatio = Spring.DampingRatioLowBouncy,
      ),
    )
    val animatedSelectedTabColor by animateColorAsState(
      targetValue = tabs[selectedTabIndex].color,
      label = "AnimatedSelectedTabColor",
      animationSpec = spring(stiffness = Spring.StiffnessLow),
    )

    BottomNavigationTabs(
      tabs = tabs,
      selectedTab = selectedTabIndex,
      onTabSelected = onTabSelected,
    )

    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
        .blur(
          radius = 50.dp,
          edgeTreatment = BlurredEdgeTreatment.Unbounded,
        ),
    ) {
      val tabWidth = size.width / tabs.size
      drawCircle(
        color = animatedSelectedTabColor.copy(alpha = .6f),
        radius = size.height / 2,
        center = Offset(
          x = (tabWidth * animatedSelectedTabIndex) + tabWidth / 2,
          y = size.height / 2,
        ),
      )
    }
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape),
    ) {
      val path = Path().apply { addRoundRect(RoundRect(size.toRect(), CornerRadius(size.height))) }
      val length = PathMeasure().apply { setPath(path, forceClosed = false) }.length
      val tabWidth = size.width / tabs.size

      drawPath(
        path = path,
        brush = Brush.horizontalGradient(
          colors = listOf(
            animatedSelectedTabColor.copy(alpha = 0f),
            animatedSelectedTabColor.copy(alpha = 1f),
            animatedSelectedTabColor.copy(alpha = 1f),
            animatedSelectedTabColor.copy(alpha = 0f),
          ),
          startX = tabWidth * animatedSelectedTabIndex,
          endX = tabWidth * (animatedSelectedTabIndex + 1),
        ),
        style = Stroke(
          width = 6f,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(length / 2, length)),
        ),
      )
    }
  }
}

@Composable private fun BottomNavigationTabs(
  tabs: ImmutableList<BottomNavigationTab>,
  selectedTab: Int,
  modifier: Modifier = Modifier,
  onTabSelected: (index: Int, tab: BottomNavigationTab) -> Unit,
) {
  CompositionLocalProvider(
    LocalTextStyle provides LocalTextStyle.current.copy(
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
    ),
    LocalContentColor provides Color.White,
  ) {
    Row(modifier = Modifier.fillMaxSize()) {
      tabs.fastForEachIndexed { index, tab ->
        val alpha by animateFloatAsState(
          targetValue = if (selectedTab == index) 1f else 0.35f,
          label = "alpha",
        )
        val scale by animateFloatAsState(
          targetValue = if (selectedTab == tabs.indexOf(tab)) 1f else 0.98f,
          visibilityThreshold = .000001f,
          animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy,
          ),
          label = "scale",
        )
        Column(
          modifier = Modifier
            .graphicsLayer {
              scaleX = scale
              scaleY = scale
              this.alpha = alpha
            }
            .fillMaxHeight()
            .weight(1f)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
            ) {
              onTabSelected(index, tab)
            },
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          Icon(
            painter = painterResource(tab.icon),
            contentDescription = "tab ${tab.title}",
          )
          Text(tab.title)
        }
      }
    }
  }
}
