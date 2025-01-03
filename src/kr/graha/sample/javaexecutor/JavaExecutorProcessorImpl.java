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
import kr.graha.post.interfaces.Processor;
import kr.graha.post.lib.Record;
import kr.graha.helper.LOG;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.sql.Connection;
import java.util.StringTokenizer;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.List;
import java.util.ArrayList;

/**
 * Java Executor(그라하(Graha) 전처리기/후처리기)
 * Java 코드를 ajax 로 받아, 컴파일하고 실행한다.
 
 * 이 프로그램은 다음과 같은 요구사항을 만족해야 한다.
 * 1. Java class 정보를 실시간으로 읽어와야 한다(메모리 캐쉬에서 읽어오면 안된다).
 * 2. 메소드를 호출할 때 classpath를 실시간으로 공급해야 한다.
 
 * Java 8 에서는 SystemClassLoader를 URLClassLoader 로 캐스팅한 다음 리플렉션(Reflection) 방식으로 addURL 메소드를 호출하여 classpath를 공급하면 족했지만, Java 9 부터는 이렇게 해야한다.
 * Javamail 및 데이타베이스 연결도 잘 되고, JNDI 를 이용한 데이타베이스 연결도 잘 된다.

 * @author HeonJik, KIM
 
 * @see kr.graha.post.interfaces.Processor
 
 * @version 0.9
 * @since 0.9
 */
