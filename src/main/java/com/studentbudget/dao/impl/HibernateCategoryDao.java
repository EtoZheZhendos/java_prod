package com.studentbudget.dao.impl;

import com.studentbudget.dao.CategoryDao;
import com.studentbudget.model.Category;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class HibernateCategoryDao implements CategoryDao {
    private final SessionFactory sessionFactory;

    public HibernateCategoryDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Category save(Category category) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(category);
        return category;
    }

    @Override
    public Category update(Category category) {
        Session session = sessionFactory.getCurrentSession();
        return (Category) session.merge(category);
    }

    @Override
    public void deleteById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Category category = session.get(Category.class, id);
        if (category != null) {
            session.remove(category);
        }
    }

    @Override
    public Optional<Category> findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return Optional.ofNullable(session.get(Category.class, id));
    }

    @Override
    public List<Category> findAll() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("from Category", Category.class).list();
    }

    @Override
    public Category findByName(String name) {
        Session session = sessionFactory.getCurrentSession();
        Query<Category> query = session.createQuery(
            "from Category where name = :name", 
            Category.class
        );
        query.setParameter("name", name);
        return query.uniqueResult();
    }
} 