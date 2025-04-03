package br.com;

import br.com.persistence.config.ConnectionConfig;
import br.com.persistence.migration.MigrationStrategy;
import br.com.ui.MainMenu;

public class Main {
    public static void main(String[] args) throws Exception {
        try (var connection = ConnectionConfig.getConnection()) {
            new MigrationStrategy(connection).executeMigration();
        }
        new MainMenu().execute();
    }
}