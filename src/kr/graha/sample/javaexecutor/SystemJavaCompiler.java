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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.JavaFileObject;
import java.nio.charset.StandardCharsets;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import java.io.StringWriter;

/**
 * Java Executor Java Compilier
 * javac 를 이용해서 Java 파일을 컴파일한다.
 
 * .java 파일을 만들지 않고, String 형태의 Java Source 코드를 컴파일 하는 방법은 다음과 같다.
 *
 * http://www.java2s.com/Code/Java/JDK-6/CompilingfromMemory.htm
 * https://stackoverflow.com/questions/12173294/compile-code-fully-in-memory-with-javax-tools-javacompiler
 * https://stackoverflow.com/questions/21544446/how-do-you-dynamically-compile-and-load-external-java-classes
 * https://stackoverflow.com/questions/3447359/how-to-provide-an-interface-to-javacompiler-when-compiling-a-source-file-dynamic

 * @author HeonJik, KIM
 
 * @version 0.9
 * @since 0.9
 */

public class SystemJavaCompiler {
	protected CompileResult compile(File javaFile, String classpath, JavaCompiler compiler) {
		StringWriter sw = null;
		String error = null;
		try {
			sw = new StringWriter();

			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
			Iterable<? extends JavaFileObject> unit = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(javaFile));
			List<String> option = new ArrayList<String>();
			if(classpath == null) {
				option.addAll(Arrays.asList("-encoding", "UTF-8"));
			} else {
				option.addAll(Arrays.asList("-classpath", classpath, "-encoding", "UTF-8"));
			}
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
				error = LOG.toString(e);
			}
		} finally {
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

