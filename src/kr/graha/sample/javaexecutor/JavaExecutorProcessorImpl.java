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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.graha.lib.Processor;
import kr.graha.lib.Record;
import kr.graha.lib.LogHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Java Executor(그라하(Graha) 전처리기/후처리기)
 * Java 코드를 ajax 로 받아, 컴파일하고 실행한다.
 
 * 이 프로그램은 다음과 같은 요구사항을 만족해야 한다.
 * 1. Java class 정보를 실시간으로 읽어와야 한다(메모리 캐쉬에서 읽어오면 안된다).
 * 2. 메소드를 호출할 때 classpath를 실시간으로 공급해야 한다.
 
 * Java 8 에서는 SystemClassLoader를 URLClassLoader 로 캐스팅한 다음 리플렉션(Reflection) 방식으로 addURL 메소드를 호출하여 classpath를 공급하면 족했지만, Java 9 부터는 이렇게 해야한다.
 * Javamail 및 데이타베이스 연결도 잘 되고, JNDI 를 이용한 데이타베이스 연결도 잘 된다.

 * @author HeonJik, KIM
 
 * @see kr.graha.lib.Processor
 
 * @version 0.9
 * @since 0.9
 */
public class JavaExecutorProcessorImpl extends ClassLoader implements Processor {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	public JavaExecutorProcessorImpl() {
		
	}

/**
 * Graha 가 호출하는 메소드
 
 * @param request HttpServlet 에 전달된 HttpServletRequest
 * @param response HttpServlet 에 전달된 HttpServletResponse
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)

 * @see javax.servlet.http.HttpServletRequest (Apache Tomcat 10 미만)
 * @see jakarta.servlet.http.HttpServletRequest (Apache Tomcat 10 이상)
 * @see javax.servlet.http.HttpServletResponse (Apache Tomcat 10 미만)
 * @see jakarta.servlet.http.HttpServletResponse (Apache Tomcat 10 이상)
 * @see kr.graha.lib.Record 
 * @see java.sql.Connection 
 */
	public void execute(HttpServletRequest request, HttpServletResponse response, Record params, Connection con) {
		String basePath = "/WEB-INF/javaexecutor/classes";
		File extLibDir = new File(request.getServletContext().getRealPath("/WEB-INF/javaexecutor/lib"));
		File dir = new File(request.getServletContext().getRealPath(basePath + "/kr/graha/sample/javaexecutor"));
		if(!dir.exists()) {
			dir.mkdirs();
		}
		String fileName = "Sample";
		if(params.hasKey("param.java_id")) {
			fileName += "" + params.getString("param.java_id");
		} else if(params.hasKey("param.java_history_id")) {
			fileName += "H" + params.getString("param.java_history_id");
		} else {
			params.put("result.err", "java_id is null");
			return;
		}
		boolean tomcat10 = false;
		System.out.println(request.getClass().getName());
		Class[] infs = request.getClass().getInterfaces();
		if(infs != null && infs.length > 0) {
			for(Class inf : infs) {
				if(inf.getName().equals("jakarta.servlet.http.HttpServletRequest")) {
					tomcat10 = true;
				}
			}
		}
		String classpath = request.getServletContext().getRealPath("/WEB-INF/classes");
		File libPath = new File(request.getServletContext().getRealPath("/WEB-INF/lib"));
		GrahaClassLoader loader = null;
		try {
			loader = new GrahaClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
			loader.addURL(classpath);
			loader.addURL(request.getServletContext().getRealPath(basePath));
			File[] jarFiles = libPath.listFiles();
			if(jarFiles != null) {
				for(int i = 0; i < jarFiles.length; i++) {
					loader.addURL(jarFiles[i]);
					classpath += ":" + jarFiles[i].getPath();
				}
			}
			if(extLibDir.exists()) {
				jarFiles = extLibDir.listFiles();
				if(jarFiles != null) {
					for(int i = 0; i < jarFiles.length; i++) {
						loader.addURL(jarFiles[i]);
						classpath += ":" + jarFiles[i].getPath();
					}
				}
			}
			if(System.getProperty("catalina.home") != null) {
				libPath = new File(System.getProperty("catalina.home") + java.io.File.separator + "lib");
				if(libPath.exists()) {
					jarFiles = libPath.listFiles();
					if(jarFiles != null) {
						for(int i = 0; i < jarFiles.length; i++) {
							loader.addURL(jarFiles[i]);
							classpath += ":" + jarFiles[i].getPath();
						}
					}
				}
			}
			if(System.getProperty("catalina.base") != null) {
				if(
					System.getProperty("catalina.home") == null
					|| !System.getProperty("catalina.base").equals(System.getProperty("catalina.home"))
				) {
				libPath = new File(System.getProperty("catalina.base") + java.io.File.separator + "lib");
					if(libPath.exists()) {
						jarFiles = libPath.listFiles();
						if(jarFiles != null) {
							for(int i = 0; i < jarFiles.length; i++) {
								loader.addURL(jarFiles[i]);
								classpath += ":" + jarFiles[i].getPath();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.severe(LogHelper.toString(e));
		}
		
		File javaFile = new File(request.getServletContext().getRealPath(basePath + "/kr/graha/sample/javaexecutor/" + fileName + ".java"));
		File classFile = new File(request.getServletContext().getRealPath(basePath + "/kr/graha/sample/javaexecutor/" + fileName + ".class"));

		CompileResult cr = this.compile(javaFile, params, classpath, tomcat10);
		if(cr.result) {
			cr = this.execute(classFile, loader, request, response, params, con, tomcat10);
			if(cr.result) {
				params.put("result.out", cr.out);
			} else {
				params.put("result.err", cr.error);
			}
		} else {
			params.put("result.err", cr.error);
		}
	}
/**
 * Java class 파일을 실행한다.
 
 * @param classFile Java class 파일
 * @param loader 사용자 정의 ClassLoader
 * @return 실행결과
 * @see CompileResult
 */
	public CompileResult execute(File classFile, GrahaClassLoader loader, HttpServletRequest request, HttpServletResponse response, Record params, Connection con, boolean tomcat10) {
		ByteArrayOutputStream baos = null;
		PrintStream out = null;
		String error = null;
		String result = null;
		try {
			baos = new ByteArrayOutputStream();
			out = new PrintStream(baos);
			Thread.currentThread().setContextClassLoader(loader);
			Class<?> c = loader.defineClass("kr.graha.sample.javaexecutor", classFile);
			
			Executable p = (Executable)c.newInstance();
			p.setRequest(request);
			p.setResponse(response);
			p.setParams(params);
			p.setConnection(con);
			p.execute(out);
/*
Apache Tomcat 안에서 실행하는 경우,
GrahaClassLoader 를 생성할 때, parent ClassLoader를 this 따위로 주면,
ClassLoader 가 서로 다르기 때문에 서로간에 casting 할 수 없고,
다음과 같이 Method.invoke 방식으로만 실행할 수 있고, 그렇게 하면 JNDI 따위를 공유하지 않고 실행 할 수 있다.
			loader.print(c, System.out);
			Method m = null;
			m = loader.getMethodByName(c, "execute");
			loader.print(m, System.out);
			Object obj = c.newInstance();
			m.invoke(obj, out);
*/			
			result = baos.toString();
			out.close();
			out = null;
			baos.close();
			baos = null;
			return new CompileResult(true, result, null);
		} catch (Exception e) {
			error = LogHelper.toString(e);
		} finally {
			if(out != null) {
					out.close();
					out = null;
			}
			if(baos != null) {
				try {
					baos.close();
					baos = null;
				} catch(IOException e) {}
			}
		}
		return new CompileResult(false, result, error);
	}
/**
 * Java 소스 파일을 컴파일 한다.
 
 * @param javaFile Java 소스 파일
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param classpath 컴파일 할 때 공급할 classpath
 * @return 실행결과
 * @see CompileResult
 */
	public CompileResult compile(File javaFile, Record params, String classpath, boolean tomcat10) {
		PrintStream source = null;
		StringWriter sw = null;
		String error = null;
		try {
			source = new PrintStream(javaFile);
			source.println("package kr.graha.sample.javaexecutor;");
			source.println();
			source.println("import java.sql.Connection;");
			source.println("import kr.graha.lib.Record;");
			if(tomcat10) {
				source.println("import jakarta.servlet.http.HttpServletRequest;");
				source.println("import jakarta.servlet.http.HttpServletResponse;");
			} else {
				source.println("import javax.servlet.http.HttpServletRequest;");
				source.println("import javax.servlet.http.HttpServletResponse;");
			}
			source.println("import java.io.PrintStream;");
			if(params.hasKey("param.source")) {
				StringTokenizer st = new StringTokenizer(params.getString("param.source"));
				while (st.hasMoreTokens()) {
					String line = st.nextToken();
					if(line == null) {
						continue;
					} else if(line.equals("import")) {
						continue;
					} else {
						source.print("import ");
					}
					source.print(line);
					if(line.trim().endsWith(";")) {
						source.println();
					} else {
						source.println(";");
					}
				}
			}
			source.println();
			source.println("public class " + javaFile.getName().substring(0, javaFile.getName().lastIndexOf(".")) + " implements kr.graha.sample.javaexecutor.Executable, java.io.Serializable {");
			source.println("	private HttpServletRequest request;");
			source.println("	private HttpServletResponse response;");
			source.println("	private Record params;");
			source.println("	private Connection con;");
			source.println("	public void setRequest(HttpServletRequest request) {");
			source.println("		this.request = request;");
			source.println("	}");
			source.println("	public void setResponse(HttpServletResponse response) {");
			source.println("		this.response = response;");
			source.println("	}");
			source.println("	public void setParams(Record params) {");
			source.println("		this.params = params;");
			source.println("	}");
			source.println("	public void setConnection(Connection con) {");
			source.println("		this.con = con;");
			source.println("	}");
			source.println("	public void execute(PrintStream out) {");
			source.println();
			source.println(params.getString("param.contents"));
			source.println();
			source.println("	}");
			source.println("}");
			source.flush();
			source.close();
			source = null;
			
			sw = new StringWriter();
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
			Iterable<? extends JavaFileObject> unit = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(javaFile));
			List<String> option = new ArrayList<String>();
			option.addAll(Arrays.asList("-classpath", classpath));
			System.out.println(classpath);
			boolean result = compiler.getTask(sw, fileManager, null, option, null, unit).call();
			fileManager.close();
			error = sw.toString();
			sw.close();
			sw = null;
			return new CompileResult(result, error);
		} catch(Exception e) {
			if(sw != null) {
				error = sw.toString();
			} else {
				error = LogHelper.toString(e);
			}
		} finally {
			if(source != null) {
					source.close();
					source = null;
			}
			if(sw != null) {
				try {
					sw.close();
					sw = null;
				} catch(IOException e) {}
			}
			
		}
		return new CompileResult(false, error);
	}
}
class CompileResult {
	public boolean result;
	public String error;
	public String out;
	public CompileResult(boolean result, String error) {
		this.result = result;
		this.error = error;
	}
	public CompileResult(boolean result, String out, String error) {
		this.result = result;
		this.out = out;
		this.error = error;
	}
}