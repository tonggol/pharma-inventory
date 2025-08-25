package com.pharma.inventory.repository;

import com.pharma.inventory.entity.Medicine;
import com.pharma.inventory.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 재고 Repository
 * 재고 데이터 접근을 위한 인터페이스
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long>, JpaSpecificationExecutor<Stock> {
    
    /**
     * 의약품별 재고 조회
     */
    List<Stock> findByMedicine(Medicine medicine);
    
    /**
     * 의약품 ID로 재고 조회
     */
    List<Stock> findByMedicineId(Long medicineId);
    
    /**
     * 로트번호로 재고 조회
     */
    Optional<Stock> findByLotNumber(String lotNumber);
    
    /**
     * 의약품과 로트번호로 재고 조회
     */
    Optional<Stock> findByMedicineAndLotNumber(Medicine medicine, String lotNumber);
    
    /**
     * 사용 가능한 재고만 조회 (상태가 AVAILABLE)
     */
    @Query("SELECT s FROM Stock s WHERE s.status = 'AVAILABLE'")
    List<Stock> findAvailableStocks();
    
    /**
     * 의약품별 사용 가능한 재고 조회
     */
    @Query("SELECT s FROM Stock s WHERE s.medicine = :medicine AND s.status = 'AVAILABLE' AND s.quantity > 0")
    List<Stock> findAvailableStocksByMedicine(@Param("medicine") Medicine medicine);
    
    /**
     * 유효기간이 만료된 재고 조회
     */
    @Query("SELECT s FROM Stock s WHERE s.expiryDate < :currentDate")
    List<Stock> findExpiredStocks(@Param("currentDate") LocalDate currentDate);
    
    /**
     * 유효기간이 임박한 재고 조회 (N일 이내)
     */
    @Query("SELECT s FROM Stock s WHERE s.expiryDate BETWEEN :currentDate AND :limitDate AND s.status = 'AVAILABLE'")
    List<Stock> findExpiringSoonStocks(@Param("currentDate") LocalDate currentDate, 
                                       @Param("limitDate") LocalDate limitDate);
    
    /**
     * 재고 부족 의약품 조회 (최소 재고 수량 이하)
     */
    @Query("SELECT s.medicine, SUM(s.quantity) as totalQuantity " +
           "FROM Stock s " +
           "WHERE s.status = 'AVAILABLE' " +
           "GROUP BY s.medicine " +
           "HAVING SUM(s.quantity) <= s.medicine.minStockQuantity")
    List<Object[]> findLowStockMedicines();
    
    /**
     * 의약품별 총 재고 수량 계산
     */
    @Query("SELECT SUM(s.quantity) FROM Stock s WHERE s.medicine = :medicine AND s.status = 'AVAILABLE'")
    Integer getTotalQuantityByMedicine(@Param("medicine") Medicine medicine);
    
    /**
     * 공급업체별 재고 조회
     */
    List<Stock> findBySupplierName(String supplierName);
    
    /**
     * 보관 위치별 재고 조회
     */
    List<Stock> findByLocation(String location);
    
    /**
     * 날짜 범위로 입고된 재고 조회
     */
    @Query("SELECT s FROM Stock s WHERE s.receivedDate BETWEEN :startDate AND :endDate")
    List<Stock> findByReceivedDateBetween(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * 상태별 재고 수 집계
     */
    @Query("SELECT s.status, COUNT(s), SUM(s.quantity) FROM Stock s GROUP BY s.status")
    List<Object[]> countByStatus();
    
    /**
     * 로트번호 중복 체크
     */
    boolean existsByLotNumber(String lotNumber);
    
    /**
     * 유효기간이 가장 빠른 재고부터 조회 (FEFO - First Expired First Out)
     */
    @Query("SELECT s FROM Stock s WHERE s.medicine = :medicine AND s.status = 'AVAILABLE' AND s.quantity > 0 ORDER BY s.expiryDate ASC")
    List<Stock> findAvailableStocksByMedicineOrderByExpiryDate(@Param("medicine") Medicine medicine);
    
    /**
     * 상태별 재고 조회
     */
    List<Stock> findByStatus(Stock.StockStatus status);
    
    /**
     * 만료 예정 재고 조회 (지정 날짜까지)
     */
    @Query("SELECT s FROM Stock s WHERE s.expiryDate <= :expiryDate AND s.expiryDate >= CURRENT_DATE")
    List<Stock> findExpiringStocks(@Param("expiryDate") LocalDate expiryDate);
}
