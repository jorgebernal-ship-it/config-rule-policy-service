-- Modelo relacional: PolicySet -> Policy -> Rule

CREATE EXTENSION IF NOT EXISTS pgcrypto; -- necesario para gen_random_uuid()

CREATE TABLE policy_set (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR NOT NULL,
  channel VARCHAR NOT NULL,
  transaction_code VARCHAR NOT NULL,
  algorithm_combination VARCHAR NOT NULL DEFAULT 'deny-overrides',
  status VARCHAR NOT NULL DEFAULT 'active',
  version INT NOT NULL DEFAULT 1,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR
);

CREATE TABLE policy (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  policy_set_id UUID NOT NULL REFERENCES policy_set(id) ON DELETE CASCADE,
  name VARCHAR NOT NULL,
  algorithm_combination_rules VARCHAR NOT NULL DEFAULT 'deny-overrides',
  sequence INT NOT NULL DEFAULT 0
);

CREATE TABLE rule (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  policy_id UUID NOT NULL REFERENCES policy(id) ON DELETE CASCADE,
  name VARCHAR NOT NULL,
  effect VARCHAR NOT NULL CHECK (effect IN ('allow','deny')),
  decision_code VARCHAR,
  target JSONB NOT NULL,
  when_attribute VARCHAR NOT NULL,
  when_operator VARCHAR NOT NULL DEFAULT 'eq',
  when_value VARCHAR NOT NULL,
  sequence INT NOT NULL DEFAULT 0
);

-- Índices para los patrones de acceso reales
CREATE INDEX idx_policyset_channel_txn ON policy_set (channel, transaction_code);
CREATE INDEX idx_policy_policyset ON policy (policy_set_id);
CREATE INDEX idx_rule_policy ON rule (policy_id);
