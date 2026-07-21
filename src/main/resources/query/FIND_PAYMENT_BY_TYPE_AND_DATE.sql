SELECT * FROM payments.payment
WHERE type = :type
  and date = :date;
