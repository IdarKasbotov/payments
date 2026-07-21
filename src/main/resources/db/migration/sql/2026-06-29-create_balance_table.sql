create table payments.balance (
    date    date           not null,
    type    varchar(50)    not null,
    balance decimal(10, 2) not null,
    constraint pk_balance primary key (date, type)
);

comment on table payments.balance is 'Агрегированные балансы по типу выплаты и дате';
comment on column payments.balance.date is 'Дата выплаты';
comment on column payments.balance.type is 'Тип выплаты (SALARY/BONUS/VACATION)';
comment on column payments.balance.balance is 'Суммарный баланс за дату и тип';