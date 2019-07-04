package de.gtrefs.transaction;

import de.gtrefs.transaction.UserDatabaseModule.EntityClass;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class EntityRepository<T> implements Repository<T>, CrudTransactions<T> {

    private Class<T> entityClass;

    @Inject
    public EntityRepository(@EntityClass Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Transaction<List<T>> findAll(String table) {
        return Transaction.of(em -> {
            final String str = "select e from table e";
            final String jpql = str.replace("table", table);
            return (List<T>) em.createQuery(jpql).getResultList();
        });
    }

    public Transaction<T> find(int id) {
        return findById(id).apply(entityClass);
    }

    public Transaction<Void> update(int id, Consumer<T>... updates) {
        return find(id).flatMap(entity -> updateEntity(entity, updates));
    }

    public Transaction<T> convert(int id, UnaryOperator<T> converter){
        return find(id).flatMap(entity -> Transaction.of(converter.apply(entity)));
    }

    public Transaction<Void> save(T entity) {
        return saveEntity(entity);
    }

    public Transaction<Void> remove(int id) {
        return find(id).flatMap(this::removeEntity);
    }
}