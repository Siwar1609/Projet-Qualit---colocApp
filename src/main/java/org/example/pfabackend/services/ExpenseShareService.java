package org.example.pfabackend.services;

public interface ExpenseShareService {
    void deleteShare(Long shareId, String userId);
    void updatePaidStatus(Long shareId, String userId, boolean paid);
}
