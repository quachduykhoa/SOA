package org.example.th5.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "orders_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OrderReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "total_revenue", precision = 10, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "total_profit", precision = 10, scale = 2)
    private BigDecimal totalProfit;

    // Một order report có nhiều product report
    @OneToMany(mappedBy = "orderReport", cascade = CascadeType.ALL)
    private List<ProductReport> productReports;

    @PrePersist @PreUpdate
    public void calculateProfit() {
        if (totalRevenue != null && totalCost != null) {
            this.totalProfit = totalRevenue.subtract(totalCost);
        }
    }
}

