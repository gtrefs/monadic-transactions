package de.gtrefs.transaction;

import javax.persistence.EntityManager;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Transaction<T>{
    private final Function<EntityManager, T> action;

    private Transaction(Function<EntityManager, T> action) {
        this.action = action;
    }

    static Transaction<Void> withoutResult(Consumer<EntityManager> action){
        return of(em -> {
            action.accept(em);
            return null;
        });
    }

    static <U> Transaction<U> of(U value){
        return of(em -> value);
    }

    static <U> Transaction<U> of(Function<EntityManager, U> action){

        return new Transaction<U>(action) {
            @Override U run(EntityManager entityManager) {
                try{
                    entityManager.getTransaction().begin();
                    final U result = action.apply(entityManager);
                    entityManager.getTransaction().commit();
                    return result;
                }catch (RuntimeException e){
                    entityManager.getTransaction().rollback();
                    throw e;
                }
            }
        };

    }

    <U> Transaction<U> map(Function<? super T, ? extends U> mapper){
        return of(action.andThen(mapper));
    }

    <U> Transaction<U> flatMap(Function<? super T, ? extends Transaction<U>> mapper){
        return of(em -> {
            final Transaction<U> transaction = action.andThen(mapper).apply(em);
            return transaction.action.apply(em);
        });
    }

    abstract T run(EntityManager entityManager);

    public <U> Transaction<U> flatten() {
        return Transaction.of(em -> {
            Object result = action.apply(em);
            while(result instanceof Transaction){
                result = ((Transaction) result).action.apply(em);
            }
            return (U) result;
        });
    }
}
