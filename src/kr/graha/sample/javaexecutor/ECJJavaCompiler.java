/*
 *
 * Copyright (C) HeonJik, KIM
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package kr.graha.sample.javaexecutor;

import java.io.File;
import java.io.IOException;
import kr.graha.helper.LOG;
import kr.graha.post.lib.Record;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import java.io.StringWriter;
import kr.graha.helper.STR;

/**
 * Java Executor ECJ Java Compilier
 * Eclipse JDT Compilier (ecj)를 이용해서 Java 파일을 컴파일한다.
 
 * Java 9 이후의 버전 (예를 들어 Java 11)을 사용하면서,
 * Apache Tomcat 7.x 의 ecj-4.4.2.jar 혹은 Apache Tomcat 8.x 의 ecj-4.6.3.jar 같은
 * 오래된 Eclipse JDT Compilier (ecj) 라이브러리를 사용한다면,
 * "The type java.lang.Object cannot be resolved." 와 같은 에러를 만날 수 있다.
 * 이 경우 Apache Tomcat 9.x 으로 업그레이드 하거나,
 * 그것이 여의치 않은 경우 Apache Tomcat 9.x 의 lib 디렉토리에서
 * ecj-4.20.jar 파일을 WEB-INF/lib 디렉토리로 복사해서 사용하면 된다.

 * 다음의 소스를 참고해서 이것을 근본적으로 개선할 수 있을 것으로 생각되지만,
 * 이 프로그램을 개발하면서 설정한 범위를 넘어서는 것이므로 거기까지는 하지 않기로 한다.
 * https://github.com/apache/tomcat/blob/main/java/org/apache/jasper/compiler/JDTCompiler.java

 * 위의 소스코드를 참조하면,
 * .java 파일을 만들지 않고, String 형태의 Java Source 코드를 컴파일 하는 더 나은 방법에 대한 힌트를 얻을 수 있다고 생각하지만,
 * Graha XML 정의파일의 Processor 에 Java Source 코드를 넣는 방식(이런 방식은 Runtime 에서 컴파일 에러를 디버깅해야 하는 문제가 있으므로 구현할 생각이 없다)을 상정하더라도
 * .java 파일을 만드는 것이 더 나은 선택일 가능성이 높다(jsp 도 .java 파일을 만든다!!!).

 * @author HeonJik, KIM

 * @version 0.9
 * @since 0.9
 */

public class ECJJavaCompiler {
	protected CompileResult compile(File javaFile, Record params, String classpath) {
		StringWriter err = null;
		StringWriter out = null;
		String error = null;
		try {
			out = new StringWriter();
			err = new StringWriter();
			String compileCmd = javaFile.getPath();
			if(classpath != null) {
				compileCmd += " -classpath " + classpath;
			}
			compileCmd += " -encoding " + "UTF-8";
			if(STR.valid(params.getString(Record.key(Record.PREFIX_TYPE_PROP, "compiler.ecj.opts")))) {
				compileCmd += " " + params.getString(Record.key(Record.PREFIX_TYPE_PROP, "compiler.ecj.opts"));
			}
			if(STR.valid(params.getString(Record.key(Record.PREFIX_TYPE_PROP, "compiler.ecj.warn")))) {
				compileCmd += " -warn:" + params.getString(Record.key(Record.PREFIX_TYPE_PROP, "compiler.ecj.warn"));
			}
			boolean result = BatchCompiler.compile(compileCmd, new java.io.PrintWriter(out), new java.io.PrintWriter(err), null);
			error = err.toString();
			err.close();
			err = null;
			out.close();
			out = null;
			return new CompileResult(result, error);
		} catch(Exception e) {
			if(err != null) {
				error = err.toString();
			} else {
				error = LOG.toString(e);
			}
		} finally {
			if(err != null) {
				try {
					err.close();
					err = null;
				} catch(IOException e) {}
			}
			if(out != null) {
				try {
					out.close();
					out = null;
				} catch(IOException e) {}
			}
		}
		return new CompileResult(false, error);
	}
}

