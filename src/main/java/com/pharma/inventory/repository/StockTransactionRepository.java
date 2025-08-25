package com.pharma.inventory.repository;

import com.pharma.inventory.entity.Medicine;
import com.pharma.inventory.entity.Stock;
import com.pharma.inventory.entity.StockTransaction;
import com.pharma.inventory.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 재고 트랜잭션 Repository
 * 입출고 이력 데이터 접근을 위한 인터페이스
 */
@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long>, JpaSpecificationExecutor<StockTransaction> {

    /**
     * 의약품별 트랜잭션 조회
     */
    List<StockTransaction> findByMedicine(Medicine medicine);

    /**
     * 재고(로트)별 트랜잭션 조회
     */
    List<StockTransaction> findByStock(Stock stock);

    /**
     * 트랜잭션 타입별 조회
     */
    List<StockTransaction> findByTransactionType(StockTransaction.TransactionType type);

    /**
     * 날짜 범위로 트랜잭션 조회
     */
    @Query("SELECT st FROM StockTransaction st WHERE st.transactionDate BETWEEN :startDate AND :endDate")
    List<StockTransaction> findByTransactionDateBetween(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    /**
     * 부서별 트랜잭션 조회
     */
    List<StockTransaction> findByDepartment(String department);

    /**
     * 요청자별 트랜잭션 조회
     */
    List<StockTransaction> findByRequesterName(String requesterName);

    /**
     * 작성자별 트랜잭션 조회
     */
    List<StockTransaction> findByCreatedBy(User createdBy);

    /**
     * 참조번호로 트랜잭션 조회
     */
    List<StockTransaction> findByReferenceNumber(String referenceNumber);

    /**
     * 최근 트랜잭션 조회 (페이징)
     */
    Page<StockTransaction> findAllByOrderByTransactionDateDesc(Pageable pageable);

    /**
     * 의약품별 최근 트랜잭션 조회
     */
    @Query("SELECT st FROM StockTransaction st WHERE st.medicine = :medicine ORDER BY st.transactionDate DESC")
    List<StockTransaction> findRecentTransactionsByMedicine(@Param("medicine") Medicine medicine, Pageable pageable);

    /**
     * 입고 트랜잭션만 조회
     */
    @Query("SELECT st FROM StockTransaction st WHERE st.transactionType = 'INBOUND'")
    List<StockTransaction> findInboundTransactions();

    /**
     * 출고 트랜잭션만 조회
     */
    @Query("SELECT st FROM StockTransaction st WHERE st.transactionType = 'OUTBOUND'")
    List<StockTransaction> findOutboundTransactions();

    /**
     * 월별 입출고 통계
     */
    @Query("""
                SELECT MONTH(st.transactionDate), st.transactionType, SUM(st.quantity)
                FROM StockTransaction st
                WHERE YEAR(st.transactionDate) = :year
                GROUP BY MONTH(st.transactionDate), st.transactionType
            """)
    List<Object[]> getMonthlyStatistics(@Param("year") int year);

    /**
     * 부서별 출고 통계
     */
    @Query("""
                SELECT st.department, SUM(st.quantity)
                FROM StockTransaction st
                WHERE st.transactionType = 'OUTBOUND'
                AND st.transactionDate BETWEEN :startDate AND :endDate
                GROUP BY st.department
            """)
    List<Object[]> getDepartmentStatistics(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * 의약품별 입출고 합계
     */
    @Query("""
                SELECT st.medicine, st.transactionType, SUM(st.quantity)
                FROM StockTransaction st
                WHERE st.transactionDate BETWEEN :startDate AND :endDate
                GROUP BY st.medicine, st.transactionType
            """)
    List<Object[]> getMedicineTransactionSummary(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * 사유별 트랜잭션 조회
     */
    List<StockTransaction> findByReason(StockTransaction.TransactionReason reason);

    /**
     * 특정 기간 동안의 폐기 트랜잭션 조회
     */
    @Query("""
                SELECT st
                FROM StockTransaction st
                WHERE st.transactionType = 'DISPOSAL'
                AND st.transactionDate BETWEEN :startDate AND :endDate
            """)
    List<StockTransaction> findDisposalTransactions(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
}
