package com.example.stushare.features.feature_profile.ui.main

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.db.UserDao
import com.example.stushare.core.data.models.UserEntity
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.features.feature_profile.ui.model.DocItem
import com.example.stushare.features.feature_profile.ui.model.UserProfile
import com.google.firebase.FirebaseException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data object Unauthenticated : ProfileUiState

    data class Authenticated(
        val profile: UserProfile,
        val totalDocs: Int = 0,
        val totalDownloads: Int = 0,
        val memberRank: String = "Thành viên mới"
    ) : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _updateMessage = MutableSharedFlow<String>()
    val updateMessage = _updateMessage.asSharedFlow()

    private val _isUploadingAvatar = MutableStateFlow(false)
    val isUploadingAvatar = _isUploadingAvatar.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // State chứa danh sách các tài khoản khác (đã từng đăng nhập)
    private val _otherAccounts = MutableStateFlow<List<UserEntity>>(emptyList())
    val otherAccounts = _otherAccounts.asStateFlow()

    private var verificationId: String = ""

    // --- KHỐI INIT ---
    init {
        saveCurrentSessionToLocalDb()
        loadOtherAccounts()
    }

    private val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProfileUiState> = authStateFlow
        .flatMapLatest { user ->
            if (user != null) {
                saveCurrentSessionToLocalDb()
                
                val userDocFlow = callbackFlow {
                    val docRef = firestore.collection("users").document(user.uid)
                    val listener = docRef.addSnapshotListener { snapshot, _ ->
                        trySend(snapshot)
                    }
                    awaitClose { listener.remove() }
                }

                val docsFlow = documentRepository.getDocumentsByAuthor(user.uid)

                combine(userDocFlow, docsFlow) { snapshot, documents ->
                    val totalDocs = documents.size
                    val totalDownloads = documents.sumOf { it.downloads }

                    val rank = when {
                        totalDownloads > 1000 -> "Huyền thoại"
                        totalDownloads > 500 -> "Chuyên gia"
                        totalDownloads > 100 -> "Tích cực"
                        totalDocs > 5 -> "Thân thiện"
                        else -> "Thành viên mới"
                    }

                    val major = snapshot?.getString("major") ?: "Chưa cập nhật"
                    val bio = snapshot?.getString("bio") ?: ""
                    val role = snapshot?.getString("role") ?: "user"

                    val profile = UserProfile(
                        id = user.uid,
                        fullName = user.displayName ?: user.email ?: "Sinh viên UTH",
                        email = user.email ?: "",
                        avatarUrl = user.photoUrl?.toString(),
                        major = major,
                        bio = bio,
                        role = role
                    )

                    ProfileUiState.Authenticated(
                        profile = profile,
                        totalDocs = totalDocs,
                        totalDownloads = totalDownloads,
                        memberRank = rank
                    )
                }
            } else {
                flowOf(ProfileUiState.Unauthenticated)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ProfileUiState.Loading
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val publishedDocuments: StateFlow<List<DocItem>> = authStateFlow
        .flatMapLatest { user ->
            if (user != null) {
                documentRepository.getDocumentsByAuthor(user.uid).map { documents ->
                    documents.map { doc ->
                        DocItem(
                            documentId = doc.id,
                            docTitle = doc.title,
                            meta = "Đã đăng • ${doc.downloads} lượt tải"
                        )
                    }
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val savedDocuments: StateFlow<List<DocItem>> = authStateFlow
        .flatMapLatest { user ->
            if (user != null) {
                documentRepository.getBookmarkedDocuments().map { documents ->
                    documents.map { doc ->
                        DocItem(
                            documentId = doc.id,
                            docTitle = doc.title,
                            meta = "Đã lưu • ${doc.type}"
                        )
                    }
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedDocuments: StateFlow<List<DocItem>> = documentRepository.getAllDocuments()
        .map { documents ->
            documents.map { doc ->
                DocItem(
                    documentId = doc.id,
                    docTitle = doc.title,
                    meta = "Đã tải về • ${doc.type.uppercase()}"
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- LOCAL DB HELPER ---

    private fun saveCurrentSessionToLocalDb() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    val userEntity = UserEntity(
                        id = user.uid,
                        email = user.email ?: "",
                        fullName = user.displayName ?: "Người dùng",
                        avatarUrl = user.photoUrl?.toString()
                    )
                    userDao.insertUser(userEntity)
                    loadOtherAccounts()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadOtherAccounts() {
        viewModelScope.launch {
            try {
                val currentUid = auth.currentUser?.uid ?: ""
                userDao.getAllUsers().collect { allUsers ->
                    _otherAccounts.value = allUsers.filter { it.id != currentUid }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- LOGIC CẬP NHẬT THÔNG TIN ---

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                auth.currentUser?.reload()?.await()
                saveCurrentSessionToLocalDb()
                delay(1000)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // Cập nhật Chuyên ngành và Bio vào Firestore
    fun updateExtendedInfo(major: String, bio: String) {
        val uid = auth.currentUser?.uid ?: return
        val data = hashMapOf("major" to major, "bio" to bio)
        viewModelScope.launch {
            try {
                // Dùng SetOptions.merge() để không ghi đè mất các trường khác
                firestore.collection("users").document(uid).set(data, SetOptions.merge()).await()
                _updateMessage.emit("Đã lưu thông tin cá nhân! ✅")
            } catch (e: Exception) {
                _updateMessage.emit("Lỗi: ${e.message}")
            }
        }
    }

    // Cập nhật Avatar
    fun uploadAvatar(uri: Uri) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            _isUploadingAvatar.value = true
            try {
                val storageRef = storage.reference.child("avatars/${user.uid}.jpg")
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await()
                
                // Cập nhật Auth
                val profileUpdates = UserProfileChangeRequest.Builder().setPhotoUri(downloadUrl).build()
                user.updateProfile(profileUpdates).await()
                
                // Cập nhật Firestore
                firestore.collection("users").document(user.uid)
                    .set(mapOf("avatarUrl" to downloadUrl.toString()), SetOptions.merge())
                    .await()
                
                user.reload().await()
                saveCurrentSessionToLocalDb()
                
                _updateMessage.emit("Đã cập nhật ảnh đại diện!")
            } catch (e: Exception) {
                e.printStackTrace()
                _updateMessage.emit("Lỗi tải ảnh: ${e.message}")
            } finally {
                _isUploadingAvatar.value = false
            }
        }
    }

    // ✅ ĐÃ SỬA: Cập nhật Tên hiển thị (Đồng bộ Auth + Firestore)
    fun updateUserName(newName: String) {
        val user = auth.currentUser ?: return
        if (newName.isBlank()) {
            viewModelScope.launch { _updateMessage.emit("Tên không được để trống!") }
            return
        }

        // 1. Cập nhật trên Firebase Auth (để hiện ngay ở local)
        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(newName).build()
        
        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            viewModelScope.launch {
                if (task.isSuccessful) {
                    // 2. Cập nhật "fullName" vào Firestore (để người khác thấy tên mới)
                    try {
                        val updateMap = mapOf("fullName" to newName)
                        firestore.collection("users").document(user.uid)
                            .set(updateMap, SetOptions.merge())
                            .await()
                        
                        _updateMessage.emit("Cập nhật tên thành công!")
                        
                        // Reload lại để đồng bộ
                        user.reload().await()
                        saveCurrentSessionToLocalDb()
                        
                    } catch (e: Exception) {
                        _updateMessage.emit("Lỗi đồng bộ dữ liệu: ${e.message}")
                    }
                } else {
                    _updateMessage.emit("Lỗi cập nhật profile: ${task.exception?.message}")
                }
            }
        }
    }

    // --- CÁC HÀM KHÁC (Change Pass, Email, OTP...) ---

    fun changePassword(currentPass: String, newPass: String) {
        val user = auth.currentUser ?: return
        if (user.email == null) return
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
        user.reauthenticate(credential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                    viewModelScope.launch {
                        if (updateTask.isSuccessful) _updateMessage.emit("Đổi mật khẩu thành công!")
                        else _updateMessage.emit("Lỗi: ${updateTask.exception?.message}")
                    }
                }
            } else {
                viewModelScope.launch { _updateMessage.emit("Mật khẩu hiện tại không đúng!") }
            }
        }
    }

    fun updateEmail(currentPass: String, newEmail: String) {
        val user = auth.currentUser ?: return
        if (user.email == null) return
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.updateEmail(newEmail).addOnCompleteListener { updateTask ->
                    viewModelScope.launch {
                        if (updateTask.isSuccessful) {
                            // Cập nhật cả trong Firestore nếu cần
                            firestore.collection("users").document(user.uid)
                                .set(mapOf("email" to newEmail), SetOptions.merge())
                            
                            _updateMessage.emit("Đổi email thành công!")
                            saveCurrentSessionToLocalDb()
                        }
                        else _updateMessage.emit("Lỗi: ${updateTask.exception?.message}")
                    }
                }
            } else {
                viewModelScope.launch { _updateMessage.emit("Mật khẩu không đúng!") }
            }
        }
    }

    fun deletePublishedDocument(docId: String) {
        viewModelScope.launch {
            try {
                val result = documentRepository.deleteDocument(docId)
                if (result.isSuccess) _updateMessage.emit("Đã xóa tài liệu") else _updateMessage.emit("Xóa thất bại")
            } catch (e: Exception) {
                _updateMessage.emit("Lỗi khi xóa: ${e.message}")
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    updatePhoneNumber(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onError(e.message ?: "Gửi OTP thất bại")
                }

                override fun onCodeSent(
                    vId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = vId
                    onCodeSent()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyAndUpdatePhone(code: String) {
        if (verificationId.isEmpty()) return
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        updatePhoneNumber(credential)
    }

    private fun updatePhoneNumber(credential: PhoneAuthCredential) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                user.updatePhoneNumber(credential).await()
                user.reload().await()

                firestore.collection("users").document(user.uid)
                    .update("phone", user.phoneNumber)
                    .await()

                _updateMessage.emit("Cập nhật số điện thoại thành công! ✅")
            } catch (e: Exception) {
                _updateMessage.emit("Lỗi: ${e.message}")
            }
        }
    }
}