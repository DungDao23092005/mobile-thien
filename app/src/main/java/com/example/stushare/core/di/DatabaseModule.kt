package com.example.stushare.core.di

import android.content.Context
import androidx.room.Room
import com.example.stushare.core.data.db.AppDatabase
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.db.NotificationDao
import com.example.stushare.core.data.db.UserDao
import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "stushare_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDocumentDao(database: AppDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    // --- REPOSITORIES (Cung cấp các Repo chưa chuyển sang RepositoryModule) ---

    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationDao: NotificationDao,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): NotificationRepository {
        return NotificationRepositoryImpl(notificationDao, firestore, auth)
    }

    @Provides
    @Singleton
    fun provideDocumentRepository(
        documentDao: DocumentDao,
        apiService: ApiService,
        settingsRepository: SettingsRepository,
        storage: FirebaseStorage,
        firestore: FirebaseFirestore,
        notificationRepository: NotificationRepository,
        auth: FirebaseAuth
    ): DocumentRepository {
        return DocumentRepositoryImpl(
            documentDao,
            apiService,
            settingsRepository,
            storage,
            firestore,
            notificationRepository,
            auth
        )
    }

    // ❌ ĐÃ XÓA: provideRequestRepository
    // Vì đã được bind bên RepositoryModule.kt -> Sửa lỗi DuplicateBindings thành công!

    @Provides
    @Singleton
    fun provideLeaderboardRepository(
        userDao: UserDao,
        documentDao: DocumentDao,
        firestore: FirebaseFirestore
    ): LeaderboardRepository {
        return LeaderboardRepositoryImpl(userDao, documentDao, firestore)
    }
}