public class JavaExecutorProcessorImpl extends ClassLoader implements Processor {
	
//	private Logger logger = Logger.getLogger(this.getClass().getName());
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
 * @see kr.graha.post.lib.Record 
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
		if(params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, "java_id"))) {
			fileName += "" + params.getString(Record.key(Record.PREFIX_TYPE_PARAM, "java_id"));
		} else if(params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, "java_history_id"))) {
			fileName += "H" + params.getString(Record.key(Record.PREFIX_TYPE_PARAM, "java_history_id"));
		} else {
			params.put(Record.key(Record.PREFIX_TYPE_RESULT, "err"), "java_id is null");
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
		List<String> classPaths = new ArrayList<String>();
		String classpath = request.getServletContext().getRealPath("/WEB-INF/classes");
		classPaths.add(classpath);
		File libPath = new File(request.getServletContext().getRealPath("/WEB-INF/lib"));
		GrahaClassLoader loader = null;
		try {
			loader = new GrahaClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
			loader.addURL(classpath);
			loader.addURL(request.getServletContext().getRealPath(basePath));
			File[] jarFiles = libPath.listFiles();
			if(jarFiles != null) {
				for(int i = 0; i < jarFiles.length; i++) {
					if(jarFiles[i] != null && jarFiles[i].getPath() != null && jarFiles[i].getPath().endsWith(".jar")) {
						loader.addURL(jarFiles[i]);
//						classpath += ":" + jarFiles[i].getPath();
						classPaths.add(jarFiles[i].getPath());
					}
				}
			}
			if(extLibDir.exists()) {
				jarFiles = extLibDir.listFiles();
				if(jarFiles != null) {
					for(int i = 0; i < jarFiles.length; i++) {
						if(jarFiles[i] != null && jarFiles[i].getPath() != null && jarFiles[i].getPath().endsWith(".jar")) {
							loader.addURL(jarFiles[i]);
//							classpath += ":" + jarFiles[i].getPath();
							classPaths.add(jarFiles[i].getPath());
						}
					}
				}
			}
			if(System.getProperty("catalina.home") != null) {
				libPath = new File(System.getProperty("catalina.home") + java.io.File.separator + "lib");
				if(libPath.exists()) {
					jarFiles = libPath.listFiles();
					if(jarFiles != null) {
						for(int i = 0; i < jarFiles.length; i++) {
							if(jarFiles[i] != null && jarFiles[i].getPath() != null && jarFiles[i].getPath().endsWith(".jar")) {
								loader.addURL(jarFiles[i]);
//								classpath += ":" + jarFiles[i].getPath();
								classPaths.add(jarFiles[i].getPath());
							}
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
								if(jarFiles[i] != null && jarFiles[i].getPath() != null && jarFiles[i].getPath().endsWith(".jar")) {
									loader.addURL(jarFiles[i]);
//									classpath += ":" + jarFiles[i].getPath();
									classPaths.add(jarFiles[i].getPath());
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.severe(e);
//			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
		}
		
		File javaFile = new File(request.getServletContext().getRealPath(basePath + "/kr/graha/sample/javaexecutor/" + fileName + ".java"));
		File classFile = new File(request.getServletContext().getRealPath(basePath + "/kr/graha/sample/javaexecutor/" + fileName + ".class"));

		CompileResult cr = this.compile(javaFile, params, classPaths, new File(request.getServletContext().getRealPath(basePath)).getPath(), tomcat10);
		if(cr.result) {
			cr = this.execute(classFile, loader, request, response, params, con, tomcat10);
			if(cr.result) {
				params.put(Record.key(Record.PREFIX_TYPE_RESULT, "out"), cr.out);
			} else {
				params.put(Record.key(Record.PREFIX_TYPE_RESULT, "err"), cr.error);
			}
		} else {
			params.put(Record.key(Record.PREFIX_TYPE_RESULT, "err"), cr.error);
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
			out = new PrintStream(baos, false, "UTF-8");
			Thread.currentThread().setContextClassLoader(loader);
			Class<?> c = loader.defineClass("kr.graha.sample.javaexecutor", classFile);
			
			Executable p = (Executable)c.getDeclaredConstructor().newInstance();
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
			out.flush();
			result = baos.toString("UTF-8");
			out.close();
			out = null;
			baos.close();
			baos = null;
			return new CompileResult(true, result, null);
		} catch (Exception e) {
			error = LOG.toString(e);
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
 * List 에 담겨 있는 classpath 를 String 으로 변환한다.
 
 * @param classPaths List 에 담겨 있는 classpath
 * @return String 으로 변환된 classpath
 */
	private String convertClassPath(List classPaths) {
		if(classPaths != null && classPaths.size() > 0) {
			String classpath = new String();
			for(int i = 0; i < classPaths.size(); i++) {
				if(i > 0) {
					classpath += ":";
				}
				classpath += classPaths.get(i);
			}
			return classpath;
		}
		return null;
	}
/**
 * Java 소스 파일을 컴파일 한다.
 
 * @param javaFile Java 소스 파일
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param classPaths 컴파일 할 때 공급할 classpath
 * @return 실행결과
 * @see CompileResult
 */
	public CompileResult compile(File javaFile, Record params, List<String> classPaths, String baseRealPath, boolean tomcat10) {
		CompileResult compileResult = generate(javaFile, params, classPaths, baseRealPath, tomcat10);
		if(compileResult.result) {
			String classpath = this.convertClassPath(classPaths);
			if(params.equals(Record.key(Record.PREFIX_TYPE_PROP, "compiler"), "ecj")) {
				return this.compileWithECJ(javaFile, params, classpath);
			}
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if(compiler == null) {
				return this.compileWithECJ(javaFile, params, classpath);
			} else {
				return this.compileWithSystemJavaCompiler(javaFile, classpath, compiler);
			}
		} else {
			return compileResult;
		}
	}
	private String getJarPath(List<String> classPaths, String prefix) {
		if(classPaths != null && classPaths.size() > 0) {
			for(int i = 0; i < classPaths.size(); i++) {
				String classpath = (String)classPaths.get(i);
				if(classpath.lastIndexOf("/") >= 0) {
					String fileName = classpath.substring(classpath.lastIndexOf("/") + 1);
					if(fileName.startsWith(prefix)) {
						return classpath;
					}
				}
			}
		}
		return null;
	}
/**
 * Java 소스 파일을 만든다.
 
 * @param javaFile Java 소스 파일
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
  * @return 실행결과
 * @see CompileResult
 */
	private CompileResult generate(File javaFile, Record params, List<String> classPaths, String baseRealPath, boolean tomcat10) {
		PrintStream source = null;
		String error = null;
		try {
			source = new PrintStream(javaFile, "UTF-8");
			source.println("package kr.graha.sample.javaexecutor;");
			source.println();
			source.println("//import kr.graha.post.interfaces.Processor;");
			source.println("import java.sql.Connection;");
			source.println("import kr.graha.post.lib.Record;");
			if(tomcat10) {
				source.println("import jakarta.servlet.http.HttpServletRequest;");
				source.println("import jakarta.servlet.http.HttpServletResponse;");
			} else {
				source.println("import javax.servlet.http.HttpServletRequest;");
				source.println("import javax.servlet.http.HttpServletResponse;");
			}
			source.println("import java.io.PrintStream;");
			if(params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, "source"))) {
				StringTokenizer st = new StringTokenizer(params.getString(Record.key(Record.PREFIX_TYPE_PARAM, "source")));
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
			String className = javaFile.getName().substring(0, javaFile.getName().lastIndexOf("."));
			source.println();
			source.println("public class " + className + " implements kr.graha.sample.javaexecutor.Executable, java.io.Serializable {");
			source.println("//public class " + className + " implements Processor {");
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
			source.println(params.getString(Record.key(Record.PREFIX_TYPE_PARAM, "contents")));
			source.println();
			source.println("	}");
			
			source.println("//java -classpath " + getJarPath(classPaths, "servlet-api.") + ":" + getJarPath(classPaths, "graha.") + ":" + getJarPath(classPaths, "graha-java-executor.") + ":" + baseRealPath + " \\");
			source.println("//kr.graha.sample.javaexecutor." + className + "");
			source.println("	public static void main(String[] args) throws Exception {");
			source.println("		" + className + " executable = new " + className + "();");
			source.println("		executable.execute(System.out);");
			source.println("	}");
			
			source.println("	public void execute(HttpServletRequest request, HttpServletResponse response, Record params, Connection con) {");
			source.println("		this.setRequest(request);");
			source.println("		this.setResponse(response);");
			source.println("		this.setParams(params);");
			source.println("		this.setConnection(con);");
			source.println("		this.execute(System.out);");
			source.println("	}");
			
			source.println("}");
			source.flush();
			source.close();
			source = null;
			return new CompileResult(true, error);
		} catch(Exception e) {
			error = LOG.toString(e);
		} finally {
			if(source != null) {
				source.close();
				source = null;
			}
		}
		return new CompileResult(false, error);
	}
/**
 * ECJ 를 이용해서 컴파일한다.
 
 * @param javaFile Java 소스 파일
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param classpath 컴파일 할 때 공급할 classpath
 * @return 실행결과
 * @see CompileResult
 */
	private CompileResult compileWithECJ(File javaFile, Record params, String classpath) {
		ECJJavaCompiler javaCompiler = new ECJJavaCompiler();
		return javaCompiler.compile(javaFile, params, classpath);
	}
/**
 * javac 를 이용해서 컴파일한다.
 
 * @param javaFile Java 소스 파일
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param classpath 컴파일 할 때 공급할 classpath
 * @param compiler 컴파일러 (ToolProvider.getSystemJavaCompiler())
 * @return 실행결과
 * @see CompileResult
 */
	private CompileResult compileWithSystemJavaCompiler(File javaFile, String classpath, JavaCompiler compiler) {
		SystemJavaCompiler javaCompiler = new SystemJavaCompiler();
		return javaCompiler.compile(javaFile, classpath, compiler);
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