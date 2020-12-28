CREATE SEQUENCE "java$java_id"; 

create table java (
	java_id integer NOT NULL DEFAULT nextval('java$java_id'::regclass),
	title varchar(1000),
	source text,
	contents text,
	results text,
	encrypted bool,
	parent_id integer,
	insert_date timestamp,
	insert_id varchar(50),
	insert_ip varchar(15),
	update_date timestamp,
	update_id varchar(50),
	update_ip varchar(15),
	PRIMARY KEY (java_id)
);

/**
like 검색 성능 향상
*/
CREATE EXTENSION pg_trgm;
CREATE INDEX "java$title" ON java USING gin (title gin_trgm_ops);
CREATE INDEX "java$contents" ON java USING gin (contents gin_trgm_ops);

comment on table java is 'Java 실행기';

COMMENT ON COLUMN java.java_id IS '고유번호';
COMMENT ON COLUMN java.title IS '제목';
COMMENT ON COLUMN java.source IS 'import 패키지';
COMMENT ON COLUMN java.contents IS '내용';
COMMENT ON COLUMN java.results IS '실행결과';
COMMENT ON COLUMN java.encrypted IS '암호화';
COMMENT ON COLUMN java.parent_id IS '상위 Java 실행기 고유번호';
COMMENT ON COLUMN java.insert_date IS '최초입력일시';
COMMENT ON COLUMN java.insert_id IS '최초입력자ID';
COMMENT ON COLUMN java.insert_ip IS '최초입력자IP';
COMMENT ON COLUMN java.update_date IS '최종수정일시';
COMMENT ON COLUMN java.update_id IS '최종수정자ID';
COMMENT ON COLUMN java.update_ip IS '최종수정자IP';

CREATE SEQUENCE "java_history$java_history_id";

create table java_history (
	java_history_id integer NOT NULL DEFAULT nextval('java_history$java_history_id'::regclass),
	title varchar(1000),
	source text,
	contents text,
	results text,
	encrypted bool,
	parent_id integer,
	java_id integer,
	autosave bool,
	insert_date timestamp,
	insert_id varchar(50),
	insert_ip varchar(15),
	update_date timestamp,                   
	update_id varchar(50),
	update_ip varchar(15),
	PRIMARY KEY (java_history_id)
);

comment on table java_history is 'Java 실행기 이력관리';

COMMENT ON COLUMN java_history.java_history_id IS '고유번호';
COMMENT ON COLUMN java_history.title IS '제목';
COMMENT ON COLUMN java_history.source IS 'import 패키지';
COMMENT ON COLUMN java_history.contents IS '내용';
COMMENT ON COLUMN java_history.results IS '실행결과';
COMMENT ON COLUMN java_history.encrypted IS '암호화';
COMMENT ON COLUMN java_history.parent_id IS '상위 Java 실행기 고유번호';
COMMENT ON COLUMN java_history.java_id IS 'Java 실행기 ID';
COMMENT ON COLUMN java_history.autosave IS '자동저장';
COMMENT ON COLUMN java_history.insert_date IS '최초입력일시';
COMMENT ON COLUMN java_history.insert_id IS '최초입력자ID';
COMMENT ON COLUMN java_history.insert_ip IS '최초입력자IP';
COMMENT ON COLUMN java_history.update_date IS '최종수정일시';
COMMENT ON COLUMN java_history.update_id IS '최종수정자ID';
COMMENT ON COLUMN java_history.update_ip IS '최종수정자IP';

