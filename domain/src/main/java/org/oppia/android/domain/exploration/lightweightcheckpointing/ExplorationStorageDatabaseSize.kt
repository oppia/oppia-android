package org.oppia.android.domain.exploration.lightweightcheckpointing

import javax.inject.Qualifier

/**
 * Represents an application injectable integer that indicates the size allocated to exploration
 * checkpoint database.
 *
 * The default current size is set to 2097152 bytes that is equal to 2MiB per profile. For an
 * assumed 20 KiB per checkpoint, this amount is expected to provide sufficient storage for
 * approximately 100 checkpoints for each profile before new checkpoints will start being dropped.
 */
@Qualifier annotation class ExplorationStorageDatabaseSize
