package com.pharma.inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 재고 등록 요청 DTO (입고 처리)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCreateRequest {

    @NotNull(message = "의약품 ID는 필수입니다")
    private Long medicineId;

    @NotBlank(message = "로트번호는 필수입니다")
    @Size(max = 50, message = "로트번호는 50자를 초과할 수 없습니다")
    private String lotNumber;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    private Integer quantity;

    @NotNull(message = "유효기간은 필수입니다")
    @Future(message = "유효기간은 현재 날짜 이후여야 합니다")
    private LocalDate expiryDate;

    private LocalDate manufactureDate;

    @NotNull(message = "입고일자는 필수입니다")
    @PastOrPresent(message = "입고일자는 현재 날짜 이전이어야 합니다")
    private LocalDate receivedDate;

    @Size(max = 100, message = "공급업체명은 100자를 초과할 수 없습니다")
    private String supplierName;

    @Min(value = 0, message = "구매단가는 0 이상이어야 합니다")
    private Double purchasePrice;

    @Size(max = 255, message = "보관위치는 255자를 초과할 수 없습니다")
    private String location;

    private String remarks;
}