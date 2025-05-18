package com.studentbudget.dao.impl;

import com.studentbudget.dao.CategoryDao;
import com.studentbudget.model.Category;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;

public class CategoryDaoImpl implements CategoryDao {
    private final SessionFactory sessionFactory;

    public CategoryDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Category save(Category category) {
        getCurrentSession().persist(category);
        return category;
    }

    @Override
    public Category update(Category category) {
        return getCurrentSession().merge(category);
    }

    @Override
    public void deleteById(Long id) {
        Query query = getCurrentSession().createQuery("delete from Category where id = :id");
        query.setParameter("id", id);
        query.executeUpdate();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(getCurrentSession().get(Category.class, id));
    }

    @Override
    public List<Category> findAll() {
        return getCurrentSession().createQuery("from Category", Category.class).list();
    }

    @Override
    public Category findByName(String name) {
        Query<Category> query = getCurrentSession().createQuery(
            "from Category where name = :name", Category.class);
        query.setParameter("name", name);
        return query.uniqueResult();
    }
} 