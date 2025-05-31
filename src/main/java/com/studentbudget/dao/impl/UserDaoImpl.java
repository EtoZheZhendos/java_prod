package com.studentbudget.dao.impl;

import com.studentbudget.dao.UserDao;
import com.studentbudget.model.User;
import com.studentbudget.model.UserRole;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {
    private final SessionFactory sessionFactory;

    public UserDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public User save(User user) {
        getCurrentSession().persist(user);
        return user;
    }

    @Override
    public User update(User user) {
        return getCurrentSession().merge(user);
    }

    @Override
    public void deleteById(Long id) {
        Query query = getCurrentSession().createQuery("delete from User where id = :id");
        query.setParameter("id", id);
        query.executeUpdate();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(getCurrentSession().get(User.class, id));
    }

    @Override
    public List<User> findAll() {
        return getCurrentSession().createQuery("from User", User.class).list();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        Query<User> query = getCurrentSession().createQuery(
            "from User where username = :username", User.class);
        query.setParameter("username", username);
        return query.uniqueResultOptional();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Query<User> query = getCurrentSession().createQuery(
            "from User where email = :email", User.class);
        query.setParameter("email", email);
        return query.uniqueResultOptional();
    }

    @Override
    public List<User> findByRole(UserRole role) {
        Query<User> query = getCurrentSession().createQuery(
            "from User where role = :role", User.class);
        query.setParameter("role", role);
        return query.list();
    }

    @Override
    public List<User> findActiveUsers() {
        return getCurrentSession().createQuery(
            "from User where active = true", User.class).list();
    }

    @Override
    public boolean existsByUsername(String username) {
        Query<Long> query = getCurrentSession().createQuery(
            "select count(u) from User u where u.username = :username", Long.class);
        query.setParameter("username", username);
        return query.uniqueResult() > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Query<Long> query = getCurrentSession().createQuery(
            "select count(u) from User u where u.email = :email", Long.class);
        query.setParameter("email", email);
        return query.uniqueResult() > 0;
    }
} 