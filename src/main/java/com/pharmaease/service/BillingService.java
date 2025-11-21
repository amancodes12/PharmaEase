package com.pharmaease.service;

import com.pharmaease.model.Invoice;
import com.pharmaease.model.Orders;
import com.pharmaease.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {

    private final InvoiceRepository invoiceRepository;

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    public Invoice getInvoiceByOrder(Orders order) {
        return invoiceRepository.findByOrder(order)
                .orElseThrow(() -> new RuntimeException("Invoice not found for order"));
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> getInvoicesBetweenDates(LocalDateTime start, LocalDateTime end) {
        return invoiceRepository.findByGeneratedAtBetween(start, end);
    }
}