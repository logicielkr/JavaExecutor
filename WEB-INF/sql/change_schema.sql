CREATE SCHEMA java;
ALTER TABLE java SET SCHEMA java;
ALTER TABLE java_history SET SCHEMA java;
alter SEQUENCE "java$java_id" SET SCHEMA java;
alter SEQUENCE "java_history$java_history_id" SET SCHEMA java;
alter table java.java alter column java_id set default nextval('java.java$java_id'::regclass);
alter table java.java_history alter column java_history_id set default nextval('java.java_history$java_history_id'::regclass);

