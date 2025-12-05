package com.example.stushare.core.di

import com.example.stushare.core.data.repository.AdminRepository
import com.example.stushare.core.data.repository.AdminRepositoryImpl
import com.example.stushare.core.data.repository.RequestRepository
import com.example.stushare.core.data.repository.RequestRepositoryImpl
// import com.example.stushare.core.data.repository.DocumentRepository
// import com.example.stushare.core.data.repository.DocumentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // 🟢 Bind AdminRepository tại đây
    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        adminRepositoryImpl: AdminRepositoryImpl
    ): AdminRepository

    // 🟢 Bind RequestRepository (Bạn đang có sẵn)
    @Binds
    @Singleton
    abstract fun bindRequestRepository(
        requestRepositoryImpl: RequestRepositoryImpl
    ): RequestRepository
}