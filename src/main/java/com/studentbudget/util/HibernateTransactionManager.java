package com.studentbudget.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class HibernateTransactionManager {
    private final SessionFactory sessionFactory;

    public HibernateTransactionManager(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public <T> T executeInTransaction(TransactionCallback<T> callback) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            T result = callback.execute(session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public void executeInTransactionWithoutResult(VoidTransactionCallback callback) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            callback.execute(session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
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