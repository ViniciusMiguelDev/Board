package br.com.ui;

import java.sql.SQLException;
import java.util.Scanner;

import br.com.persistence.config.ConnectionConfig;
import br.com.persistence.entity.BoardColumnEntity;
import br.com.persistence.entity.BoardEntity;
import br.com.persistence.entity.CardEntity;
import br.com.service.BoardColumnQueryService;
import br.com.service.BoardQueryService;
import br.com.service.CardQueryService;
import br.com.service.CardService;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BoardMenu {

    Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    private final BoardEntity entity;

    public void execute() throws SQLException {
        System.out.printf("Bem vindo ao board %s, selecione a operação desejada\n", entity.getId());
        var option = -1;
        while (true) {
            System.out.println("1 - Criar um card");
            System.out.println("2 - Mover um card");
            System.out.println("3 - Bloquear um card");
            System.out.println("4 - Desbloquear um card");
            System.out.println("5 - Cancelar um card");

            System.out.println("6 - Ver board");
            System.out.println("7 - Ver coluna com cards");
            System.out.println("8 - Ver card");

            System.out.println("9 - Voltar para o menu anterior");
            System.out.println("10 - Sair");
            option = scanner.nextInt();
            switch (option) {
                case 1 -> createCard();
                case 2 -> moveCardToNextColumn();
                case 3 -> blockCard();
                case 4 -> unblockCard();
                case 5 -> cancelCard();
                case 6 -> showBoard();
                case 7 -> showColumn();
                case 8 -> showCard();
                case 9 -> System.out.println("Voltando para o menu anterior");
                case 10 -> System.exit(0);

                default -> System.out.println("Opção inválida, tente novamente");
            }

            scanner.close();
        }
    }

    private void createCard() throws SQLException {
        var card = new CardEntity();
        System.out.println("Informe o titulo do card");
        card.setTitle(scanner.next());

        System.out.println("Informe a descrição do card");
        card.setDescription(scanner.next());

        card.setBoardColumn(entity.getInitialColumn());

        try (var connection = ConnectionConfig.getConnection()) {
            new CardService(connection).insert(card);
        }
    }

    private void moveCardToNextColumn() throws SQLException {
        System.out.println("Informe o id do card que deseja mover para a próxima coluna");
        var cardId = scanner.nextLong();
    }

    private void blockCard() throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'blockCard'");
    }

    private void unblockCard() throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unblockCard'");
    }

    private void cancelCard() throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelCard'");
    }

    private void showBoard() throws SQLException {
        try (var connection = ConnectionConfig.getConnection()) {
            var optional = new BoardQueryService(connection).showBoardDetails(entity.getId());
            optional.ifPresent(b -> {
                System.out.printf("Board [%s, %s] \n", b.id(), b.name());
                b.columns().forEach(c -> {
                    System.out.printf("Coluna [%s] tipo: [%s] tem %s cards\n", c.name(), c.kind(), c.cardsAmount());
                });
            });

        }
    }

    private void showColumn() throws SQLException {
        var columnsIds = entity.getBoardColumns().stream().map(BoardColumnEntity::getId).toList();
        var selectedColumn = -1L;
        while (!columnsIds.contains(selectedColumn)) {
            System.out.printf("Escolha uma coluna do board %s\n", entity.getName());
            entity.getBoardColumns()
                    .forEach(c -> System.out.printf("%s - %s [%s]\n", c.getId(), c.getName(), c.getKind()));
            selectedColumn = scanner.nextLong();
        }
        try (var connection = ConnectionConfig.getConnection()) {
            var column = new BoardColumnQueryService(connection).findById(selectedColumn);
            column.ifPresent(co -> {
                System.out.printf("Coluna %s tipo %s\n", co.getName(), co.getKind());
                co.getCards()
                        .forEach(ca -> System.out.printf("Card %s - %s\nDescrição: %s\n", ca.getId(), ca.getTitle(),
                                ca.getDescription()));

            });
        }
    }

    private void showCard() throws SQLException {
        System.out.println("Informe o id do card que deseja visualizar");
        var selectedCardId = scanner.nextLong();
        try (var connection = ConnectionConfig.getConnection()) {
            new CardQueryService(connection).findById(selectedCardId).ifPresentOrElse(c -> {
                System.out.printf("Card %s - %s.\n", c.id(), c.title());
                System.out.printf("Descrição %s\n", c.description());
                System.out.printf(c.blocked() ? "Está bloqueado. Motivo %s" : "Não está bloquado\n", c.blockReason());
                System.out.printf("Já foi bloqueado %s vezes.\n", c.blocksAmount());
                System.out.printf("Está no momento na coluna %s - %s", c.columnId(), c.columnName());
            }, () -> System.out.printf("Não existe um card com o id %s\n", selectedCardId));
        }
    }

}
