-- V1__Initial_Schema.sql (Unified & Corrected)
-- This script creates the complete initial database schema for the risk warning system.
-- It includes simulation management tables from the start to ensure correct dependency order.

-- =================================================================
-- Table for Company Information
-- =================================================================
CREATE TABLE company_info
(
    id                              BIGSERIAL PRIMARY KEY,
    name                            VARCHAR(512),
    major_product1                  VARCHAR(512),
    major_product2                  VARCHAR(512),
    main_products_summary           TEXT,
    related_products                TEXT,
    industry                        VARCHAR(512),
    company_type                    VARCHAR(255),
    is_diversified                  VARCHAR(255),
    is_well_known                   VARCHAR(255),
    company_size                    VARCHAR(255),
    employee_count                  VARCHAR(255),
    registered_capital              VARCHAR(255),
    paid_in_capital                 VARCHAR(255),
    revenue                         VARCHAR(255),
    assets                          VARCHAR(255),
    profit                          VARCHAR(255),
    stock_price_index               VARCHAR(255),
    qualification_certificate_count INTEGER,
    tax_rating                      VARCHAR(255),
    public_opinion_count            INTEGER,
    legal_dispute_count             INTEGER,
    registered_address              VARCHAR(512),
    latitude                        DOUBLE PRECISION,
    longitude                       DOUBLE PRECISION
);

-- =================================================================
-- Table for Product Information (Hierarchical)
-- =================================================================
CREATE TABLE product_info
(
    id     BIGSERIAL PRIMARY KEY,
    level1 VARCHAR(255),
    level2 VARCHAR(255),
    level3 VARCHAR(255),
    level4 VARCHAR(255),
    level5 VARCHAR(255)
);

-- =================================================================
-- Tables for Product Knowledge Graph (Nodes and Edges)
-- =================================================================
CREATE TABLE product_nodes
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL UNIQUE,
    level INTEGER      NOT NULL
);

CREATE TABLE product_edges
(
    id        BIGSERIAL PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    child_id  BIGINT NOT NULL,
    CONSTRAINT fk_parent FOREIGN KEY (parent_id) REFERENCES product_nodes (id),
    CONSTRAINT fk_child FOREIGN KEY (child_id) REFERENCES product_nodes (id)
);

-- =================================================================
-- Table for Company Relationships
-- =================================================================
CREATE TABLE company_relations
(
    id                  BIGSERIAL PRIMARY KEY,
    company_one_id      BIGINT       NOT NULL,
    company_two_id      BIGINT       NOT NULL,
    shared_product_name VARCHAR(255) NOT NULL,
    relation_name       VARCHAR(255) NOT NULL,
    relation_type       VARCHAR(255) NOT NULL,
    UNIQUE (company_one_id, company_two_id, shared_product_name, relation_name, relation_type)
);

-- =================================================================
-- Tables for System Management (RBAC)
-- =================================================================
CREATE TABLE organizations
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255),
    description VARCHAR(255),
    manager_id  BIGINT,
    parent_id   BIGINT
);

CREATE TABLE users
(
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(255) UNIQUE NOT NULL,
    password        VARCHAR(255)        NOT NULL,
    name            VARCHAR(255),
    email           VARCHAR(255),
    phone           VARCHAR(255),
    enabled         BOOLEAN             NOT NULL DEFAULT true,
    status          VARCHAR(255),
    last_login      TIMESTAMP,
    organization_id BIGINT
);

ALTER TABLE organizations
    ADD CONSTRAINT fk_org_parent FOREIGN KEY (parent_id) REFERENCES organizations (id),
    ADD CONSTRAINT fk_org_manager FOREIGN KEY (manager_id) REFERENCES users (id);

ALTER TABLE users
    ADD CONSTRAINT fk_user_org FOREIGN KEY (organization_id) REFERENCES organizations (id);

CREATE TABLE roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE permissions
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255),
    description VARCHAR(255),
    resource    VARCHAR(255),
    key         VARCHAR(255) UNIQUE NOT NULL,
    label       VARCHAR(255),
    parent_id   BIGINT
);

CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE role_permissions
(
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_perm FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_perm_role FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

-- =================================================================
-- Table for Monitoring Articles
-- =================================================================
CREATE TABLE monitoring_articles
(
    id              BIGSERIAL PRIMARY KEY,
    type            VARCHAR(50)  NOT NULL,
    title           VARCHAR(512) NOT NULL,
    author          VARCHAR(255),
    publish_date    DATE,
    image           VARCHAR(255),
    risk_source     TEXT,
    notice          TEXT,
    related_company TEXT,
    related_product TEXT,
    url             VARCHAR(1024),
    content         TEXT         NOT NULL
);

CREATE TABLE monitoring_article_tags
(
    article_id BIGINT NOT NULL,
    tag        VARCHAR(255),
    CONSTRAINT fk_article_tags FOREIGN KEY (article_id) REFERENCES monitoring_articles (id) ON DELETE CASCADE
);

-- =================================================================
-- Tables for Simulation Management & Data
-- =================================================================

-- Create the 'simulations' table FIRST, as other tables depend on it.
CREATE TABLE simulations
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table for KRI (Key Risk Indicators)
CREATE TABLE kri
(
    id         BIGSERIAL PRIMARY KEY,
    kz         DOUBLE PRECISION,
    kc1_k      DOUBLE PRECISION, kc1_score  DOUBLE PRECISION,
    kc2_k      DOUBLE PRECISION, kc2_score  DOUBLE PRECISION,
    kc3_k      DOUBLE PRECISION, kc3_score  DOUBLE PRECISION,
    kc4_k      DOUBLE PRECISION, kc4_score  DOUBLE PRECISION,
    kc5_k      DOUBLE PRECISION, kc5_score  DOUBLE PRECISION,
    kc6_k      DOUBLE PRECISION, kc6_score  DOUBLE PRECISION,
    kc7_k      DOUBLE PRECISION, kc7_score  DOUBLE PRECISION,
    kc8_k      DOUBLE PRECISION, kc8_score  DOUBLE PRECISION,
    kc9_k      DOUBLE PRECISION, kc9_score  DOUBLE PRECISION,
    kc10_k     DOUBLE PRECISION, kc10_score DOUBLE PRECISION,
    kc11_k     DOUBLE PRECISION, kc11_score DOUBLE PRECISION,
    kf         DOUBLE PRECISION,
    kc12_k     DOUBLE PRECISION, kc12_score DOUBLE PRECISION,
    kc13_k     DOUBLE PRECISION, kc13_score DOUBLE PRECISION,
    kri_score  DOUBLE PRECISION
);

-- Table for Company Simulation Data, now with a direct foreign key to 'simulations'.
CREATE TABLE company_simulation_data
(
    id              BIGSERIAL PRIMARY KEY,
    simulation_id   BIGINT,
    company_id      INTEGER,
    name            VARCHAR(255),
    time            INTEGER,
    state           INTEGER,
    inner_factor    DOUBLE PRECISION,
    compete_factor  DOUBLE PRECISION,
    material_factor DOUBLE PRECISION,
    kri_id          BIGINT,
    CONSTRAINT fk_simulation FOREIGN KEY (simulation_id) REFERENCES simulations (id) ON DELETE CASCADE,
    CONSTRAINT fk_company_simulation_kri FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE
);

-- Tables for KCI W values (one for each KC)
CREATE TABLE kri_kc1_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc1_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);
CREATE TABLE kri_kc2_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc2_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);
CREATE TABLE kri_kc3_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc3_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);
CREATE TABLE kri_kc4_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc4_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);
CREATE TABLE kri_kc5_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc5_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);
CREATE TABLE kri_kc6_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc6_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);
CREATE TABLE kri_kc7_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc7_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);
CREATE TABLE kri_kc8_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc8_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);
CREATE TABLE kri_kc9_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc9_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);
CREATE TABLE kri_kc10_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc10_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);
CREATE TABLE kri_kc11_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc11_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);
CREATE TABLE kri_kc12_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc12_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);
CREATE TABLE kri_kc13_w (kri_id BIGINT NOT NULL, value DOUBLE PRECISION, CONSTRAINT fk_kri_kc13_w FOREIGN KEY (kri_id) REFERENCES kri (id) ON DELETE CASCADE);

-- Table for Material Data, linked to company_simulation_data
CREATE TABLE material_data
(
    id    BIGSERIAL PRIMARY KEY,
    simulation_id BIGINT, -- This is the FOREIGN KEY to company_simulation_data.id
    name  VARCHAR(255),
    w     DOUBLE PRECISION,
    n_max DOUBLE PRECISION,
    CONSTRAINT fk_material_data_simulation FOREIGN KEY (simulation_id) REFERENCES company_simulation_data (id) ON DELETE CASCADE
);

CREATE TABLE material_data_suppliers (material_data_id BIGINT NOT NULL, supplier_id INTEGER, CONSTRAINT fk_material_data_suppliers FOREIGN KEY (material_data_id) REFERENCES material_data (id) ON DELETE CASCADE);

-- Table for Product Data, linked to company_simulation_data
CREATE TABLE product_data
(
    id   BIGSERIAL PRIMARY KEY,
    simulation_id BIGINT, -- This is the FOREIGN KEY to company_simulation_data.id
    name VARCHAR(255),
    w    DOUBLE PRECISION,
    nums DOUBLE PRECISION,
    f    DOUBLE PRECISION,
    CONSTRAINT fk_product_data_simulation FOREIGN KEY (simulation_id) REFERENCES company_simulation_data (id) ON DELETE CASCADE
);

CREATE TABLE product_data_competitors (product_data_id BIGINT NOT NULL, competitor_id INTEGER, CONSTRAINT fk_product_data_competitors FOREIGN KEY (product_data_id) REFERENCES product_data (id) ON DELETE CASCADE);
CREATE TABLE product_data_customers (product_data_id BIGINT NOT NULL, customer_id VARCHAR(255), value DOUBLE PRECISION, CONSTRAINT fk_product_data_customers FOREIGN KEY (product_data_id) REFERENCES product_data (id) ON DELETE CASCADE);

-- Table for Company Simulation List X
CREATE TABLE company_simulation_list_x
(
    simulation_id BIGINT NOT NULL,
    value         DOUBLE PRECISION,
    CONSTRAINT fk_company_simulation_list_x FOREIGN KEY (simulation_id) REFERENCES company_simulation_data (id) ON DELETE CASCADE
);