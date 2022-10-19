SET GLOBAL event_scheduler = ON;

CREATE TABLE Application
(
    instance_id                         VARCHAR(128)        NOT NULL PRIMARY KEY,
    workspace_id                        VARCHAR(64),
    name                                VARCHAR(255),
    version                             VARCHAR(64),
    stage                               VARCHAR(32),
    ip                                  VARCHAR(32),
    host_name                           VARCHAR(255),
    runtime                             VARCHAR(16),
    last_update_time                    BIGINT,
    trace_points                        JSON,
    log_points                          JSON,
    custom_tags                         JSON
);

CREATE EVENT IF NOT EXISTS clean_expired_apps
    ON SCHEDULE EVERY 5 MINUTE
DO
DELETE
FROM Application
WHERE last_update_time < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 5 MINUTE)) * 1000;

CREATE TABLE TracePoint
(
    id                                  VARCHAR(255)        NOT NULL PRIMARY KEY,
    workspace_id                        VARCHAR(64),
    user_id                             VARCHAR(64)         NOT NULL,
    file_name                           VARCHAR(255)        NOT NULL,
    line_no                             INT                 NOT NULL,
    client                              VARCHAR(64)         NOT NULL,
    condition_expression                VARCHAR(255),
    expire_secs                         INT                 NOT NULL DEFAULT -1,
    expire_count                        INT                 NOT NULL DEFAULT -1,
    tracing_enabled                     BOOLEAN             NOT NULL DEFAULT 0,
    file_hash                           VARCHAR(255),
    disabled                            BOOLEAN             NOT NULL DEFAULT 0,
    expire_timestamp                    BIGINT              NOT NULL DEFAULT -1,
    application_filters                 JSON,
    webhook_ids                         JSON,
    from_api                            BOOLEAN             NOT NULL DEFAULT 0,
    predefined                          BOOLEAN             NOT NULL DEFAULT 0,
    probe_name                          VARCHAR(255)
);

CREATE EVENT IF NOT EXISTS clean_expired_tracepoints
    ON SCHEDULE EVERY 5 MINUTE
DO
DELETE
FROM TracePoint
WHERE expire_timestamp < UNIX_TIMESTAMP() * 1000 AND predefined = 0;

CREATE TABLE LogPoint
(
    id                                  VARCHAR(255)        NOT NULL PRIMARY KEY,
    workspace_id                        VARCHAR(64),
    user_id                             VARCHAR(64)         NOT NULL,
    file_name                           VARCHAR(255)        NOT NULL,
    line_no                             INT                 NOT NULL,
    client                              VARCHAR(64)         NOT NULL,
    condition_expression                VARCHAR(255),
    expire_secs                         INT                 NOT NULL DEFAULT -1,
    expire_count                        INT                 NOT NULL DEFAULT -1,
    file_hash                           VARCHAR(255),
    disabled                            BOOLEAN             NOT NULL DEFAULT 0,
    expire_timestamp                    BIGINT              NOT NULL DEFAULT -1,
    application_filters                 JSON,
    log_expression                      VARCHAR(1024)       NOT NULL,
    stdout_enabled                      BOOLEAN             NOT NULL DEFAULT 0,
    log_level                           VARCHAR(1024),
    webhook_ids                         JSON,
    from_api                            BOOLEAN             NOT NULL DEFAULT 0,
    predefined                          BOOLEAN             NOT NULL DEFAULT 0,
    probe_name                          VARCHAR(255)
);

CREATE EVENT IF NOT EXISTS clean_expired_logpoints
    ON SCHEDULE EVERY 5 MINUTE
DO
DELETE
FROM LogPoint
WHERE expire_timestamp < UNIX_TIMESTAMP() * 1000 AND predefined = 0;

CREATE TABLE Webhook
(
    id                                  VARCHAR(64)         NOT NULL PRIMARY KEY,
    workspace_id                    	VARCHAR(64)         NOT NULL,
    name                    			VARCHAR(255)        NOT NULL,
    type                                VARCHAR(64)         NOT NULL,
    config                              JSON                NOT NULL,
    last_error_reason                   VARCHAR(255),
    last_error_date                     TIMESTAMP,
    error_count                         INT(10)             NOT NULL DEFAULT 0,
    disabled                            TINYINT             NOT NULL DEFAULT 0
);

CREATE TABLE ReferenceEvent (
    probe_id                            VARCHAR(255)        NOT NULL,
    workspace_id                    	VARCHAR(64),
    application_filter                  JSON,
    probe_type                    	    ENUM(
        "TRACEPOINT",
        "LOGPOINT"
        )                     NOT NULL,
    event                    			JSON                NOT NULL,
    INDEX (probe_id, workspace_id, probe_type)
);

CREATE TABLE ServerStatistics (
    workspace_id                        VARCHAR(64)         NOT NULL PRIMARY KEY,
    application_instance_count          INT(10)             DEFAULT 0,
    tracepoint_count                    INT(10)             DEFAULT 0,
    logpoint_count                      INT(10)             DEFAULT 0
);

CREATE TABLE ApplicationConfig (
    id                                  VARCHAR(64)         NOT NULL PRIMARY KEY,
    workspace_id                        VARCHAR(64),
    application_filter                  JSON,
    config                              JSON,
    detached                            BOOLEAN             NOT NULL DEFAULT 0
);