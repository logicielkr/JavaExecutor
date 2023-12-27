package kr.graha.sample.javaexecutor;

import java.sql.Connection;
import kr.graha.post.lib.Record;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintStream;

public interface Executable {
	void execute(PrintStream out);
	void setRequest(HttpServletRequest request);
	void setResponse(HttpServletResponse response);
	void setParams(Record params);
	void setConnection(Connection con);
}

