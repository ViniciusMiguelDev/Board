package br.com.persistence.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import com.mysql.cj.jdbc.StatementImpl;

import br.com.dto.CardDetailsDTO;
import br.com.persistence.converter.OffsetDateTimeConverter;
import br.com.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CardDAO {

    private final Connection connection;

    public CardEntity insert(final CardEntity entity) throws SQLException {
        var sql = "INSERT INTO CARDS (title, description, board_column_id) VALUES (?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var i = 1;
            statement.setString(i++, entity.getTitle());
            statement.setString(i++, entity.getDescription());
            statement.setLong(i, entity.getBoardColumn().getId());
            statement.executeUpdate();
            if (statement instanceof StatementImpl impl) {
                entity.setId(impl.getLastInsertID());
            }
        }
        return entity;
    }

    public Optional<CardDetailsDTO> findById(final Long id) throws SQLException {
        var sql = """
                SELECT
                    c.id,
                    c.title,
                    c.description,
                    b.blocked_at,
                    b.block_reason,
                    c.board_column_id,
                    bc.name,
                    (SELECT COUNT (sub_b.id) FROM BLOCKS sub_b WHERE sub_b.id = c.id) blocks_amount
                FROM CARDS c
                LEFT JOIN BLOCKS b
                    ON c.id = b.card_id
                    AND b.unblocked_at IS NOT NULL
                INNER JOIN BOARDS_COLUMNS bc
                    ON bc.id = c.board_column_id
                WHERE c.id = ?
                """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if (resultSet.next()) {
                var dto = new CardDetailsDTO(
                        resultSet.getLong("c.id"),
                        resultSet.getString("c.title"),
                        resultSet.getString("c.description"),
                        Objects.nonNull(resultSet.getString("b.block_reason")),
                        OffsetDateTimeConverter.toOffsetDateTime(resultSet.getTimestamp("b.blocked_at")),
                        resultSet.getString("b.block_reason"),
                        resultSet.getInt("blocks_amount"),
                        resultSet.getLong("c.board_column_id"),
                        resultSet.getString("bc.name"));
                return Optional.of(dto);
            }
        }
        return Optional.empty();
    }
}
