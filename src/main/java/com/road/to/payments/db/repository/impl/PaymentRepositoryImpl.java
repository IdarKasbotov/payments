package com.road.to.payments.db.repository.impl;

import com.road.to.payments.db.entity.PaymentEntity;
import com.road.to.payments.db.repository.PaymentRepository;
import com.road.to.payments.model.PaymentType;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;


@Repository
@AllArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    //    private final JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<PaymentEntity> paymentRowMapper =
            BeanPropertyRowMapper.newInstance(PaymentEntity.class);

    @Override
    public List<PaymentEntity> findPaymentByTypeAndDate(PaymentType type, LocalDate date) {
        String query = "SELECT * FROM payments.payment WHERE type = :type AND date = :date";
        return jdbcTemplate.query(
                query, // TODO Переделать на QueryHolder
//                "/query/FIND_PAYMENT_BY_TYPE_AND_DATE.sql",
                new MapSqlParameterSource()
                        .addValue("type", type, Types.VARCHAR)
                        .addValue("date", java.sql.Date.valueOf(date), Types.DATE),
                paymentRowMapper
        );
    }

    @Override
    public void updatePaymentSum(Long id, BigDecimal sum) {
        String query = "UPDATE payments.payment SET sum = :sum WHERE id = :id";
        jdbcTemplate.update(
                query,
                new MapSqlParameterSource()
                        .addValue("sum", sum, Types.DECIMAL)
                        .addValue("id", id, Types.BIGINT)
        );
    }

    @Override
    public void save(PaymentEntity payment) {
        String query = "INSERT INTO payments.payment (sum, type, date) VALUES (:sum, :type, :date)";
        jdbcTemplate.update(
                query,
                new MapSqlParameterSource()
                        .addValue("sum", payment.getSum(), Types.DOUBLE)
                        .addValue("type", payment.getType(), Types.VARCHAR)
                        .addValue("date", java.sql.Date.valueOf(payment.getDate()), Types.DATE)
        );
    }

    @Override
    public List<PaymentEntity> paymentsGroupedByTypeAndDate() {
        String query = """
                SELECT
                    p.date,
                    p.type,
                    SUM(p.sum)
                FROM payments.payment p
                GROUP BY p.date, p.type
                """;
        return jdbcTemplate.query(query, paymentRowMapper);
    }

//    @Query("""
//    SELECT new com.road.to.payments.db.entity.BalanceEntity(
//        p.date,
//        p.type,
//        SUM(p.sum)
//    )
//    FROM PaymentEntity p
//    GROUP BY p.date, p.type
//    ORDER BY p.date, p.type
//    """)
//    List<BalanceEntity> findBalancesGroupedByTypeAndDate();

}
