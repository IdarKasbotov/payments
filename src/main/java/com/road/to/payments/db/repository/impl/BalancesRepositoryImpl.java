package com.road.to.payments.db.repository.impl;

import com.road.to.payments.db.entity.BalanceEntity;
import com.road.to.payments.db.repository.BalancesRepository;
import com.road.to.payments.model.PaymentType;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class BalancesRepositoryImpl implements BalancesRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void deleteAll() {
        String query = "DELETE FROM payments.balance";
        namedParameterJdbcTemplate.update(query, Map.of());
    }

    public void save(List<BalanceEntity> balances) {
        String query = "INSERT INTO payments.balance (date, type, balance) VALUES (:date, :type, :balance)";
        namedParameterJdbcTemplate.batchUpdate(query, SqlParameterSourceUtils.createBatch(balances));
    }

    @Override
    public Optional<BigDecimal> findBalanceByDateAndType(PaymentType type, LocalDate date) {
        String query = "SELECT balance FROM payments.balance WHERE date = :date AND type = :type";

        try {
            BigDecimal balance = namedParameterJdbcTemplate.queryForObject(
                    query,
                    new MapSqlParameterSource()
                            .addValue("date", date)
                            .addValue("type", type.name()),
                    BigDecimal.class
            );
            return Optional.ofNullable(balance);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
