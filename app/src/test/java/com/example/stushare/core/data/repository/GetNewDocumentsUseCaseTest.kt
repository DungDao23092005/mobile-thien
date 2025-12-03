package com.example.stushare.core.domain.usecase

import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.data.models.Document
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class GetNewDocumentsUseCaseTest {

    // Mock Repository để giả lập nguồn dữ liệu
    private val mockRepository: DocumentRepository = mockk()

    private lateinit var getNewDocumentsUseCase: GetNewDocumentsUseCase

    // DATA GIẢ LẬP ĐÃ SỬA LỖI KIỂU DỮ LIỆU VÀ THAM SỐ
    private val MOCK_DOCUMENTS = listOf(
        // Tài liệu MỚI (type = "Sách")
        Document(
            id = 1L, // Sửa: Long
            title = "Toán Cao Cấp",
            type = "Sách",
            author = "User A", // Thêm tham số
            courseCode = "CS101",
            downloads = 100,
            rating = 4.5, // Sửa: Giả định là Double
            imageUrl = "http://fake.com/math.jpg"
        ),
        Document(
            id = 2L,
            title = "Vật Lý Đại Cương",
            type = "Sách",
            author = "User B",
            courseCode = "PHY101",
            downloads = 50,
            rating = 4.0, // Sửa: Giả định là Double
            imageUrl = "http://fake.com/physics.jpg"
        ),
        // Tài liệu KHÔNG PHẢI MỚI (type != "Sách")
        Document(
            id = 3L,
            title = "Đề Thi Kì 1",
            type = "Tài Liệu",
            author = "Admin",
            courseCode = "ALL",
            downloads = 200,
            rating = 5.0,
            imageUrl = "http://fake.com/exam.jpg"
        ),
        Document(
            id = 4L,
            title = "Báo Cáo Thực Tập",
            type = "Báo Cáo",
            author = "Intern A",
            courseCode = "PRJ490",
            downloads = 5,
            rating = 3.5,
            imageUrl = "http://fake.com/report.jpg"
        )
    )

    @Before
    fun setup() {
        // Cấu hình Mock Repository để nó luôn trả về danh sách MOCK_DOCUMENTS đầy đủ
        every { mockRepository.getAllDocuments() } returns flowOf(MOCK_DOCUMENTS)

        // Khởi tạo Use Case với Mock Repository
        getNewDocumentsUseCase = GetNewDocumentsUseCase(mockRepository)
    }

    // -----------------------------------------------------------------
    // KỊCH BẢN KIỂM TRA 1: LỌC THÀNH CÔNG
    // -----------------------------------------------------------------
    @Test
    fun `invoke_shouldFilterAndReturnOnlyNewDocuments`() = runTest {
        // 1. Thực hiện Use Case (Lớp nghiệp vụ)
        val result = getNewDocumentsUseCase.invoke().first()

        // 2. Kiểm tra (Assert)

        // Kích thước danh sách phải là 2
        assertEquals(2, result.size)

        // Đảm bảo chỉ chứa các tài liệu có type == "Sách"
        assertTrue(result.all { it.type == "Sách" })

        // Kiểm tra tiêu đề để xác nhận đó là đúng tài liệu
        assertEquals("Toán Cao Cấp", result[0].title)
        assertEquals("Vật Lý Đại Cương", result[1].title)
    }

    // -----------------------------------------------------------------
    // KỊCH BẢN KIỂM TRA 2: DANH SÁCH RỖNG
    // -----------------------------------------------------------------
    @Test
    fun `invoke_shouldReturnEmptyList_whenNoNewDocumentsFound`() = runTest {
        // Thiết lập Mock Repository để trả về một danh sách không có tài liệu loại "Sách"
        val mockDataWithoutNew = listOf(
            Document(
                id = 5L, title = "Project Thesis", type = "Thesis", author = "X", courseCode = "Y",
                downloads = 1, rating = 1.0, imageUrl = "http://fake.com/a.jpg"
            ),
            Document(
                id = 6L, title = "Final Review", type = "Review", author = "X", courseCode = "Y",
                downloads = 1, rating = 1.0, imageUrl = "http://fake.com/b.jpg"
            )
        )
        // Thiết lập lại hành vi của Mock Repository chỉ cho test case này
        every { mockRepository.getAllDocuments() } returns flowOf(mockDataWithoutNew)

        // Thực hiện Use Case
        val result = getNewDocumentsUseCase.invoke().first()

        // Kiểm tra: Danh sách phải rỗng
        assertTrue(result.isEmpty())
    }
}