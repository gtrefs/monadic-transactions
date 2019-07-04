package de.gtrefs.transaction;

import java.util.function.Consumer;
import java.util.List;
import java.util.function.UnaryOperator;

public interface Repository<T> {

    Transaction<T> find(int id);

    Transaction<List<T>> findAll(String table);

    Transaction<Void> update(int id, Consumer<T>... updates) throws Exception;

    Transaction<Void> save(T entity);

    Transaction<Void> remove(int id);

    Transaction<T> convert(int id, UnaryOperator<T> converter);

}