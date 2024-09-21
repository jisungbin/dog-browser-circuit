package land.sungbin.dogbrowser.circuit.screen

import com.slack.circuit.runtime.screen.Screen

public sealed interface TitledScreen : Screen {
  public val title: String
}
