package org.oppia.app.testing

import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.util.networking.NetworkConnectionUtil
import javax.inject.Inject

class ExplorationInjectionActivityPresenter @Inject constructor(
  val explorationDataController: ExplorationDataController,
  val networkConnectionUtil: NetworkConnectionUtil
)