package com.dws.challenge.service;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;

@Service
public class AccountsService {

  @Autowired
  private NotificationService notificationService;

  // Assume AccountRepository is a Spring Data JPA repository for managing accounts
  @Autowired
  private AccountsRepository accountRepository;


  @Transactional
  public synchronized void transferMoney(long accountFromId, long accountToId, double amount) {
    // Check if amount is positive
    if (amount <= 0) {
      throw new IllegalArgumentException("Transfer amount must be positive");
    }

    // Retrieve source and destination accounts
    Account accountFrom = accountRepository.findById(accountFromId).orElseThrow(() -> new IllegalArgumentException("Account not found"));
    Account accountTo = accountRepository.findById(accountToId).orElseThrow(() -> new IllegalArgumentException("Account not found"));

    // Check if source account has sufficient balance
    if (accountFrom.getBalance().doubleValue() < amount) {
      throw new IllegalArgumentException("Insufficient balance for transfer");
    }

    // Perform transfer
    accountFrom.setBalance(new BigDecimal(accountFrom.getBalance().doubleValue() - amount));
    accountTo.setBalance(new BigDecimal(accountTo.getBalance().doubleValue() + amount));

    // Save updated account balances
    accountRepository.save(accountFrom);
    accountRepository.save(accountTo);

    // Send notifications
    notificationService.notifyAboutTransfer(accountFrom, "Transfer of " + amount + " to account " + accountToId);
    notificationService.notifyAboutTransfer(accountTo, "Received " + amount + " from account " + accountFromId);
  }
}
