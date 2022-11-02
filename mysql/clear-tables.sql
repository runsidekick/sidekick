SET FOREIGN_KEY_CHECKS = 0;

-- ## --
DELETE FROM Application;
DELETE FROM EventHistory;
DELETE FROM LogPoint;
DELETE FROM ProbeTag;
DELETE FROM ReferenceEvent;
DELETE FROM ServerStatistics;
DELETE FROM TracePoint;
DELETE FROM Webhook;
-- ## --

SET FOREIGN_KEY_CHECKS = 1;
