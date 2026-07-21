create table payments.payment (
    id   bigserial      primary key,
    sum  decimal(10, 2) not null,
    type varchar(50)    not null,
    date date           not null
);

create index payment__type_date__idx on payments.payment (type, date);

comment on table payments.payment is 'Выплаты сотрудникам';
comment on column payments.payment.id is 'Идентификатор выплаты';
comment on column payments.payment.sum is 'Сумма выплаты';
comment on column payments.payment.type is 'Тип выплаты (SALARY/BONUS/VACATION)';
comment on column payments.payment.date is 'Дата выплаты';