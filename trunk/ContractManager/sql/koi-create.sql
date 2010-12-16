CREATE TABLE contract (
  id NUMERIC default UNIQUEKEY('contract'),
  name varchar(255) NOT NULL,
  contract_no varchar(255),
  customer_no varchar(255),
  address_id int(4),
  comment text,
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

CREATE TABLE umsaetze (
  umsatz_id int(4),
  contract_id int(4),
  UNIQUE (umsatz_id),
  PRIMARY KEY (umsatz_id)
);

CREATE TABLE address (
  id NUMERIC default UNIQUEKEY('address'),
  name varchar(255) NOT NULL,
  street varchar(255),
  number int(4),
  extra varchar(255),
  zipcode int(4),
  city varchar(255),
  state varchar(255),
  country varchar(255),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE settings (
  key varchar(255) NOT NULL UNIQUEKEY('settings'),
  value varchar(255),
  UNIQUE (key),
  PRIMARY KEY (key)
);
ALTER TABLE contract ADD CONSTRAINT fk_address FOREIGN KEY (address_id) REFERENCES address (id) DEFERRABLE;
ALTER TABLE umsaetze ADD CONSTRAINT fk_contract FOREIGN KEY (contract_id) REFERENCES contract (id) DEFERRABLE;
