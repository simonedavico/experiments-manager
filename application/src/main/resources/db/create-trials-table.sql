create table TRIALS
(USERNAME varchar(255) not null,
BENCHMARK_NAME varchar(255) not null,
EXP_NUMBER bigint not null,
TRIAL_NUMBER integer not null,
FABAN_RUN_ID varchar(255),
PERFORMED_ON timestamp,
STATUS varchar(255) not null,
primary key (USERNAME, BENCHMARK_NAME, EXP_NUMBER, TRIAL_NUMBER))