package com.studentbudget.dao.impl;

import com.studentbudget.dao.TransactionDao;
import com.studentbudget.model.Transaction;
import com.studentbudget.model.Category;
import com.studentbudget.model.TransactionType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class HibernateTransactionDao implements TransactionDao {
    private final SessionFactory sessionFactory;

    public HibernateTransactionDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Transaction save(Transaction transaction) {
        getCurrentSession().persist(transaction);
        return transaction;
    }

    @Override
    public Transaction update(Transaction transaction) {
        return getCurrentSession().merge(transaction);
    }

    @Override
    public void deleteById(Long id) {
        Query query = getCurrentSession().createQuery("delete from Transaction where id = :id");
        query.setParameter("id", id);
        query.executeUpdate();
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(getCurrentSession().get(Transaction.class, id));
    }

    @Override
    public List<Transaction> findAll() {
        return getCurrentSession().createQuery("from Transaction", Transaction.class).list();
    }

    @Override
    public List<Transaction> findByType(TransactionType type) {
        Query<Transaction> query = getCurrentSession().createQuery(
            "from Transaction where type = :type", Transaction.class);
        query.setParameter("type", type);
        return query.list();
    }

    @Override
    public List<Transaction> findByCategory(Category category) {
        Query<Transaction> query = getCurrentSession().createQuery(
            "from Transaction where category = :category", Transaction.class);
        query.setParameter("category", category);
        return query.list();
    }

    @Override
    public List<Transaction> findByDateRange(LocalDateTime start, LocalDateTime end) {
        Query<Transaction> query = getCurrentSession().createQuery(
            "from Transaction where date between :start and :end", Transaction.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.list();
    }

    @Override
    public List<Transaction> searchByDescription(String searchTerm) {
        Query<Transaction> query = getCurrentSession().createQuery(
            "from Transaction where lower(description) like lower(:searchTerm)", Transaction.class);
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        return query.list();
    }

    @Override
    public List<Transaction> findByStatus(String status) {
        Query<Transaction> query = getCurrentSession().createQuery(
            "from Transaction where status = :status", Transaction.class);
        query.setParameter("status", status);
        return query.list();
    }
} 