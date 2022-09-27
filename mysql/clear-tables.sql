SET FOREIGN_KEY_CHECKS = 0;

-- ## --
DELETE FROM Application;
DELETE FROM LogPoint;
DELETE FROM ReferenceEvent;
DELETE FROM ServerStatistics;
DELETE FROM TracePoint;
DELETE FROM Webhook;
-- ## --

SET FOREIGN_KEY_CHECKS = 1;
