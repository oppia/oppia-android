package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.devoptions.RouteToMathExpressionParserTestListener

/** Tests for [DeveloperOptionsTestParsersViewModel]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class DeveloperOptionsTestParsersViewModelTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockMathParserRouteListener: RouteToMathExpressionParserTestListener

  @Test
  fun testOnMathExpressionsClicked_initialized_doesNotCallRouteToMathExpressionParserCallback() {
    DeveloperOptionsTestParsersViewModel(mockMathParserRouteListener)

    verifyNoMoreInteractions(mockMathParserRouteListener)
  }

  @Test
  fun testOnMathExpressionsClicked_callsRouteToMathExpressionParserCallback() {
    val viewModel = DeveloperOptionsTestParsersViewModel(mockMathParserRouteListener)

    viewModel.onMathExpressionsClicked()

    verify(mockMathParserRouteListener).routeToMathExpressionParserTest()
  }
}
