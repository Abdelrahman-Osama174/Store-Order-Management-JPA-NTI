package com.store.service;

import com.store.dto.*;
import com.store.enums.OrderStatus;
import com.store.model.Product;
import com.store.repository.ReportRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ReportRepo reportRepository;

    public ReportService(ReportRepo reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryRevenue> revenueByCategory() {
        return reportRepository.revenueByCategory();
    }

    @Transactional(readOnly = true)
    public List<CustomerSpend> topCustomers(int limit) {
        return reportRepository.topCustomers(limit);
    }

    @Transactional(readOnly = true)
    public Map<OrderStatus, Long> ordersPerStatus() {
        return reportRepository.ordersPerStatus();
    }

    @Transactional(readOnly = true)
    public List<Product> productsNeverOrdered() {
        return reportRepository.productsNeverOrdered();
    }

    @Transactional(readOnly = true)
    public List<MonthlySales> monthlySales(int year) {
        return reportRepository.monthlySales(year);
    }

    @Transactional
    public int applyDiscount(String category, double percent) {
        return reportRepository.applyDiscount(category, percent);
    }
}