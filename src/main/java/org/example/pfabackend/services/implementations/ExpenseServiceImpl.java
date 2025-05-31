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

    @Override
    public ExpenseDTO createExpense(ExpenseDTO dto) {
        Expense expense = new Expense();
        expense.setLabel(dto.getLabel());
        expense.setTotalAmount(dto.getTotalAmount());
        expense.setDateLimit(dto.getDateLimit());
        expense.setPaidByUserId(dto.getPaidByUserId());
        expense.setPaidByUserEmail(dto.getPaidByUserEmail()); // <-- ajouté ici
        expense.setColocation(colocationRepository.findById(dto.getColocationId()).orElseThrow());

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
        List<Expense> expenses = expenseRepository.findByColocationId(colocationId);

        Map<String, Double> paid = new HashMap<>();
        Map<String, Double> owes = new HashMap<>();

        for (Expense expense : expenses) {
            String paidBy = expense.getPaidByUserId();
            paid.put(paidBy, paid.getOrDefault(paidBy, 0.0) + expense.getTotalAmount());

            for (ExpenseShare share : expense.getShares()) {
                String userId = share.getUserId();
                owes.put(userId, owes.getOrDefault(userId, 0.0) + share.getAmount());
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
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NoSuchElementException("Expense not found"));

        expense.setLabel(dto.getLabel());
        expense.setTotalAmount(dto.getTotalAmount());
        expense.setDateLimit(dto.getDateLimit());
        expense.setPaidByUserId(dto.getPaidByUserId());
        expense.setPaidByUserEmail(dto.getPaidByUserEmail());
        expense.setColocation(colocationRepository.findById(dto.getColocationId()).orElseThrow());

        // Map des shares actuelles par userId pour un accès rapide
        Map<String, ExpenseShare> currentSharesMap = expense.getShares().stream()
                .collect(Collectors.toMap(ExpenseShare::getUserId, share -> share));

        Set<String> incomingUserIds = dto.getShares().stream()
                .map(ExpenseShareDTO::getUserId)
                .collect(Collectors.toSet());

        // Mise à jour ou ajout des shares provenant du DTO
        for (ExpenseShareDTO shareDTO : dto.getShares()) {
            ExpenseShare existingShare = currentSharesMap.get(shareDTO.getUserId());
            if (existingShare != null) {
                existingShare.setUserEmail(shareDTO.getUserEmail());
                existingShare.setAmount(shareDTO.getAmount());
                existingShare.setPaid(shareDTO.getPaid());
                if (shareDTO.getPaid()) {
                    existingShare.setDatePaid(LocalDate.now());
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
                    newShare.setDatePaid(LocalDate.now());
                }
                newShare.setExpense(expense);
                expense.getShares().add(newShare);
            }
        }


        Expense updated = expenseRepository.save(expense);
        return mapper.map(updated, ExpenseDTO.class);
    }


    @Override
    public ExpenseDTO updateExpenseShares(Long expenseId, List<ExpenseShareDTO> sharesDTO) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        List<ExpenseShare> currentShares = expense.getShares();

        Map<String, ExpenseShare> currentSharesMap = currentShares.stream()
                .collect(Collectors.toMap(ExpenseShare::getUserId, share -> share));

        for (ExpenseShareDTO shareDTO : sharesDTO) {
            ExpenseShare existingShare = currentSharesMap.get(shareDTO.getUserId());

            if (existingShare != null) {
                existingShare.setAmount(shareDTO.getAmount());
                existingShare.setUserEmail(shareDTO.getUserEmail());
                existingShare.setPaid(shareDTO.getPaid());
            } else {
                ExpenseShare newShare = new ExpenseShare();
                newShare.setUserId(shareDTO.getUserId());
                newShare.setUserEmail(shareDTO.getUserEmail());
                newShare.setAmount(shareDTO.getAmount());
                newShare.setExpense(expense);
                newShare.setPaid(shareDTO.getPaid());
                currentShares.add(newShare);
            }
        }



        Expense saved = expenseRepository.save(expense);
        return mapper.map(saved, ExpenseDTO.class);
    }


    @Override
    public UserExpenseSummaryDTO getUserExpensesFiltered(String userId, boolean paid) {
        List<Expense> allExpenses = expenseRepository.findAll(); // or filter by colocation if needed

        // Filter shares for this user by paid/unpaid
        List<Expense> filteredExpenses = new ArrayList<>();
        double totalUnpaid = 0.0;

        for (Expense expense : allExpenses) {
            // Find shares related to user
            List<ExpenseShare> userShares = expense.getShares().stream()
                    .filter(share -> share.getUserId().equals(userId))
                    .filter(share -> (paid && share.getPaid()) || (!paid && !share.getPaid())) // Assuming ExpenseShare has isPaid field
                    .toList();

            if (!userShares.isEmpty()) {
                filteredExpenses.add(expense);
                if (!paid) {
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
        return null;
    }

    @Override
    public List<ExpenseWithStatsDTO> getExpensesWithStatsByUserEmail(String userEmail) {
        List<Expense> allExpenses = expenseRepository.findAll();

        return allExpenses.stream()
                .filter(expense ->
                        userEmail.equals(expense.getPaidByUserEmail()) ||
                                expense.getShares().stream().anyMatch(share -> userEmail.equals(share.getUserEmail()))
                )
                .map(expense -> {
                    double paid = expense.getShares().stream()
                            .filter(ExpenseShare::getPaid)
                            .mapToDouble(ExpenseShare::getAmount)
                            .sum();

                    double unpaid = expense.getShares().stream()
                            .filter(share -> !share.getPaid())
                            .mapToDouble(ExpenseShare::getAmount)
                            .sum();

                    ExpenseWithStatsDTO dto = new ExpenseWithStatsDTO();
                    dto.setId(expense.getId());
                    dto.setLabel(expense.getLabel());
                    dto.setTotalAmount(expense.getTotalAmount());
                    dto.setDateLimit(expense.getDateLimit());
                    dto.setDatePaid(expense.getDatePaid());
                    dto.setPaidByUserEmail(expense.getPaidByUserEmail());
                    dto.setColocationId(expense.getColocation().getId());
                    dto.setTotalPaidShares(paid);
                    dto.setTotalUnpaidShares(unpaid);
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<UserColocationStatsDTO> getStatisticsByUserEmail(String userEmail) {
        List<Expense> allExpenses = expenseRepository.findAll(); // tu peux filtrer aussi par colocation si besoin

        Map<Long, List<Expense>> groupedByColocation = allExpenses.stream()
                .filter(expense -> userEmail.equals(expense.getPaidByUserEmail()) ||
                        expense.getShares().stream().anyMatch(s -> userEmail.equals(s.getUserEmail())))
                .collect(Collectors.groupingBy(expense -> expense.getColocation().getId()));

        List<UserColocationStatsDTO> statsList = new ArrayList<>();

        for (Map.Entry<Long, List<Expense>> entry : groupedByColocation.entrySet()) {
            Long colocationId = entry.getKey();
            List<Expense> expenses = entry.getValue();

            double totalSpent = 0;
            double totalOwed = 0;

            for (Expense expense : expenses) {
                if (userEmail.equals(expense.getPaidByUserEmail())) {
                    totalSpent += expense.getTotalAmount();
                }

                for (ExpenseShare share : expense.getShares()) {
                    if (userEmail.equals(share.getUserEmail())) {
                        totalOwed += share.getAmount();
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
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Seul l'utilisateur ayant payé ou le créateur du colocation peut supprimer
        if (!expense.getPaidByUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this expense");
        }

        expenseRepository.delete(expense);
    }


    private ExpenseDTO toDto(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setLabel(expense.getLabel());
        dto.setTotalAmount(expense.getTotalAmount());
        dto.setDateLimit(expense.getDateLimit());
        dto.setDatePaid(expense.getDatePaid());
        dto.setPaidByUserId(expense.getPaidByUserId());
        dto.setPaidByUserEmail(expense.getPaidByUserEmail());
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
        return expenseRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ExpenseDTO> getExpensesForUser(String userId, boolean share) {
        System.out.println("Fetching expenses for user: " + userId + " | share=" + share);

        List<Expense> expenses = share
                ? expenseRepository.findByShareUserId(userId)
                : expenseRepository.findByPaidByUserId(userId);

        System.out.println("Expenses found: " + expenses.size());
        return expenses.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }





    private ExpenseDTO mapToDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setLabel(expense.getLabel());
        dto.setTotalAmount(expense.getTotalAmount());
        dto.setDateLimit(expense.getDateLimit());
        dto.setPaidByUserId(expense.getPaidByUserId());
        dto.setPaidByUserEmail(expense.getPaidByUserEmail());
        dto.setColocationId(expense.getColocation().getId());
        dto.setDatePaid(expense.getDatePaid());

        List<ExpenseShareDTO> shareDTOs = expense.getShares().stream().map(share -> {
            ExpenseShareDTO s = new ExpenseShareDTO();
            s.setUserId(share.getUserId());
            s.setUserEmail(share.getUserEmail());
            s.setAmount(share.getAmount());
            s.setPaid(share.getPaid());
            s.setDatePaid(share.getDatePaid());
            return s;
        }).collect(Collectors.toList());

        dto.setShares(shareDTOs);
        return dto;
    }

}
