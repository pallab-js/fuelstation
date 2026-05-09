package com.pallab.pumpmanager.core.di

import com.pallab.pumpmanager.core.util.Clock
import com.pallab.pumpmanager.core.util.IdGenerator
import com.pallab.pumpmanager.core.util.SystemClock
import com.pallab.pumpmanager.core.util.UuidGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UtilModule {

    @Binds @Singleton
    abstract fun bindClock(impl: SystemClock): Clock

    @Binds @Singleton
    abstract fun bindIdGenerator(impl: UuidGenerator): IdGenerator
}
