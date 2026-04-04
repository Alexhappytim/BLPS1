package com.blps.app.infrastructure.transaction;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.UserTransaction;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;

public class NarayanaHibernateJtaPlatform extends AbstractJtaPlatform {

    @Override
    protected jakarta.transaction.TransactionManager locateTransactionManager() {
        return TransactionManager.transactionManager();
    }

    @Override
    protected jakarta.transaction.UserTransaction locateUserTransaction() {
        return UserTransaction.userTransaction();
    }
}
