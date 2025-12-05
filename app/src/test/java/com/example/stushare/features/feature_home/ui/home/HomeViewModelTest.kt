package com.example.stushare.features.feature_home.ui.home

import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.DocumentRequest
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.data.repository.NotificationRepository
import com.example.stushare.core.data.repository.RequestRepository
import com.example.stushare.core.domain.usecase.GetExamDocumentsUseCase
import com.example.stushare.core.domain.usecase.GetNewDocumentsUseCase
import com.example.stushare.rules.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    // 2. Áp dụng Rule cho Coroutine
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 3. Khai báo các đối tượng giả (mock)
    private lateinit var mockRepository: DocumentRepository
    private lateinit var mockGetNewDocsUseCase: GetNewDocumentsUseCase
    private lateinit var mockGetExamDocsUseCase: GetExamDocumentsUseCase
    private lateinit var mockNotificationRepository: NotificationRepository // 🟢 Thêm
    private lateinit var mockRequestRepository: RequestRepository         // 🟢 Thêm
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser

    // 4. Đối tượng cần test
    private lateinit var viewModel: HomeViewModel

    // 5. Dữ liệu giả để test - 🟢 SỬA LẠI CONSTRUCTOR DOCUMENT (Dùng named arguments cho an toàn)
    private val fakeNewDocs = listOf(
        Document(
            id = "1",
            title = "Sách Mới 1",
            type = "Sách",
            imageUrl = "",
            downloads = 10,
            rating = 4.5,
            author = "Tác giả A",
            courseCode = "IT123"
        )
    )
    private val fakeExamDocs = listOf(
        Document(
            id = "2",
            title = "Đề Thi 1",
            type = "Tài Liệu",
            imageUrl = "",
            downloads = 20,
            rating = 4.8,
            author = "Tác giả B",
            courseCode = "CS101"
        )
    )

    // 6. Hàm Setup: Chạy trước mỗi hàm @Test
    @Before
    fun setUp() {
        // Khởi tạo Mocks
        mockRepository = mockk(relaxed = true)
        mockGetNewDocsUseCase = mockk()
        mockGetExamDocsUseCase = mockk()
        mockNotificationRepository = mockk(relaxed = true) // 🟢 Init
        mockRequestRepository = mockk(relaxed = true)      // 🟢 Init
        mockFirebaseAuth = mockk()
        mockFirebaseUser = mockk()

        // 7. Định nghĩa hành vi mặc định cho Mocks

        // Khi Use Case được gọi, trả về dữ liệu giả
        every { mockGetNewDocsUseCase.invoke() } returns flowOf(fakeNewDocs)
        every { mockGetExamDocsUseCase.invoke() } returns flowOf(fakeExamDocs)
        
        // 🟢 Mock thêm các hàm mới được gọi trong init của ViewModel
        every { mockRepository.getDocumentsByType("book") } returns flowOf(emptyList())
        every { mockRepository.getDocumentsByType("lecture") } returns flowOf(emptyList())
        every { mockNotificationRepository.getUnreadCount() } returns flowOf(0)
        every { mockRequestRepository.getAllRequests() } returns flowOf(emptyList<DocumentRequest>())

        // Khi hỏi thông tin User, trả về "Test User"
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.displayName } returns "Test User"
        every { mockFirebaseUser.photoUrl } returns null

        // 8. Khởi tạo ViewModel (hàm init sẽ tự động chạy)
        // 🟢 CẬP NHẬT CONSTRUCTOR CHO ĐÚNG VỚI HOMEVIEWMODEL HIỆN TẠI
        viewModel = HomeViewModel(
            repository = mockRepository,
            getNewDocumentsUseCase = mockGetNewDocsUseCase,
            getExamDocumentsUseCase = mockGetExamDocsUseCase,
            notificationRepository = mockNotificationRepository,
            requestRepository = mockRequestRepository,
            firebaseAuth = mockFirebaseAuth
        )
    }

    // --- KỊCH BẢN TEST 1 ---
    @Test
    fun `init - tải thành công, UiState được cập nhật chính xác`() {
        // Khi ViewModel được khởi tạo trong hàm setUp():
        val currentState = viewModel.uiState.value

        // Kiểm tra
        assertFalse(currentState.isLoading) // Loading phải tắt sau khi init
        assertNull(currentState.errorMessage) // Không có lỗi

        // Kiểm tra dữ liệu
        assertEquals(fakeNewDocs, currentState.newDocuments)
        assertEquals(fakeExamDocs, currentState.examDocuments)

        // Kiểm tra thông tin User
        assertEquals("Test User", currentState.userName)
    }

    // --- KỊCH BẢN TEST 2 ---
    @Test
    fun `refreshData - xảy ra lỗi mạng, UiState cập nhật errorMessage`() {
        // 1. Giả lập: Khi gọi refresh, Repository sẽ ném ra lỗi
        val networkError = DataFailureException.NetworkError
        coEvery { mockRepository.refreshDocumentsIfStale() } throws networkError

        // 2. Hành động: Gọi hàm refresh
        viewModel.refreshData()

        // 3. Kiểm tra:
        val currentState = viewModel.uiState.value
        assertFalse(currentState.isLoading) // Loading phải tắt
        assertFalse(currentState.isRefreshing) // Refreshing phải tắt

        // Quan trọng: Kiểm tra thông báo lỗi
        assertNotNull(currentState.errorMessage)
        assertEquals(networkError.message, currentState.errorMessage)
    }

    // --- KỊCH BẢN TEST 3 ---
    @Test
    fun `clearError - xóa errorMessage khỏi UiState`() {
        // 1. Giả lập: Trạng thái đang có lỗi (tương tự test 2)
        val networkError = DataFailureException.NetworkError
        coEvery { mockRepository.refreshDocumentsIfStale() } throws networkError
        viewModel.refreshData()

        // Kiểm tra (trước khi gọi)
        assertNotNull(viewModel.uiState.value.errorMessage)

        // 2. Hành động: Gọi clearError
        viewModel.clearError()

        // 3. Kiểm tra:
        assertNull(viewModel.uiState.value.errorMessage) // Lỗi đã bị xóa
    }
}