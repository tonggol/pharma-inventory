package com.pharma.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharma.inventory.dto.request.*;
import com.pharma.inventory.dto.response.TokenResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("전체 API 통합 테스트")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 클래스 단위로 테스트 인스턴스 생성
public class PharmaInventoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;
    private String refreshToken;

    @BeforeAll
    void initialSetup() throws Exception {
        // 테스트 시작 전 단 한번만 로그인하여 토큰 발급
        // 실제 데이터베이스의 관리자 계정 정보로 수정해야 합니다.
        LoginRequest loginRequest = new LoginRequest("testadmin", "password123!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        TokenResponse tokenResponse = objectMapper.readValue(objectMapper.readTree(responseBody).get("data").toString(), TokenResponse.class);
        this.accessToken = tokenResponse.getAccessToken();
        this.refreshToken = tokenResponse.getRefreshToken();
    }

    @Test
    @DisplayName("[Medicine & Stock] 의약품과 재고 CRUD 통합 테스트")
    void medicineAndStock_Crud_IntegrationTest() throws Exception {
        // 1. 의약품 등록 (Create)
        MedicineCreateRequest createMedicineRequest = new MedicineCreateRequest(
                "TEST-DRUG-123", "테스트 의약품", "Test Drug",
                "통합 테스트용 의약품입니다.", "테스트 제약", "정",
                "일반의약품", "실온 보관", 50, false, true
        );

        MvcResult createMedicineResult = mockMvc.perform(post("/api/medicines")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMedicineRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        long medicineId = objectMapper.readTree(createMedicineResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 2. 의약품 상세 조회 (Read)
        mockMvc.perform(get("/api/medicines/" + medicineId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("테스트 의약품"));

        // 3. 의약품 수정 (Update)
        MedicineUpdateRequest updateRequest = MedicineUpdateRequest.builder().name("테스트 의약품 (수정)").build();
        mockMvc.perform(put("/api/medicines/" + medicineId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("테스트 의약품 (수정)"));

        // 4. 재고 등록 (Create Stock)
        StockCreateRequest createStockRequest = new StockCreateRequest();
        createStockRequest.setMedicineId(medicineId);
        createStockRequest.setLotNumber("LOT-12345");
        createStockRequest.setQuantity(100);
        createStockRequest.setExpiryDate(LocalDate.now().plusYears(1));
        createStockRequest.setReceivedDate(LocalDate.now()); // 필수 필드 추가

        mockMvc.perform(post("/api/stocks")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createStockRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.lotNumber").value("LOT-12345"));

        // 5. 의약품 검색 (Search)
        MedicineSearchRequest searchRequest = new MedicineSearchRequest();
        searchRequest.setName("테스트 의약품");
        mockMvc.perform(post("/api/medicines/search")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThan(0)));

        // 6. 의약품 삭제 (비활성화)
        mockMvc.perform(delete("/api/medicines/" + medicineId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[Auth] 인증 관련 API 통합 테스트")
    void auth_Apis_IntegrationTest() throws Exception {
        // 내 정보 조회
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testadmin"));

        // 토큰 갱신
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        refreshRequest.setRefreshToken(refreshToken);
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.access_token").exists());

        // 로그아웃
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[Security] 인증 없이 API 접근 시 401 Unauthorized")
    void security_UnauthorizedAccessTest() throws Exception {
        mockMvc.perform(get("/api/medicines"))
                .andExpect(status().isUnauthorized());
    }
}
