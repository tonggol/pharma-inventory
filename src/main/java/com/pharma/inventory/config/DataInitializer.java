package com.pharma.inventory.config;

import com.pharma.inventory.entity.Medicine;
import com.pharma.inventory.entity.Stock;
import com.pharma.inventory.entity.StockTransaction;
import com.pharma.inventory.entity.User;
import com.pharma.inventory.repository.MedicineRepository;
import com.pharma.inventory.repository.StockRepository;
import com.pharma.inventory.repository.StockTransactionRepository;
import com.pharma.inventory.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 애플리케이션 시작 시 초기 데이터를 생성하는 클래스
 * - 기본 사용자 계정 생성
 * - 샘플 의약품 데이터 생성
 * - 초기 재고 설정
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("초기 데이터 생성을 시작합니다...");

        // 데이터가 이미 존재하면 스킵
        if (userRepository.count() > 0) {
            log.info("이미 데이터가 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        try {
            createUsers();
            createMedicines();
            createInitialStock();
            log.info("초기 데이터 생성이 완료되었습니다.");
        } catch (Exception e) {
            log.error("초기 데이터 생성 중 오류가 발생했습니다: {}", e.getMessage(), e);
        }
    }

    /**
     * 기본 사용자 계정 생성
     */
    private void createUsers() {
        log.info("사용자 계정을 생성합니다...");

        List<User> users = Arrays.asList(
                // 시스템 관리자
                User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123!"))
                        .email("admin@pharma.com")
                        .fullName("시스템 관리자")
                        .employeeId("EMP001")
                        .department("IT부")
                        .position("시스템관리자")
                        .phoneNumber("010-1234-5678")
                        .role(User.UserRole.ADMIN)
                        .isActive(true)
                        .build(),

                // 재고 관리자
                User.builder()
                        .username("manager")
                        .password(passwordEncoder.encode("manager123!"))
                        .email("manager@pharma.com")
                        .fullName("김재고")
                        .employeeId("EMP002")
                        .department("물류부")
                        .position("재고관리팀장")
                        .phoneNumber("010-2345-6789")
                        .role(User.UserRole.MANAGER)
                        .isActive(true)
                        .build(),

                // 약사
                User.builder()
                        .username("pharmacist")
                        .password(passwordEncoder.encode("pharma123!"))
                        .email("pharmacist@pharma.com")
                        .fullName("이약사")
                        .employeeId("EMP003")
                        .department("약제부")
                        .position("수석약사")
                        .phoneNumber("010-3456-7890")
                        .role(User.UserRole.PHARMACIST)
                        .isActive(true)
                        .build(),

                // 의사
                User.builder()
                        .username("doctor")
                        .password(passwordEncoder.encode("doctor123!"))
                        .email("doctor@pharma.com")
                        .fullName("박의사")
                        .employeeId("EMP004")
                        .department("진료부")
                        .position("내과과장")
                        .phoneNumber("010-4567-8901")
                        .role(User.UserRole.DOCTOR)
                        .isActive(true)
                        .build(),

                // 일반 사용자
                User.builder()
                        .username("user")
                        .password(passwordEncoder.encode("user123!"))
                        .email("user@pharma.com")
                        .fullName("최사용자")
                        .employeeId("EMP005")
                        .department("간호부")
                        .position("간호사")
                        .phoneNumber("010-5678-9012")
                        .role(User.UserRole.USER)
                        .isActive(true)
                        .build()
        );

        userRepository.saveAll(users);
        log.info("사용자 계정 {}개가 생성되었습니다.", users.size());
    }

    /**
     * 샘플 의약품 데이터 생성
     */
    private void createMedicines() {
        log.info("의약품 데이터를 생성합니다...");

        List<Medicine> medicines = Arrays.asList(
                // 일반의약품
                Medicine.builder()
                        .code("MED001")
                        .name("타이레놀정 500mg")
                        .nameEn("Tylenol Tab 500mg")
                        .description("해열진통제로 두통, 발열, 근육통 등에 사용")
                        .manufacturer("한국얀센")
                        .unit("정")
                        .category("해열진통제")
                        .storageCondition("실온보관(1~30℃)")
                        .minStockQuantity(100)
                        .isPrescriptionRequired(false)
                        .isActive(true)
                        .build(),

                Medicine.builder()
                        .code("MED002")
                        .name("게보린정")
                        .nameEn("Gevorin Tab")
                        .description("두통, 신경통, 생리통, 관절염 등의 진통")
                        .manufacturer("삼진제약")
                        .unit("정")
                        .category("해열진통제")
                        .storageCondition("실온보관(1~30℃)")
                        .minStockQuantity(50)
                        .isPrescriptionRequired(false)
                        .isActive(true)
                        .build(),

                // 전문의약품
                Medicine.builder()
                        .code("MED003")
                        .name("아목시실린캡슐 250mg")
                        .nameEn("Amoxicillin Cap 250mg")
                        .description("광범위 항생제, 세균감염증 치료")
                        .manufacturer("유한양행")
                        .unit("캡슐")
                        .category("항생제")
                        .storageCondition("실온보관(1~30℃)")
                        .minStockQuantity(200)
                        .isPrescriptionRequired(true)
                        .isActive(true)
                        .build(),

                Medicine.builder()
                        .code("MED004")
                        .name("오메프라졸캡슐 20mg")
                        .nameEn("Omeprazole Cap 20mg")
                        .description("위산분비억제제, 위궤양 치료")
                        .manufacturer("동아제약")
                        .unit("캡슐")
                        .category("소화기계약물")
                        .storageCondition("실온보관(1~30℃)")
                        .minStockQuantity(150)
                        .isPrescriptionRequired(true)
                        .isActive(true)
                        .build(),

                // 주사제
                Medicine.builder()
                        .code("MED005")
                        .name("생리식염수주사액 500mL")
                        .nameEn("Normal Saline Inj 500mL")
                        .description("수액요법, 전해질 보충")
                        .manufacturer("대한약품")
                        .unit("바이알")
                        .category("수액제")
                        .storageCondition("실온보관(1~30℃)")
                        .minStockQuantity(300)
                        .isPrescriptionRequired(true)
                        .isActive(true)
                        .build(),

                Medicine.builder()
                        .code("MED006")
                        .name("인슐린주사액 100IU/mL")
                        .nameEn("Insulin Inj 100IU/mL")
                        .description("당뇨병 치료용 인슐린")
                        .manufacturer("노보노디스크")
                        .unit("바이알")
                        .category("호르몬제")
                        .storageCondition("냉장보관(2~8℃)")
                        .minStockQuantity(50)
                        .isPrescriptionRequired(true)
                        .isActive(true)
                        .build(),

                // 기타 의약품
                Medicine.builder()
                        .code("MED007")
                        .name("비타민C정 1000mg")
                        .nameEn("Vitamin C Tab 1000mg")
                        .description("비타민C 보충제")
                        .manufacturer("고려은단")
                        .unit("정")
                        .category("비타민제")
                        .storageCondition("실온보관(1~30℃)")
                        .minStockQuantity(200)
                        .isPrescriptionRequired(false)
                        .isActive(true)
                        .build(),

                Medicine.builder()
                        .code("MED008")
                        .name("후시딘연고 2%")
                        .nameEn("Fucidin Oint 2%")
                        .description("세균성 피부감염 치료")
                        .manufacturer("동화약품")
                        .unit("튜브")
                        .category("외용제")
                        .storageCondition("실온보관(1~30℃)")
                        .minStockQuantity(30)
                        .isPrescriptionRequired(true)
                        .isActive(true)
                        .build(),

                Medicine.builder()
                        .code("MED009")
                        .name("엽산정 5mg")
                        .nameEn("Folic Acid Tab 5mg")
                        .description("엽산결핍증 예방 및 치료")
                        .manufacturer("광동제약")
                        .unit("정")
                        .category("비타민제")
                        .storageCondition("실온보관(1~30℃)")
                        .minStockQuantity(100)
                        .isPrescriptionRequired(true)
                        .isActive(true)
                        .build(),

                Medicine.builder()
                        .code("MED010")
                        .name("세티리진정 10mg")
                        .nameEn("Cetirizine Tab 10mg")
                        .description("알레르기성 비염, 두드러기 치료")
                        .manufacturer("한국유니온제약")
                        .unit("정")
                        .category("항히스타민제")
                        .storageCondition("실온보관(1~30℃)")
                        .minStockQuantity(80)
                        .isPrescriptionRequired(true)
                        .isActive(true)
                        .build()
        );

        medicineRepository.saveAll(medicines);
        log.info("의약품 데이터 {}개가 생성되었습니다.", medicines.size());
    }

    /**
     * 초기 재고 및 거래 내역 생성
     */
    private void createInitialStock() {
        log.info("초기 재고를 생성합니다...");

        List<Medicine> medicines = medicineRepository.findAll();
        User adminUser = userRepository.findByUsername("admin").orElseThrow();

        for (Medicine medicine : medicines) {
            // 초기 재고 수량 설정 (최소재고의 2-5배)
            int initialQuantity = medicine.getMinStockQuantity() * (2 + (int)(Math.random() * 4));

            // 유효기간 설정 (1~3년 후)
            LocalDate expiryDate = LocalDate.now().plusMonths(12 + (int)(Math.random() * 24));

            // 재고 생성
            Stock stock = Stock.builder()
                    .medicine(medicine)
                    .quantity(initialQuantity)
                    .lotNumber(generateLotNumber())
                    .expiryDate(expiryDate)
                    .receivedDate(LocalDate.now().minusDays((int)(Math.random() * 30)))
                    .supplierName(getRandomSupplier())
                    .purchasePrice(getRandomPrice())
                    .location("창고-A" + ((int)(Math.random() * 5) + 1) + "구역")
                    .status(Stock.StockStatus.AVAILABLE)
                    .build();

            stockRepository.save(stock);

            // 입고 거래 내역 생성
            StockTransaction transaction = StockTransaction.builder()
                    .stock(stock)
                    .medicine(medicine)
                    .transactionType(StockTransaction.TransactionType.INBOUND)
                    .quantity(initialQuantity)
                    .beforeQuantity(0)
                    .afterQuantity(initialQuantity)
                    .referenceNumber("INIT-" + System.currentTimeMillis() % 10000)
                    .reason(StockTransaction.TransactionReason.PURCHASE)
                    .remarks("초기 재고 입고")
                    .transactionDate(LocalDateTime.now().minusDays((int)(Math.random() * 30)))
                    .createdBy(adminUser)
                    .build();

            stockTransactionRepository.save(transaction);
        }

        log.info("초기 재고 {}개가 생성되었습니다.", medicines.size());
    }

    /**
     * 로트번호 생성
     */
    private String generateLotNumber() {
        return "LOT" + System.currentTimeMillis() % 1000000;
    }

    /**
     * 랜덤 공급업체 선택
     */
    private String getRandomSupplier() {
        String[] suppliers = {
                "대웅제약", "유한양행", "동아제약", "한국얀센",
                "삼진제약", "광동제약", "동화약품", "고려은단"
        };
        return suppliers[(int)(Math.random() * suppliers.length)];
    }

    /**
     * 랜덤 가격 생성
     */
    private Double getRandomPrice() {
        // 100원 ~ 50,000원 사이의 랜덤 가격
        return 100.0 + (Math.random() * 49900.0);
    }
}