-- V1__Initial_Schema.sql
-- This script creates the initial database schema for the risk warning system.

-- =================================================================
-- Table for Company Information
-- =================================================================
CREATE TABLE company_info (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(512),
    major_product1 VARCHAR(512),
    major_product2 VARCHAR(512),
    main_products_summary TEXT,
    related_products TEXT,
    industry VARCHAR(512),
    company_type VARCHAR(255),
    is_diversified VARCHAR(255),
    is_well_known VARCHAR(255),
    company_size VARCHAR(255),
    employee_count VARCHAR(255),
    registered_capital VARCHAR(255),
    paid_in_capital VARCHAR(255),
    revenue VARCHAR(255),
    assets VARCHAR(255),
    profit VARCHAR(255),
    stock_price_index VARCHAR(255),
    qualification_certificate_count INTEGER,
    tax_rating VARCHAR(255),
    public_opinion_count INTEGER,
    legal_dispute_count INTEGER,
    registered_address VARCHAR(512),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);

-- =================================================================
-- Table for Product Information (Hierarchical)
-- =================================================================
CREATE TABLE product_info (
    id BIGSERIAL PRIMARY KEY,
    level1 VARCHAR(255),
    level2 VARCHAR(255),
    level3 VARCHAR(255),
    level4 VARCHAR(255),
    level5 VARCHAR(255)
);

-- =================================================================
-- Tables for Product Knowledge Graph (Nodes and Edges)
-- =================================================================
CREATE TABLE product_nodes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    level INTEGER NOT NULL
);

CREATE TABLE product_edges (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    CONSTRAINT fk_parent FOREIGN KEY (parent_id) REFERENCES product_nodes(id),
    CONSTRAINT fk_child FOREIGN KEY (child_id) REFERENCES product_nodes(id)
);

-- =================================================================
-- Table for Company Relationships
-- =================================================================
CREATE TABLE company_relations (
    id BIGSERIAL PRIMARY KEY,
    company_one_id BIGINT NOT NULL,
    company_two_id BIGINT NOT NULL,
    shared_product_name VARCHAR(255) NOT NULL,
    relation_name VARCHAR(255) NOT NULL,
    relation_type VARCHAR(255) NOT NULL,
    UNIQUE (company_one_id, company_two_id, shared_product_name, relation_name, relation_type)
);

-- =================================================================
-- Tables for System Management (RBAC)
-- =================================================================
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    manager_id BIGINT,
    parent_id BIGINT
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT true,
    status VARCHAR(255),
    last_login TIMESTAMP,
    organization_id BIGINT
);

-- Add constraints after table creation to handle circular dependencies
ALTER TABLE organizations
    ADD CONSTRAINT fk_org_parent FOREIGN KEY (parent_id) REFERENCES organizations(id),
    ADD CONSTRAINT fk_org_manager FOREIGN KEY (manager_id) REFERENCES users(id);

ALTER TABLE users
    ADD CONSTRAINT fk_user_org FOREIGN KEY (organization_id) REFERENCES organizations(id);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    resource VARCHAR(255),
    key VARCHAR(255) UNIQUE NOT NULL,
    label VARCHAR(255),
    parent_id BIGINT
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_perm FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_perm_role FOREIGN KEY (permission_id) REFERENCES permissions(id)
);
