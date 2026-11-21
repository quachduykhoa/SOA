package org.example.th5.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductReportRequest {
    private Integer orderReportId;
    private Integer productId;
    private Integer totalSold;
    private BigDecimal revenue;
    private BigDecimal cost;
}
