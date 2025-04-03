package br.com.persistence.migration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;

import br.com.persistence.config.ConnectionConfig;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CommandExecutionException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MigrationStrategy {
    @SuppressWarnings("unused")
    private final Connection connection;

    public void executeMigration() throws Exception {
        var originalOut = System.out;
        var originalError = System.err;

        try {
            try (var fos = new FileOutputStream("liquibase.log")) {
                System.setOut(new PrintStream(fos));
                System.setErr(new PrintStream(fos));
                try (Connection connection = ConnectionConfig.getConnection()) {
                    Database database = DatabaseFactory.getInstance()
                            .findCorrectDatabaseImplementation(new JdbcConnection(connection));

                    Scope.child(Scope.Attr.resourceAccessor, new ClassLoaderResourceAccessor(), () -> {
                        new CommandScope("update")
                                .addArgumentValue("database", database)
                                .addArgumentValue("changelogFile", "db/changelog/db.changelog-master.yml")

                                .execute();
                    });

                } catch (SQLException | CommandExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            System.setOut(originalOut);
            System.setErr(originalError);
        }
    }

}
