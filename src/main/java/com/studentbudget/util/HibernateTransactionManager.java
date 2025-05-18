package com.studentbudget.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class HibernateTransactionManager {
    private final SessionFactory sessionFactory;
    private final ThreadLocal<Integer> transactionCount = new ThreadLocal<>();

    public HibernateTransactionManager(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private void incrementTransactionCount() {
        Integer count = transactionCount.get();
        transactionCount.set(count == null ? 1 : count + 1);
    }

    private void decrementTransactionCount() {
        Integer count = transactionCount.get();
        if (count != null) {
            if (count == 1) {
                transactionCount.remove();
            } else {
                transactionCount.set(count - 1);
            }
        }
    }

    private boolean isTransactionActive() {
        return transactionCount.get() != null;
    }

    public <T> T executeInTransaction(TransactionCallback<T> callback) {
        Session session = sessionFactory.getCurrentSession();
        boolean isOuterTransaction = !isTransactionActive();
        Transaction transaction = null;

        try {
            if (isOuterTransaction) {
                transaction = session.beginTransaction();
            }
            incrementTransactionCount();

            T result = callback.execute(session);

            if (isOuterTransaction) {
                transaction.commit();
            }
            return result;
        } catch (Exception e) {
            if (isOuterTransaction && transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            decrementTransactionCount();
        }
    }

    public void executeInTransactionWithoutResult(VoidTransactionCallback callback) {
        Session session = sessionFactory.getCurrentSession();
        boolean isOuterTransaction = !isTransactionActive();
        Transaction transaction = null;

        try {
            if (isOuterTransaction) {
                transaction = session.beginTransaction();
            }
            incrementTransactionCount();

            callback.execute(session);

            if (isOuterTransaction) {
                transaction.commit();
            }
        } catch (Exception e) {
            if (isOuterTransaction && transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            decrementTransactionCount();
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Session session);
    }

    @FunctionalInterface
    public interface VoidTransactionCallback {
        void execute(Session session);
    }
} 