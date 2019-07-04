package de.gtrefs.transaction;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.UnaryOperator;

import static de.gtrefs.transaction.UserDatabaseModule.*;

public class UserRegister {

    private final Repository<User> userRepository;
    private final BufferedReader userInputReader;
    private final EntityManager entityManager;

    @Inject
    public UserRegister(
            EntityRepository<User> userRepository,
            BufferedReader userInputReader,
            @HSQL EntityManager entityManager) {
        this.userRepository = userRepository;
        this.userInputReader = userInputReader;
        this.entityManager = entityManager;
    }

    public void run() throws Exception {
        // runs until the user quits
        Boolean running = true;
        while (running) {
            System.out.println("Enter an option: "
                               + "1) Insert a new user. "
                               + "2) Find a user. "
                               + "3) List all users "
                               + "4) Edit a user. "
                               + "5) Delete a user. "
                               + "6) Quit the application");
            running = runUserOperation(readUserInput());
        }
    }

    private boolean runUserOperation(String option) throws Exception {
        switch (option) {
            case "1":
                persistNewUser();
                return true;
            case "2":
                fetchExistingUser();
                return true;
            case "3":
                fetchAllExistingUsers();
                return true;
            case "4":
                updateExistingUser();
                return true;
            case "5":
                removeExistingUser();
                return true;
            case "6":
                return false;
        }
        return true;
    }

    private void persistNewUser() throws IOException {
        String name = requestStringInput("the name of the user");
        String email = requestStringInput("the email of the user");
        userRepository.save(new User(name, email)).run(entityManager);
    }

    private void fetchExistingUser() throws IOException {
        int id = requestIntegerInput("the user ID");
        User user = userRepository.find(id).run(entityManager);
        System.out.println(user);
    }

    private void fetchAllExistingUsers() throws IOException {
        userRepository.findAll("de.gtrefs.transaction.User")
               .run(entityManager)
               .forEach(System.out::println);
    }

    private void updateExistingUser() throws Exception {
        int id = requestIntegerInput("the user ID");
        String name = requestStringInput("the name of the user");
        String email = requestStringInput("the email of the user");
        userRepository.update(id,
                user -> user.setName(name),
                user -> user.setEmail(email))
               .run(entityManager);
    }

    private void removeExistingUser() throws IOException {
        int id = requestIntegerInput("the user ID");
        userRepository.remove(id).run(entityManager);
    }

    private void convertExistingUser() {
        final UnaryOperator<User> clone = user -> new User(user.getName(), user.getEmail());
        userRepository.convert(10, clone).run(entityManager);
    }

    private String readUserInput() throws IOException {
        return userInputReader.readLine();
    }

    private String requestStringInput(String request) throws IOException {
        System.out.printf("Enter %s: ", request);
        return readUserInput();
    }

    private int requestIntegerInput(String request) throws IOException {
        System.out.printf("Enter %s: ", request);
        return Integer.parseInt(readUserInput());
    }

}