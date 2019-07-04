package de.gtrefs.transaction;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public interface CrudTransactions<T> {

    default Transaction<Void> saveEntity(T entity) {
        return transactional(em -> em.persist(entity));
    }

    default Transaction<Void> removeEntity(T entity) {
        return transactional(em -> em.remove(entity));
    }

    default Transaction<Void> updateEntity(T entity, Consumer<T>... updates) {
        return transactional(em -> Arrays.stream(updates).forEach(up -> up.accept(entity)));
    }
    
    default Transaction<Void> transactional(Consumer<EntityManager> action){
        return Transaction.withoutResult(action);
    }

    default Function<Class<T>, Transaction<T>> findById(int id) {
        return clazz -> Transaction.of(em -> em.find(clazz, id));
    }
}
