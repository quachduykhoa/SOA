package org.example.th5.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "product_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_report_id")
    @JsonIgnore
    private OrderReport orderReport;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "total_sold")
    private Integer totalSold;

    @Column(precision = 10, scale = 2)
    private BigDecimal revenue;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(precision = 10, scale = 2)
    private BigDecimal profit;

    @PrePersist @PreUpdate
    public void calculateProfit() {
        if (revenue != null && cost != null) {
            this.profit = revenue.subtract(cost);
        }
    }
}
