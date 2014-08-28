CREATE TABLE address (
  id IDENTITY,
  name varchar(255) NOT NULL,
  street varchar(255),
  number varchar(16),
  extra varchar(255),
  zipcode varchar(5),
  city varchar(255),
  state varchar(255),
  country varchar(255),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE contract (
  id IDENTITY,
  name varchar(255) NOT NULL,
  contract_no varchar(255),
  customer_no varchar(255),
  address_id int(5),
  comment varchar(10000),
  startdate date,
  enddate date,
  cancelation_period_count int(4),
  cancelation_period_type int(4),
  first_min_runtime_count int(4),
  first_min_runtime_type int(4),
  next_min_runtime_count int(4),
  next_min_runtime_type int(4),
  uri varchar(4096),
  hibiscus_category varchar(255) NULL,
  ignore_cancellations int(1) NOT NULL,
  do_not_remind_before date,
  sepa_creditor varchar(35),
  sepa_customer varchar(35),
  UNIQUE (id),
  PRIMARY KEY (id),
  CONSTRAINT fk_address FOREIGN KEY (address_id) REFERENCES address (id)
);

CREATE TABLE costs (
  id IDENTITY,
  contract_id int(5),
  description varchar(255),
  money double,
  period int(1),
  UNIQUE (id),
  PRIMARY KEY (id),
  CONSTRAINT fk_costs_contract FOREIGN KEY (contract_id) REFERENCES contract (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE transactions (
  transaction_id int(5),
  contract_id int(5),
  UNIQUE (transaction_id),
  PRIMARY KEY (transaction_id),
  CONSTRAINT fk_transactions_contract FOREIGN KEY (contract_id) REFERENCES contract (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE storage (
  id IDENTITY,
  contract_id int(5),
  description varchar(255),
  path varchar(65535),
  file BLOB,
  UNIQUE (id),
  PRIMARY KEY (id),
  CONSTRAINT fk_storage_contract FOREIGN KEY (contract_id) REFERENCES contract (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE settings (
  key varchar(255) NOT NULL,
  value varchar(255),
  UNIQUE (key),
  PRIMARY KEY (key)
);

CREATE TABLE version (
  id IDENTITY,
  name varchar(255) NOT NULL,
  version int(5) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

INSERT INTO version (name,version) values ('contract_db',21);
