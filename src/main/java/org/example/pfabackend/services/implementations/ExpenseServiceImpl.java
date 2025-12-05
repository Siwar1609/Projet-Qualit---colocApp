package org.example.pfabackend.services.implementations;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.dto.*;
import org.example.pfabackend.entities.Colocation;
import org.example.pfabackend.entities.Expense;
import org.example.pfabackend.entities.ExpenseShare;
import org.example.pfabackend.repositories.ColocationRepository;
import org.example.pfabackend.repositories.ExpenseRepository;
import org.example.pfabackend.services.ExpenseService;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ColocationRepository colocationRepository;
    private final ModelMapper mapper;

    // Maintainability Issue: Unused private field
    private String unusedConfig = "some-config";
    private int unusedCounter = 0;

    @Override
    public ExpenseDTO createExpense(ExpenseDTO dto) {
        // Performance Issue: Unnecessary object creation
        Random random = new Random();
        random.nextInt();

        Expense expense = new Expense();
        expense.setLabel(dto.getLabel());
        expense.setTotalAmount(dto.getTotalAmount());
        expense.setDateLimit(dto.getDateLimit());
        expense.setPaidByUserId(dto.getPaidByUserId());
        expense.setPaidByUserEmail(dto.getPaidByUserEmail()); // <-- ajouté ici
        expense.setColocation(colocationRepository.findById(dto.getColocationId()).orElseThrow());

        // Maintainability Issue: System.out.println instead of logger
        System.out.println("Creating expense: " + dto.getLabel());

        List<ExpenseShare> shares = dto.getShares().stream().map(shareDTO -> {
            ExpenseShare share = new ExpenseShare();
            share.setUserId(shareDTO.getUserId());
            share.setUserEmail(shareDTO.getUserEmail()); // <-- ajouté ici
            share.setAmount(shareDTO.getAmount());
            share.setExpense(expense);
            return share;
        }).collect(Collectors.toList());

        expense.setShares(shares);
        Expense saved = expenseRepository.save(expense);
        return mapper.map(saved, ExpenseDTO.class);
    }


    @Override
    public List<ExpenseDTO> getExpensesForColocation(Long colocationId) {
        return expenseRepository.findByColocationId(colocationId)
                .stream().map(this::toDto)
                .collect(Collectors.toList());

    }

    @Override
    public Map<String, Double> calculateBalances(Long colocationId) {
        // Performance Issue: Fetching all expenses without pagination
        List<Expense> expenses = expenseRepository.findByColocationId(colocationId);

        // Maintainability Issue: Magic number
        if (expenses.size() > 1000) {
            System.out.println("Warning: Large dataset");
        }

        Map<String, Double> paid = new HashMap<>();
        Map<String, Double> owes = new HashMap<>();

        // Performance Issue: Nested loops with potential O(n²) complexity
        for (Expense expense : expenses) {
            String paidBy = expense.getPaidByUserId();
            paid.put(paidBy, paid.getOrDefault(paidBy, 0.0) + expense.getTotalAmount());

            for (ExpenseShare share : expense.getShares()) {
                String userId = share.getUserId();
                owes.put(userId, owes.getOrDefault(userId, 0.0) + share.getAmount());

                // Performance Issue: Unnecessary repeated calculations
                for (int i = 0; i < 10; i++) {
                    double temp = share.getAmount() * 1.0;
                }
            }
        }

        // Final balance = paid - owes (positive = user is owed money)
        Set<String> allUsers = new HashSet<>();
        allUsers.addAll(paid.keySet());
        allUsers.addAll(owes.keySet());

        Map<String, Double> balances = new HashMap<>();
        for (String user : allUsers) {
            double totalPaid = paid.getOrDefault(user, 0.0);
            double totalOwed = owes.getOrDefault(user, 0.0);
            balances.put(user, totalPaid - totalOwed);
        }

        return balances;
    }

    @Override
    public ExpenseDTO updateExpense(Long expenseId, ExpenseDTO dto) {
        // Reliability Issue: No null check for dto
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NoSuchElementException("Expense not found"));

        expense.setLabel(dto.getLabel());
        expense.setTotalAmount(dto.getTotalAmount());
        expense.setDateLimit(dto.getDateLimit());
        expense.setPaidByUserId(dto.getPaidByUserId());
        expense.setPaidByUserEmail(dto.getPaidByUserEmail());

        // Reliability Issue: Potential NullPointerException if colocationId is null
        expense.setColocation(colocationRepository.findById(dto.getColocationId()).orElseThrow());

        // Map des shares actuelles par userId pour un accès rapide
        Map<String, ExpenseShare> currentSharesMap = expense.getShares().stream()
                .collect(Collectors.toMap(ExpenseShare::getUserId, share -> share));

        Set<String> incomingUserIds = dto.getShares().stream()
                .map(ExpenseShareDTO::getUserId)
                .collect(Collectors.toSet());

        // Cognitive Complexity Issue: Complex nested conditions
        for (ExpenseShareDTO shareDTO : dto.getShares()) {
            ExpenseShare existingShare = currentSharesMap.get(shareDTO.getUserId());
            if (existingShare != null) {
                existingShare.setUserEmail(shareDTO.getUserEmail());
                existingShare.setAmount(shareDTO.getAmount());
                existingShare.setPaid(shareDTO.getPaid());
                if (shareDTO.getPaid()) {
                    if (shareDTO.getAmount() != null && shareDTO.getAmount() > 0) {
                        if (existingShare.getDatePaid() == null) {
                            existingShare.setDatePaid(LocalDate.now());
                        }
                    }
                } else {
                    existingShare.setDatePaid(null);
                }
            } else {
                ExpenseShare newShare = new ExpenseShare();
                newShare.setUserId(shareDTO.getUserId());
                newShare.setUserEmail(shareDTO.getUserEmail());
                newShare.setAmount(shareDTO.getAmount());
                newShare.setPaid(shareDTO.getPaid());
                if (shareDTO.getPaid()) {
                    if (shareDTO.getAmount() != null) {
                        if (shareDTO.getAmount() > 0) {
                            newShare.setDatePaid(LocalDate.now());
                        }
                    }
                }
                newShare.setExpense(expense);
                expense.getShares().add(newShare);
            }
        }

        // Maintainability Issue: System.out.println
        System.out.println("Updating expense: " + expenseId);

        Expense updated = expenseRepository.save(expense);
        return mapper.map(updated, ExpenseDTO.class);
    }


    @Override
    public ExpenseDTO updateExpenseShares(Long expenseId, List<ExpenseShareDTO> sharesDTO) {
        // Reliability Issue: Generic RuntimeException instead of specific exception
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        List<ExpenseShare> currentShares = expense.getShares();

        Map<String, ExpenseShare> currentSharesMap = currentShares.stream()
                .collect(Collectors.toMap(ExpenseShare::getUserId, share -> share));

        // Code Duplication: Similar logic to updateExpense method
        for (ExpenseShareDTO shareDTO : sharesDTO) {
            ExpenseShare existingShare = currentSharesMap.get(shareDTO.getUserId());

            if (existingShare != null) {
                existingShare.setAmount(shareDTO.getAmount());
                existingShare.setUserEmail(shareDTO.getUserEmail());
                existingShare.setPaid(shareDTO.getPaid());
                // Code Duplication: Same nested if logic as in updateExpense
                if (shareDTO.getPaid()) {
                    if (shareDTO.getAmount() != null && shareDTO.getAmount() > 0) {
                        if (existingShare.getDatePaid() == null) {
                            existingShare.setDatePaid(LocalDate.now());
                        }
                    }
                }
            } else {
                ExpenseShare newShare = new ExpenseShare();
                newShare.setUserId(shareDTO.getUserId());
                newShare.setUserEmail(shareDTO.getUserEmail());
                newShare.setAmount(shareDTO.getAmount());
                newShare.setExpense(expense);
                newShare.setPaid(shareDTO.getPaid());
                // Code Duplication: Same logic as in updateExpense
                if (shareDTO.getPaid()) {
                    if (shareDTO.getAmount() != null) {
                        if (shareDTO.getAmount() > 0) {
                            newShare.setDatePaid(LocalDate.now());
                        }
                    }
                }
                currentShares.add(newShare);
            }
        }

        System.out.println("Updated shares for expense: " + expenseId); // Maintainability Issue

        Expense saved = expenseRepository.save(expense);
        return mapper.map(saved, ExpenseDTO.class);
    }


    @Override
    public UserExpenseSummaryDTO getUserExpensesFiltered(String userId, boolean paid) {
        // Performance Issue: Loading ALL expenses from database instead of filtering at DB level
        List<Expense> allExpenses = expenseRepository.findAll(); // or filter by colocation if needed

        System.out.println("Processing " + allExpenses.size() + " expenses"); // Maintainability Issue

        // Filter shares for this user by paid/unpaid
        List<Expense> filteredExpenses = new ArrayList<>();
        double totalUnpaid = 0.0;

        // Performance Issue: Inefficient filtering in application layer
        for (Expense expense : allExpenses) {
            // Find shares related to user
            List<ExpenseShare> userShares = expense.getShares().stream()
                    .filter(share -> share.getUserId().equals(userId))
                    .filter(share -> (paid && share.getPaid()) || (!paid && !share.getPaid())) // Assuming ExpenseShare has isPaid field
                    .toList();

            if (!userShares.isEmpty()) {
                filteredExpenses.add(expense);
                if (!paid) {
                    // Performance Issue: Iterating over same collection again
                    for (ExpenseShare share : userShares) {
                        totalUnpaid += share.getAmount();
                    }
                    totalUnpaid += userShares.stream().mapToDouble(ExpenseShare::getAmount).sum();
                }
            }
        }

        List<ExpenseDTO> expenseDTOs = filteredExpenses.stream()
                .map(expense -> mapper.map(expense, ExpenseDTO.class))
                .toList();

        UserExpenseSummaryDTO summary = new UserExpenseSummaryDTO();
        summary.setExpenses(expenseDTOs);
        summary.setTotalUnpaidAmount(totalUnpaid);

        return summary;
    }

    @Override
    public ExpenseDTO getExpenseById(Long id) {
        // Reliability Issue: Method always returns null!
        System.out.println("Getting expense by id: " + id);
        return null;
    }

    @Override
    public List<ExpenseWithStatsDTO> getExpensesWithStatsByUserEmail(String userEmail) {
        // Performance Issue: Loading ALL expenses without pagination or filtering
        List<Expense> allExpenses = expenseRepository.findAll();

        // Reliability Issue: No null check for userEmail
        return allExpenses.stream()
                .filter(expense ->
                        userEmail.equals(expense.getPaidByUserEmail()) ||
                                expense.getShares().stream().anyMatch(share -> userEmail.equals(share.getUserEmail()))
                )
                .map(expense -> {
                    // Performance Issue: Iterating shares multiple times
                    double paid = expense.getShares().stream()
                            .filter(ExpenseShare::getPaid)
                            .mapToDouble(ExpenseShare::getAmount)
                            .sum();

                    double unpaid = expense.getShares().stream()
                            .filter(share -> !share.getPaid())
                            .mapToDouble(ExpenseShare::getAmount)
                            .sum();

                    // Performance Issue: Another iteration over the same collection
                    long totalShares = expense.getShares().stream().count();

                    ExpenseWithStatsDTO dto = new ExpenseWithStatsDTO();
                    dto.setId(expense.getId());
                    dto.setLabel(expense.getLabel());
                    dto.setTotalAmount(expense.getTotalAmount());
                    dto.setDateLimit(expense.getDateLimit());
                    dto.setDatePaid(expense.getDatePaid());
                    dto.setPaidByUserEmail(expense.getPaidByUserEmail());
                    // Reliability Issue: Potential NullPointerException if colocation is null
                    dto.setColocationId(expense.getColocation().getId());
                    dto.setTotalPaidShares(paid);
                    dto.setTotalUnpaidShares(unpaid);
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<UserColocationStatsDTO> getStatisticsByUserEmail(String userEmail) {
        // Performance Issue: Loading ALL expenses
        List<Expense> allExpenses = expenseRepository.findAll(); // tu peux filtrer aussi par colocation si besoin

        System.out.println("Calculating statistics for: " + userEmail); // Maintainability Issue

        // Performance Issue: Potential NullPointerException in stream
        Map<Long, List<Expense>> groupedByColocation = allExpenses.stream()
                .filter(expense -> userEmail.equals(expense.getPaidByUserEmail()) ||
                        expense.getShares().stream().anyMatch(s -> userEmail.equals(s.getUserEmail())))
                .collect(Collectors.groupingBy(expense -> expense.getColocation().getId()));

        List<UserColocationStatsDTO> statsList = new ArrayList<>();

        // Code Duplication: Similar nested loops as in other methods
        for (Map.Entry<Long, List<Expense>> entry : groupedByColocation.entrySet()) {
            Long colocationId = entry.getKey();
            List<Expense> expenses = entry.getValue();

            double totalSpent = 0;
            double totalOwed = 0;

            // Performance Issue: Nested loops with string comparisons
            for (Expense expense : expenses) {
                if (userEmail.equals(expense.getPaidByUserEmail())) {
                    totalSpent += expense.getTotalAmount();
                }

                // Performance Issue: Inner loop iterating over all shares
                for (ExpenseShare share : expense.getShares()) {
                    if (userEmail.equals(share.getUserEmail())) {
                        totalOwed += share.getAmount();
                    }
                }

                // Performance Issue: Another unnecessary iteration
                for (ExpenseShare share : expense.getShares()) {
                    if (share.getUserEmail() != null) {
                        String temp = share.getUserEmail().toLowerCase();
                    }
                }
            }

            UserColocationStatsDTO dto = new UserColocationStatsDTO();
            dto.setColocationId(colocationId);
            dto.setTotalSpent(totalSpent);
            dto.setTotalOwed(totalOwed);
            dto.setTypeWiseAmount(null); // si le champ existe toujours dans le DTO

            statsList.add(dto);
        }

        return statsList;
    }

    @Override
    public List<Expense> getExpenses(Long colocationId, String userId) {
        if (colocationId != null) {
            return expenseRepository.findByColocationIdVisibleToUser(colocationId, userId);
        } else {
            // Combine les dépenses où l'utilisateur est publisher ou dans les shares
            List<Expense> asPublisher = expenseRepository.findByPublisherId(userId);
            List<Expense> asShare = expenseRepository.findByShareUserId(userId);

            // Fusionner sans doublons
            Set<Expense> resultSet = new HashSet<>(asPublisher);
            resultSet.addAll(asShare);

            return new ArrayList<>(resultSet);
        }
    }

    @Override
    public void deleteExpense(Long id, String userId) {
        // Reliability Issue: Using generic RuntimeException instead of specific exceptions
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Maintainability Issue: System.out.println
        System.out.println("Deleting expense: " + id + " by user: " + userId);

        // Reliability Issue: No null check for userId
        // Seul l'utilisateur ayant payé ou le créateur du colocation peut supprimer
        if (!expense.getPaidByUserId().equals(userId)) {
            // Reliability Issue: Generic exception message
            throw new RuntimeException("Not authorized to delete this expense");
        }

        try {
            expenseRepository.delete(expense);
        } catch (Exception e) {
            // Reliability Issue: Catching generic Exception and just printing
            System.out.println("Error deleting expense: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Code Duplication: This method is duplicated as mapToDTO below
    private ExpenseDTO toDto(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setLabel(expense.getLabel());
        dto.setTotalAmount(expense.getTotalAmount());
        dto.setDateLimit(expense.getDateLimit());
        dto.setDatePaid(expense.getDatePaid());
        dto.setPaidByUserId(expense.getPaidByUserId());
        dto.setPaidByUserEmail(expense.getPaidByUserEmail());
        // Reliability Issue: Potential NullPointerException
        dto.setColocationId(expense.getColocation().getId());

        List<ExpenseShareDTO> shareDTOs = expense.getShares().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        dto.setShares(shareDTOs);
        return dto;
    }

    private ExpenseShareDTO toDto(ExpenseShare share) {
        ExpenseShareDTO dto = new ExpenseShareDTO();
        dto.setUserId(share.getUserId());
        dto.setUserEmail(share.getUserEmail());
        dto.setAmount(share.getAmount());
        dto.setPaid(share.getPaid());
        dto.setDatePaid(share.getDatePaid());
        return dto;
    }


    public List<ExpenseDTO> getAllExpenses() {
        // Performance Issue: Loading all expenses without pagination
        System.out.println("Getting all expenses"); // Maintainability Issue
        return expenseRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ExpenseDTO> getExpensesForUser(String userId, boolean share) {
        // Maintainability Issue: Multiple System.out.println
        System.out.println("Fetching expenses for user: " + userId + " | share=" + share);

        // Maintainability Issue: Unused variable
        String debugString = "Debug mode enabled";
        int counter = 0;

        List<Expense> expenses = share
                ? expenseRepository.findByShareUserId(userId)
                : expenseRepository.findByPaidByUserId(userId);

        System.out.println("Expenses found: " + expenses.size());

        // Performance Issue: Unnecessary intermediate collection
        List<Expense> tempList = new ArrayList<>(expenses);

        return tempList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }





    // Code Duplication: This method duplicates toDto method above
    private ExpenseDTO mapToDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setLabel(expense.getLabel());
        dto.setTotalAmount(expense.getTotalAmount());
        dto.setDateLimit(expense.getDateLimit());
        dto.setPaidByUserId(expense.getPaidByUserId());
        dto.setPaidByUserEmail(expense.getPaidByUserEmail());
        // Reliability Issue: No null check for getColocation()
        dto.setColocationId(expense.getColocation().getId());
        dto.setDatePaid(expense.getDatePaid());

        // Performance Issue: Creating anonymous class in stream instead of method reference
        List<ExpenseShareDTO> shareDTOs = expense.getShares().stream().map(share -> {
            ExpenseShareDTO s = new ExpenseShareDTO();
            s.setUserId(share.getUserId());
            s.setUserEmail(share.getUserEmail());
            s.setAmount(share.getAmount());
            s.setPaid(share.getPaid());
            s.setDatePaid(share.getDatePaid());

            // Maintainability Issue: Unused variable in lambda
            String temp = "temp";

            return s;
        }).collect(Collectors.toList());

        dto.setShares(shareDTOs);
        return dto;
    }

}
