package de.gtrefs.transaction;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class Main {

    public static void main(String[] args) throws Exception {
        try (WeldContainer container = new Weld().initialize()) {
            UserRegister userApplication = container.instance()
                    .select(UserRegister.class)
                    .get();
            userApplication.run();
        }
    }

}