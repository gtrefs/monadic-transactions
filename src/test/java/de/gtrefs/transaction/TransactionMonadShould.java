package de.gtrefs.transaction;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionMonadShould {

    @Mock
    EntityManager entityManager;

    @Mock
    EntityTransaction entityTransaction;

    @Before
    public void setUp() {
        when(entityManager.getTransaction()).thenReturn(entityTransaction);
    }

    @Test
    public void composeActionsWithMapCombinator(){
        final Transaction<String> transaction = Transaction.of("hello").map(other -> other + " world");

        final String result = transaction.run(entityManager);

        assertThat(result, is("hello world"));
        verify(entityTransaction).begin();
        verify(entityTransaction).commit();
    }

    @Test
    public void composeTransactionsWithFlatMapCombinator(){
        final Transaction<String> transaction = Transaction.of("hello")
                                                           .flatMap(other -> Transaction.of(em -> other + " world"));

        final String result = transaction.run(entityManager);

        assertThat(result, is("hello world"));
        verify(entityTransaction).begin();
        verify(entityTransaction).commit();
    }

    @Test
    public void beAbleToFlattenTwoTransactionsIntoOne(){
        Transaction<Transaction<String>> twoTransactions = Transaction.of(Transaction.of("hello"));

        Transaction<String> flattened = twoTransactions.flatten();

        assertThat(flattened.run(entityManager), is(twoTransactions.run(entityManager).run(entityManager)));
    }

    @Test
    public void beAbleToFlattenMultipleTransactionsIntoOne(){
        Transaction<Transaction<Transaction<String>>> threeTransactions = Transaction.of(Transaction.of(Transaction.of("hello")));

        Transaction<String> flattened = threeTransactions.flatten();

        assertThat(flattened.run(entityManager), is(threeTransactions.run(entityManager).run(entityManager).run(entityManager)));
    }


    @Test
    public void callBeginAndCommitTransactionExactlyOnceWhenFlattened(){
        Transaction<Transaction<Transaction<String>>> threeTransactions = Transaction.of(Transaction.of(Transaction.of("hello")));

        Transaction<String> flattened = threeTransactions.flatten();
        flattened.run(entityManager);

        verify(entityTransaction).begin();
        verify(entityTransaction).commit();
    }

    @Test
    public void notFlattenATransactionWithoutNestedTransaction(){
        Transaction<String> transaction = Transaction.of("hello");

        Transaction<String> flattened = transaction.flatten();

        assertThat(flattened.run(entityManager), is(transaction.run(entityManager)));
    }

    @Test
    @Ignore("I could not find a way to express this in the type system. It fails at runtime.")
    public void notBreakBecauseOfDifferentTypes(){
        Transaction<String> transaction = Transaction.of("hello");

        Transaction<Integer> flattened = transaction.flatten();

        assertThat(flattened.run(entityManager), is(transaction.run(entityManager)));
    }

    @Test
    public void obeyAssociativeLaw(){
        // Keisli Arrwos
        // See also: FP in Scala page 196 and 197
        final Function<String, Transaction<String>> f = Transaction::of;
        final Function<String, Transaction<Integer>> g = str -> Transaction.of(str.length());
        final Function<Integer, Transaction<Long>> h = integer -> Transaction.of(Long.valueOf(integer));

        // assert compose(compose(f,g),h) == compose(f, compose(g,h)); will fail to due object identity

        final Transaction<Long> firstTransaction = compose(compose(f, g), h).apply("test");
        final Transaction<Long> secondTransaction = compose(f, compose(g, h)).apply("test");

        assertThat(firstTransaction.run(entityManager), is(secondTransaction.run(entityManager)));
        verify(entityTransaction, times(2)).begin();
        verify(entityTransaction, times(2)).commit();
    }

    private static <A,B,C> Function<A, Transaction<C>> compose(Function<A,Transaction<B>> f, Function<B, Transaction<C>> g){
        return a -> f.apply(a).flatMap(g);
    }

    @Test
    public void obeyIdentityLaw(){
        final Function<String, Transaction<String>> unit = Transaction::of;

        // left identity
        final Transaction<String> left = Transaction.of("test");
        final Transaction<String> leftIdentity = left.flatMap(unit);
        assertThat(leftIdentity.run(entityManager), is(equalTo(left.run(entityManager))));

        // right identiy
        final String value = "test";
        final Function<String, Transaction<String>> right = Transaction::of;
        final Transaction<String> rightIdentity = unit.apply(value).flatMap(right);
        assertThat(rightIdentity.run(entityManager), is(equalTo(right.apply(value).run(entityManager))));

        verify(entityTransaction, times(4)).begin();
        verify(entityTransaction, times(4)).commit();
    }

}
