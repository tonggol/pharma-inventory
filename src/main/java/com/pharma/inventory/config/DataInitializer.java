package com.pharma.inventory.config;

import com.pharma.inventory.entity.*;
import com.pharma.inventory.entity.*;
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

import java.math.BigDecimal;
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

        // 시스템 관리자
        User admin = new User("admin", passwordEncoder.encode("admin123!"), 
                "admin@pharma.com", "시스템 관리자", UserRole.ADMIN);
        admin.updateUserInfo("시스템 관리자", "IT부", "시스템관리자", "010-1234-5678");
        
        // 재고 관리자
        User manager = new User("manager", passwordEncoder.encode("manager123!"),
                "manager@pharma.com", "김재고", UserRole.MANAGER);
        manager.updateUserInfo("김재고", "물류부", "재고관리팀장", "010-2345-6789");
        
        // 약사
        User pharmacist = new User("pharmacist", passwordEncoder.encode("pharma123!"),
                "pharmacist@pharma.com", "이약사", UserRole.PHARMACIST);
        pharmacist.updateUserInfo("이약사", "약제부", "수석약사", "010-3456-7890");
        
        // 의사
        User doctor = new User("doctor", passwordEncoder.encode("doctor123!"),
                "doctor@pharma.com", "박의사", UserRole.DOCTOR);
        doctor.updateUserInfo("박의사", "진료부", "내과과장", "010-4567-8901");
        
        // 일반 사용자
        User user = new User("user", passwordEncoder.encode("user123!"),
                "user@pharma.com", "최사용자", UserRole.USER);
        user.updateUserInfo("최사용자", "간호부", "간호사", "010-5678-9012");
        
        List<User> users = Arrays.asList(admin, manager, pharmacist, doctor, user);

        userRepository.saveAll(users);
        log.info("사용자 계정 {}개가 생성되었습니다.", users.size());
    }

    /**
     * 샘플 의약품 데이터 생성
     */
    private void createMedicines() {
        log.info("의약품 데이터를 생성합니다...");

        List<Medicine> medicines = Arrays.asList(
                new Medicine("MED001", "타이레놀정 500mg", "Tylenol Tab 500mg", "해열진통제로 두통, 발열, 근육통 등에 사용", "한국얀센", "정", MedicineCategory.fromDescription("해열진통제"), "실온보관(1~30℃)", 100, false),
                new Medicine("MED002", "게보린정", "Gevorin Tab", "두통, 신경통, 생리통, 관절염 등의 진통", "삼진제약", "정", MedicineCategory.fromDescription("해열진통제"), "실온보관(1~30℃)", 50, false),
                new Medicine("MED003", "아목시실린캡슐 250mg", "Amoxicillin Cap 250mg", "광범위 항생제, 세균감염증 치료", "유한양행", "캡슐", MedicineCategory.fromDescription("항생제"), "실온보관(1~30℃)", 200, true),
                new Medicine("MED004", "오메프라졸캡슐 20mg", "Omeprazole Cap 20mg", "위산분비억제제, 위궤양 치료", "동아제약", "캡슐", MedicineCategory.fromDescription("소화기계약물"), "실온보관(1~30℃)", 150, true),
                new Medicine("MED005", "생리식염수주사액 500mL", "Normal Saline Inj 500mL", "수액요법, 전해질 보충", "대한약품", "바이알", MedicineCategory.fromDescription("수액제"), "실온보관(1~30℃)", 300, true),
                new Medicine("MED006", "인슐린주사액 100IU/mL", "Insulin Inj 100IU/mL", "당뇨병 치료용 인슐린", "노보노디스크", "바이알", MedicineCategory.fromDescription("호르몬제"), "냉장보관(2~8℃)", 50, true),
                new Medicine("MED007", "비타민C정 1000mg", "Vitamin C Tab 1000mg", "비타민C 보충제", "고려은단", "정", MedicineCategory.fromDescription("비타민제"), "실온보관(1~30℃)", 200, false),
                new Medicine("MED008", "후시딘연고 2%", "Fucidin Oint 2%", "세균성 피부감염 치료", "동화약품", "튜브", MedicineCategory.fromDescription("외용제"), "실온보관(1~30℃)", 30, true),
                new Medicine("MED009", "엽산정 5mg", "Folic Acid Tab 5mg", "엽산결핍증 예방 및 치료", "광동제약", "정", MedicineCategory.fromDescription("비타민제"), "실온보관(1~30℃)", 100, true),
                new Medicine("MED010", "세티리진정 10mg", "Cetirizine Tab 10mg", "알레르기성 비염, 두드러기 치료", "한국유니온제약", "정", MedicineCategory.fromDescription("항히스타민제"), "실온보관(1~30℃)", 80, true)
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

        for (Medicine medicine : medicines) {
            int initialQuantity = medicine.getMinStockQuantity() * (2 + (int)(Math.random() * 4));
            LocalDate expiryDate = LocalDate.now().plusMonths(12 + (int)(Math.random() * 24));
            LocalDate manufactureDate = LocalDate.now().minusDays(90 + (int)(Math.random() * 90));
            String lotNumber = generateLotNumber();

            Stock stock = new Stock(
                    medicine,
                    lotNumber,
                    initialQuantity,
                    manufactureDate,
                    expiryDate
            );

            stock.setSupplierInfo(getRandomSupplier(), BigDecimal.valueOf(getRandomPrice()));
            stock.updateLocation("창고-A" + ((int)(Math.random() * 5) + 1) + "구역");
            stockRepository.save(stock);

            LocalDateTime transactionDate = LocalDateTime.now().minusDays((int)(Math.random() * 30));
            StockTransaction transaction = new StockTransaction(
                    medicine,
                    stock,
                    TransactionType.INBOUND,
                    initialQuantity,
                    0,
                    initialQuantity,
                    transactionDate,
                    TransactionReason.PURCHASE
            );
            
            transaction.setReferenceInfo("INIT-" + System.currentTimeMillis() % 10000, null);
            transaction.addRemarks("초기 재고 입고");
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