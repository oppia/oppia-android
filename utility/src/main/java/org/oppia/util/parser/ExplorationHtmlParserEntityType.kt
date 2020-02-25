package org.oppia.util.parser

import javax.inject.Qualifier

/** Qualifier for injecting the entity type for exploration. */
@Qualifier
annotation class ExplorationHtmlParserEntityType

/** Qualifier for injecting the entity type for concept card. */
@Qualifier
annotation class ConceptCardHtmlParserEntityType

/** Qualifier for injecting the entity type for review card. */
@Qualifier
annotation class ReviewCardHtmlParserEntityType
