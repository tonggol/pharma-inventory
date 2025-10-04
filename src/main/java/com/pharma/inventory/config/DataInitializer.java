package com.pharma.inventory.config;

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
 * アプリケーション起動時に初期データを生成するクラス
 * - 基本的なユーザーアカウントの生成
 * - サンプル医薬品データの生成
 * - 初期在庫の設定
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
        log.info("初期データの生成を開始します...");

        if (userRepository.count() > 0) {
            log.info("データが既に存在するため、初期化をスキップします。");
            return;
        }

        try {
            createUsers();
            createMedicines();
            createInitialStock();
            log.info("初期データの生成が完了しました。");
        } catch (Exception e) {
            log.error("初期データの生成中にエラーが発生しました: {}", e.getMessage(), e);
        }
    }

    private void createUsers() {
        log.info("ユーザーアカウントを生成します...");
        User admin = new User("admin", passwordEncoder.encode("admin123!"), "admin@pharma.com", "システム管理者", UserRole.ADMIN);
        admin.updateUserInfo("システム管理者", "IT部", "システム管理者", "010-1234-5678");

        User manager = new User("manager", passwordEncoder.encode("manager123!"), "manager@pharma.com", "キム・ジェゴ", UserRole.MANAGER);
        manager.updateUserInfo("キム・ジェゴ", "物流部", "在庫管理チーム長", "010-2345-6789");

        User pharmacist = new User("pharmacist", passwordEncoder.encode("pharma123!"), "pharmacist@pharma.com", "イ・ヤクサ", UserRole.PHARMACIST);
        pharmacist.updateUserInfo("イ・ヤクサ", "薬剤部", "首席薬剤師", "010-3456-7890");

        User doctor = new User("doctor", passwordEncoder.encode("doctor123!"), "doctor@pharma.com", "パク・イサ", UserRole.DOCTOR);
        doctor.updateUserInfo("パク・イサ", "診療部", "内科課長", "010-4567-8901");

        User user = new User("user", passwordEncoder.encode("user123!"), "user@pharma.com", "チェ・サヨンジャ", UserRole.USER);
        user.updateUserInfo("チェ・サヨンジャ", "看護部", "看護師", "010-5678-9012");

        List<User> users = Arrays.asList(admin, manager, pharmacist, doctor, user);
        userRepository.saveAll(users);
        log.info("{}件のユーザーアカウントが生成されました。", users.size());
    }

    private void createMedicines() {
        log.info("医薬品データを生成します...");
        List<Medicine> medicines = Arrays.asList(
            new Medicine("MED001", "タイレノール錠 500mg", "Tylenol Tab 500mg", "解熱鎮痛剤として頭痛、発熱、筋肉痛などに使用", "韓国ヤンセン", "錠", MedicineCategory.fromDescription("解熱鎮痛剤"), "室温保管(1〜30℃)", 100, false),
            new Medicine("MED002", "ゲボリン錠", "Gevorin Tab", "頭痛、神経痛、生理痛、関節炎などの鎮痛", "三進製薬", "錠", MedicineCategory.fromDescription("解熱鎮痛剤"), "室温保管(1〜30℃)", 50, false),
            new Medicine("MED003", "アモキシシリンカプセル 250mg", "Amoxicillin Cap 250mg", "広範囲抗生物質、細菌感染症の治療", "柳韓洋行", "カプセル", MedicineCategory.fromDescription("抗生物質"), "室温保管(1〜30℃)", 200, true),
            new Medicine("MED004", "オメプラゾールカプセル 20mg", "Omeprazole Cap 20mg", "胃酸分泌抑制剤、胃潰瘍の治療", "東亜製薬", "カプセル", MedicineCategory.fromDescription("消化器系薬物"), "室温保管(1〜30℃)", 150, true),
            new Medicine("MED005", "生理食塩水注射液 500mL", "Normal Saline Inj 500mL", "輸液療法、電解質補充", "大韓薬品", "バイアル", MedicineCategory.fromDescription("輸液剤"), "室温保管(1〜30℃)", 300, true),
            new Medicine("MED006", "インスリン注射液 100IU/mL", "Insulin Inj 100IU/mL", "糖尿病治療用インスリン", "ノボノルディスク", "バイアル", MedicineCategory.fromDescription("ホルモン剤"), "冷蔵保管(2〜8℃)", 50, true),
            new Medicine("MED007", "ビタミンC錠 1000mg", "Vitamin C Tab 1000mg", "ビタミンC補充剤", "高麗ウンダン", "錠", MedicineCategory.fromDescription("ビタミン剤"), "室温保管(1〜30℃)", 200, false),
            new Medicine("MED008", "フシジン軟膏 2%", "Fucidin Oint 2%", "細菌性皮膚感染症の治療", "同和薬品", "チューブ", MedicineCategory.fromDescription("外用剤"), "室温保管(1〜30℃)", 30, true),
            new Medicine("MED009", "葉酸錠 5mg", "Folic Acid Tab 5mg", "葉酸欠乏症の予防および治療", "広東製薬", "錠", MedicineCategory.fromDescription("ビタミン剤"), "室温保管(1〜30℃)", 100, true),
            new Medicine("MED010", "セチリジン錠 10mg", "Cetirizine Tab 10mg", "アレルギー性鼻炎、じんましんの治療", "韓国ユニオン製薬", "錠", MedicineCategory.fromDescription("抗ヒスタミン剤"), "室温保管(1〜30℃)", 80, true)
        );
        medicineRepository.saveAll(medicines);
        log.info("{}件の医薬品データが生成されました。", medicines.size());
    }

    private void createInitialStock() {
        log.info("多様なシナリオの初期在庫を生成します...");
        List<Medicine> medicines = medicineRepository.findAll();
        User pharmacist = userRepository.findByUsername("pharmacist").orElse(null);

        // Scenario 1: Low Stock
        Medicine lowStockMedicine = medicines.getFirst();
        int lowQuantity = lowStockMedicine.getMinStockQuantity() / 2;
        Stock lowStock = createStock(lowStockMedicine, lowQuantity, LocalDate.now().plusYears(1), "倉庫-A1区域");
        createTransaction(lowStock, TransactionType.INBOUND, TransactionReason.PURCHASE, lowQuantity, 0, lowQuantity, "初期在庫 (在庫不足テスト)", pharmacist);

        // Scenario 2: Expiring Soon
        Medicine expiringMedicine = medicines.get(1);
        int expiringQuantity = expiringMedicine.getMinStockQuantity() * 2;
        Stock expiringStock = createStock(expiringMedicine, expiringQuantity, LocalDate.now().plusDays(25), "倉庫-B2区域");
        createTransaction(expiringStock, TransactionType.INBOUND, TransactionReason.PURCHASE, expiringQuantity, 0, expiringQuantity, "初期在庫 (有効期限間近テスト)", pharmacist);

        // Scenario 3: Outbound Transaction
        Medicine outboundMedicine = medicines.get(2);
        int outboundInitialQty = outboundMedicine.getMinStockQuantity() * 5;
        Stock outboundStock = createStock(outboundMedicine, outboundInitialQty, LocalDate.now().plusYears(2), "倉庫-C3区域");
        createTransaction(outboundStock, TransactionType.INBOUND, TransactionReason.PURCHASE, outboundInitialQty, 0, outboundInitialQty, "初期在庫", pharmacist);
        int outboundQty = 30;
        outboundStock.adjustQuantity(outboundInitialQty - outboundQty);
        stockRepository.save(outboundStock);
        createTransaction(outboundStock, TransactionType.OUTBOUND, TransactionReason.PRESCRIPTION, outboundQty, outboundInitialQty, outboundInitialQty - outboundQty, "処方による出庫", pharmacist);

        // Scenario 4: Adjustment Transaction
        Medicine adjustmentMedicine = medicines.get(3);
        int adjustmentInitialQty = adjustmentMedicine.getMinStockQuantity() * 3;
        Stock adjustmentStock = createStock(adjustmentMedicine, adjustmentInitialQty, LocalDate.now().plusYears(1), "倉庫-D4区域");
        createTransaction(adjustmentStock, TransactionType.INBOUND, TransactionReason.PURCHASE, adjustmentInitialQty, 0, adjustmentInitialQty, "初期在庫", pharmacist);
        int adjustmentQty = -5; // 5개 감소
        adjustmentStock.adjustQuantity(adjustmentInitialQty + adjustmentQty);
        stockRepository.save(adjustmentStock);
        createTransaction(adjustmentStock, TransactionType.ADJUSTMENT, TransactionReason.INVENTORY_CHECK, Math.abs(adjustmentQty), adjustmentInitialQty, adjustmentStock.getQuantity(), "棚卸による在庫調整", pharmacist);

        // Create normal stock for the rest
        for (int i = 4; i < medicines.size(); i++) {
            Medicine medicine = medicines.get(i);
            int initialQuantity = medicine.getMinStockQuantity() * (2 + (int)(Math.random() * 3));
            Stock stock = createStock(medicine, initialQuantity, LocalDate.now().plusMonths(12 + (int)(Math.random() * 12)), "倉庫-E" + (i - 3) + "区域");
            createTransaction(stock, TransactionType.INBOUND, TransactionReason.PURCHASE, initialQuantity, 0, initialQuantity, "初期在庫入庫", pharmacist);
        }

        log.info("{}件の初期在庫シナリオが生成されました。", medicines.size());
    }

    private Stock createStock(Medicine medicine, int quantity, LocalDate expiryDate, String location) {
        Stock stock = new Stock(
                medicine,
                generateLotNumber(),
                quantity,
                LocalDate.now().minusMonths(6),
                expiryDate
        );
        stock.setSupplierInfo(getRandomSupplier(), BigDecimal.valueOf(getRandomPrice()));
        stock.updateLocation(location);
        return stockRepository.save(stock);
    }

    private void createTransaction(Stock stock, TransactionType type, TransactionReason reason, int quantity, int stockBefore, int stockAfter, String remarks, User user) {
        StockTransaction transaction = new StockTransaction(
                stock.getMedicine(),
                stock,
                type,
                quantity,
                stockBefore,
                stockAfter,
                LocalDateTime.now().minusDays((int)(Math.random() * 10)),
                reason
        );
        transaction.setCreatedBy(user);
        transaction.addRemarks(remarks);
        stockTransactionRepository.save(transaction);
    }

    private String generateLotNumber() {
        return "LOT" + (100000 + (int)(Math.random() * 900000));
    }

    private String getRandomSupplier() {
        String[] suppliers = {"大熊製薬", "柳韓洋行", "東亜製薬", "韓国ヤンセン", "三進製薬", "広東製薬", "同和薬品", "高麗ウンダン"};
        return suppliers[(int)(Math.random() * suppliers.length)];
    }

    private Double getRandomPrice() {
        return 100.0 + (Math.random() * 49900.0);
    }
}