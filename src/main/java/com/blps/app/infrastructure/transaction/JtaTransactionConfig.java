package com.blps.app.infrastructure.transaction;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

@Configuration
@EnableTransactionManagement
public class JtaTransactionConfig {

	@Bean
	public UserTransaction userTransaction() {
		return com.arjuna.ats.jta.UserTransaction.userTransaction();
	}

	@Bean
	public TransactionManager narayanaTransactionManager() {
		return com.arjuna.ats.jta.TransactionManager.transactionManager();
	}

	@Bean
	public PlatformTransactionManager transactionManager(UserTransaction userTransaction,
														 TransactionManager transactionManager) {
		return new JtaTransactionManager(userTransaction, transactionManager);
	}
}
