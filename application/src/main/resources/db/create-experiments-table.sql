create table EXPERIMENTS
(USERNAME varchar(255) not null,
BENCHMARK_NAME varchar(255) not null,
EXP_NUMBER bigint not null,
PERFORMED_ON timestamp,
STATUS varchar(255) not null,
primary key(USERNAME, BENCHMARK_NAME, EXP_NUMBER))