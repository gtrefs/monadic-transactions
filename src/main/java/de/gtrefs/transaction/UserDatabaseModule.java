package de.gtrefs.transaction;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Qualifier;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface UserDatabaseModule {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.METHOD,
              ElementType.TYPE, ElementType.PARAMETER})
    @interface HSQL {}

    class EntityManagerProducer {

        @Produces
        @HSQL
        public EntityManager createEntityManager() {
            return Persistence
                    .createEntityManagerFactory("example")
                    .createEntityManager();
        }

        public void close(
                @Disposes @HSQL EntityManager entityManager) {
            entityManager.close();
        }
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD,
             ElementType.TYPE, ElementType.PARAMETER})
    @interface EntityClass {}

    class UserClassProducer {

        @Produces
        @EntityClass
        public Class createUserClass() {
            return User.class;
        }

    }

    class BufferedReaderProducer {

        @Produces
        BufferedReader getBufferedReader() {
            return new BufferedReader(new InputStreamReader(System.in));
        }

    }
}
