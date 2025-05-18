package com.studentbudget.dao.impl;

import com.studentbudget.dao.TransactionDao;
import com.studentbudget.model.Transaction;
import com.studentbudget.model.TransactionType;
import com.studentbudget.model.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class HibernateTransactionDao implements TransactionDao {
    private final SessionFactory sessionFactory;

    public HibernateTransactionDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Transaction save(Transaction entity) {
        getSession().persist(entity);
        return entity;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(getSession().get(Transaction.class, id));
    }

    @Override
    public List<Transaction> findAll() {
        return getSession().createQuery("from Transaction", Transaction.class).list();
    }

    @Override
    public void delete(Transaction entity) {
        getSession().remove(entity);
    }

    @Override
    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    public Transaction update(Transaction entity) {
        return getSession().merge(entity);
    }

    @Override
    public List<Transaction> findByType(TransactionType type) {
        TypedQuery<Transaction> query = getSession().createQuery(
            "from Transaction t where t.type = :type", Transaction.class);
        query.setParameter("type", type);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findByCategory(Category category) {
        TypedQuery<Transaction> query = getSession().createQuery(
            "from Transaction t where t.category = :category", Transaction.class);
        query.setParameter("category", category);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findByDateRange(LocalDateTime start, LocalDateTime end) {
        TypedQuery<Transaction> query = getSession().createQuery(
            "from Transaction t where t.date between :start and :end", Transaction.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findByStatus(String status) {
        TypedQuery<Transaction> query = getSession().createQuery(
            "from Transaction t where t.status = :status", Transaction.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    public List<Transaction> searchByDescription(String searchTerm) {
        TypedQuery<Transaction> query = getSession().createQuery(
            "from Transaction t where lower(t.description) like lower(:searchTerm)", Transaction.class);
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        return query.getResultList();
    }
} 