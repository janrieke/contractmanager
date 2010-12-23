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
  money_once double,
  money_per_day double,
  money_per_week double,
  money_per_month double,
  money_per_year double,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE transactions (
  transaction_id int(5),
  contract_id int(5),
  UNIQUE (transaction_id),
  PRIMARY KEY (transaction_id)
);

CREATE TABLE address (
  id IDENTITY,
  name varchar(255) NOT NULL,
  street varchar(255),
  number int(5),
  extra varchar(255),
  zipcode int(5),
  city varchar(255),
  state varchar(255),
  country varchar(255),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE settings (
  key varchar(255) NOT NULL,
  value varchar(255),
  UNIQUE (key),
  PRIMARY KEY (key)
);

ALTER TABLE contract ADD CONSTRAINT fk_address FOREIGN KEY (address_id) REFERENCES address (id) DEFERRABLE;
ALTER TABLE umsaetze ADD CONSTRAINT fk_contract FOREIGN KEY (contract_id) REFERENCES contract (id) DEFERRABLE;
