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
			String compileCmd = javaFile.getPath() + " -classpath " + classpath + " -encoding " + "UTF-8";
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

