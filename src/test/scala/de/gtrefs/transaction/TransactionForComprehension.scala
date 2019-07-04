package de.gtrefs.transaction

import java.util.function.Consumer
import javax.persistence.{EntityManager, EntityTransaction, Persistence}

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, Matchers}

class TransactionForComprehension extends FunSpec with Matchers with MockitoSugar {

  describe("A transaction"){
    val entityManager = mock[EntityManager]
    val entityTransaction = mock[EntityTransaction]
    when(entityManager.getTransaction) thenReturn entityTransaction

    it("should be composable with for comprehension"){
      val transaction:Transaction[String] = for {
         t1 <- Transaction.of("hello ")
         t2 <- Transaction.of(t1 + "world")
      } yield t2

      transaction.run(entityManager).shouldBe("hello world")
    }
  }

  describe("Combination of crud methods"){

    it("should combine findById with update"){
      val user = new User("Test", "test")
      val findAndUpdate = new CrudTransactions[User]{
        def apply(id:Int, clazz:Class[User], updates:Consumer[User]): Transaction[Unit] = for {
          entity <- findById(id)(clazz)
        } yield updateEntity(entity, updates)
      }

      withEntityManager(em => {
        em.persist(user)
        findAndUpdate(user.getId, classOf[User], user => user.setEmail("other Mail")).run(em)
      })

    }
  }

  def withEntityManager(testCode: EntityManager => Any) {
    val entityManager = Persistence.createEntityManagerFactory("example").createEntityManager
    try {
      testCode(entityManager)
    } finally entityManager.close()
  }
}
