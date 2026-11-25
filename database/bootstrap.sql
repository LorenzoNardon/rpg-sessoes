SELECT 'CREATE ROLE rpg_app LOGIN PASSWORD ''rpg_app'''
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'rpg_app')\gexec

SELECT 'CREATE DATABASE rpg_sessoes OWNER rpg_app'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rpg_sessoes')\gexec
