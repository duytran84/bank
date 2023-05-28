package com.demo.bank.repositories;

import com.demo.bank.domains.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
	Optional<Account> getByAccountNumber(String accountNumber);

}
