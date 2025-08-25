package com.pharma.inventory.dto.response;

import com.pharma.inventory.entity.Stock;
import com.pharma.inventory.entity.Stock.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 재고 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private String medicineCode;
    private String lotNumber;
    private Integer quantity;
    private LocalDate expiryDate;
    private LocalDate manufactureDate;
    private LocalDate receivedDate;
    private String supplierName;
    private Double purchasePrice;
    private String location;
    private StockStatus status;
    private String statusDescription;
    private String remarks;
    private Long daysUntilExpiry;
    private Boolean isExpired;
    private Boolean isExpiringSoon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static StockResponse from(Stock stock) {
        return StockResponse.builder()
                .id(stock.getId())
                .medicineId(stock.getMedicine().getId())
                .medicineName(stock.getMedicine().getName())
                .medicineCode(stock.getMedicine().getCode())
                .lotNumber(stock.getLotNumber())
                .quantity(stock.getQuantity())
                .expiryDate(stock.getExpiryDate())
                .manufactureDate(stock.getManufactureDate())
                .receivedDate(stock.getReceivedDate())
                .supplierName(stock.getSupplierName())
                .purchasePrice(stock.getPurchasePrice())
                .location(stock.getLocation())
                .status(stock.getStatus())
                .statusDescription(stock.getStatus().getDescription())
                .remarks(stock.getRemarks())
                .daysUntilExpiry(stock.getDaysUntilExpiry())
                .isExpired(stock.isExpired())
                .isExpiringSoon(stock.isExpiringSoon(30))
                .createdAt(stock.getCreatedAt())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }
}