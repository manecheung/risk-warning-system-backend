-- V1__Initial_Schema.sql
-- This script creates the initial database schema for the risk warning system.

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

-- Add constraints after table creation to handle circular dependencies
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
-- Table for Chain Risk Simulations
-- =================================================================
CREATE TABLE chain_risk_simulations
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(1024),
    creator     VARCHAR(255),
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nodes       TEXT         NOT NULL,
    edges       TEXT         NOT NULL,
    risk_path   TEXT
);

-- =================================================================
-- Table for Monitoring Articles
-- =================================================================
CREATE TABLE monitoring_articles
(
    id              BIGSERIAL PRIMARY KEY,
    type            VARCHAR(50)  NOT NULL, -- 'news' or 'risk'
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

-- Create a table for article tags, as defined in the entity.
CREATE TABLE monitoring_article_tags
(
    article_id BIGINT NOT NULL,
    tag        VARCHAR(255),
    CONSTRAINT fk_article_tags FOREIGN KEY (article_id) REFERENCES monitoring_articles (id) ON DELETE CASCADE
);

-- =================================================================
-- Mock Data for Monitoring Articles
-- =================================================================
INSERT INTO monitoring_articles (id, type, title, author, publish_date, image, content, risk_source, notice,
                                 related_company, related_product)
VALUES (1, 'news',
        '这是一个非常非常长的标题，用于专门测试当标题内容超出容器宽度时，是否能够正确地显示横向滚动条而不是简单地截断文本或者破坏布局。',
        '北极星风力发电网', '2024-11-12', '/风电.svg', '这是文章1的内容。这是一个测试内容，用于确保长标题的显示效果。',
        NULL, NULL, NULL, NULL),
       (2, 'news', '风电“抢装潮”退潮！华东勘测设计院发布5份行政处罚决定书', '北极星风力发电网', '2024-11-12',
        '/法规.svg', '这是文章2的内容。详细描述了“抢装潮”退潮后的市场情况和相关处罚决定。', NULL, NULL, NULL, NULL),
       (3, 'news', '新能源汽车下乡政策再加码，充电桩建设成关键', '第一财经', '2024-11-11', '/法规.svg',
        '这是文章3的内容。分析了新能源汽车下乡政策的最新动态和充电桩建设的重要性。', NULL, NULL, NULL, NULL),
       (4, 'risk', '漳州帆船配舾工程有限公司员工坠亡', '北极星风力发电网', '2024-11-12', '/风险.svg',
        '<h4>事故背景</h4><p>近期，安全生产监督管理部门发布了一则关于高处作业安全的紧急通报，通报中披露了此次不幸的事故。据了解，涉事员工在进行高空焊接准备工作时，未按规定佩戴安全防护设备，且现场缺乏有效的安全监护措施，最终导致了悲剧的发生。</p><p>该事件不仅给遇难者家属带来了巨大的悲痛，也为相关企业敲响了安全生产的警钟。监管部门已责令该公司全面停产整顿，并对相关责任人展开调查。</p>',
        '人员坠落, 抢救无效死亡',
        '《通知》显示，2024年9月4日3时10分许，在漳浦县六鳌镇某船厂一新能源装备制造有限公司风电装备车间，漳州帆船配舾工程有限公司的1名员工在管桩机上进行绕管焊接作业准备中发生坠落，经抢救无效死亡。事故具体原因仍在调查中。',
        '漳州帆船配舾工程有限公司', '船舵总筒'),
       (5, 'risk',
        '某上市公司财务造假被证监会立案调查，股价连续跌停引发市场恐慌，这是一个为了测试而设置的非常长的风险新闻标题',
        '证券时报', '2024-11-10', '/风险.svg', '这是文章5的内容。关于某上市公司财务造假的详细报道和市场反应。',
        '财务造假', '证监会已正式立案调查，相关细节待后续公布。', '某上市公司', '该公司股票'),
       (6, 'risk', '供应链中断，某手机厂商新款发布或将延迟', '供应链前沿', '2024-11-09', '/风险.svg',
        '这是文章6的内容。由于关键组件供应中断，某知名手机厂商的新款发布计划可能受到影响。', '供应链中断',
        '官方尚未发布正式延迟通知，但内部消息人士透露了这一可能性。', '某手机厂商', '新款智能手机'),
       (7, 'news', '光伏产业迎来新一轮技术迭代，N型电池成市场主流', '光伏资讯', '2024-11-08', '/法规.svg',
        '这是文章7的内容。探讨了光伏产业的技术发展趋势，特别是N型电池的市场前景。', NULL, NULL, NULL, NULL),
       (8, 'news', '“东数西算”工程全面启动，数据中心建设提速', '人民邮电报', '2024-11-07', '/法规.svg',
        '这是文章8的内容。报道了“东数西算”工程的启动及其对数据中心建设的推动作用。', NULL, NULL, NULL, NULL),
       (9, 'risk', '数据安全漏洞曝光，知名社交平台用户隐私面临威胁', '网络安全观察', '2024-11-06', '/风险.svg',
        '这是文章9的内容。一个严重的数据安全漏洞被发现，可能影响数百万用户的隐私数据。', '数据安全漏洞',
        '该社交平台已承认漏洞存在，并正在紧急修复中。', '知名社交平台', '用户隐私数据'),
       (10, 'risk', '环保审查趋严，某化工企业因排污超标被责令停产整顿', '环保在线', '2024-11-05', '/风险.svg',
        '这是文章10的内容。在最新的环保审查中，某化工企业因严重超标排放污染物被要求立即停产整顿。', '排污超标',
        '环保部门已发出正式通知，并将进行进一步调查。', '某化工企业', '化工原料');

INSERT INTO monitoring_article_tags (article_id, tag)
VALUES (1, '运维'),
       (1, '人才'),
       (2, '法规'),
       (2, '处罚决定书'),
       (3, '政策'),
       (3, '汽车'),
       (4, '事故'),
       (4, '安全'),
       (5, '财务风险'),
       (5, '调查'),
       (6, '供应链'),
       (6, '中断'),
       (7, '技术'),
       (7, '光伏'),
       (8, '新基建'),
       (8, '数据中心'),
       (9, '数据安全'),
       (9, '隐私泄露'),
       (10, '环保'),
       (10, '监管');
