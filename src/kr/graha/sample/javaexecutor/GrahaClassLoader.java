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

import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.io.PrintStream;
import java.net.MalformedURLException;

/**
 * Graha ClassLoader
 * Java Executor 에서 사용하는 ClassLoader

 * @author HeonJik, KIM
 * @version 0.9
 * @since 0.9
 */

public class GrahaClassLoader extends URLClassLoader {
	public GrahaClassLoader(URL[] url, ClassLoader loader) {
		super(url, loader);
	}
	public void addURL(String path) throws MalformedURLException {
		File f = new File(path);
		this.addURL(f);
	}
	public void addURL(File f) throws MalformedURLException {
		if(f.exists()) {
			this.addURL(f.toURI().toURL());
		}
	}
	public void addURL(URL url) {
		super.addURL(url);
	}
/**
 * defineClass 를 다르게 정의한다.
 * defineClass 는 protected final 이기 때문에 이런 방식으로만 우회할 수 있다.
 * GrahaClassLoader 를 작성해야 하는 가장 중요한 이유이다. 
 
 * @param p 패키지 이름
 * @param classFile Java class 파일
 * @return 파일에서 읽어들인 Class
 */
	public Class<?> defineClass(String p, File classFile) throws ClassFormatError, IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(classFile.getPath());
			byte rawBytes[] = new byte[fis.available()];
			fis.read(rawBytes);
			Class<?> c = null;
			c = super.defineClass(p + "." + classFile.getName().substring(0, classFile.getName().lastIndexOf(".")) + "", rawBytes, 0, rawBytes.length);
			fis.close();
			fis = null;
			return c;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			if(fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}
/**
 * Class의 모든 메소드의 정보를 출력한다.
 * 다음과 같이 호출한다.
 * print(c, System.out);
 * 사용하지 않는다.
 * 개발초창기에 debug 용도로 사용했다.
 
 * @param c Class
 * @param out 출력할 스트림
 */
	public void print(Class c, PrintStream out) {
		Method[] methods = c.getMethods();
		for( Method method : methods ){
			this.print(method, out);
		}
	}
/**
 * 메소드 이름만으로 메소드를 가져온다.
 * @param c Class
 * @param name 메소드 이름
 * @return Method
 */
	public Method getMethodByName(Class c, String name) {
		Method[] methods = c.getMethods();
		for(Method method : methods) {
			if((method.getName()).equals(name)) {
				return method;
			}
		}
		return null;
	}
/**
 * 메소드의 정보를 출력한다.
 * 다음과 같이 호출한다.
 * print(method, System.out);
 * 사용하지 않는다.
 * 개발초창기에 debug 용도로 사용했다.
 
 * @param method 메소드
 * @param out 출력할 스트림
 */
	public void print(Method method, PrintStream out) {
		out.print(method.getName());
		Class<?>[] argTypes = method.getParameterTypes();
		out.print("(");
		int size = argTypes.length;
		for(Class<?> argType : argTypes) {
			String argName = argType.getName();
			out.print(argName + " val");
			if( --size != 0 ){
				out.print(", ");
			}
		}
		out.print(")");
		Class<?> returnType = method.getReturnType();
		out.println(" : " + returnType.getName());
	}
/**
 * 서로 다른 ClassLoader 간의 같은 Class 를 캐스팅한다.
 * 사용하지 않는다.
 * defineClass로 가져온 Class Object를 파라미터로 공급해도, 메모리에 캐쉬된 것을 리턴한다.
 * classpath 에 없는 Class를 파라미터로 공급하면 readObject 메소드를 호출할 때 ClassNotFoundException이 발생한다.
 
 * @param o 캐스팅 할 Class Object
 * @return 캐스팅 한 Class
 */
	public <T> T cast(Object o) throws IOException, ClassNotFoundException {
		if(o == null) {
			return null;
		}
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
//			oos.writeObject(o);
			oos.writeUnshared(o);
			oos.flush();
			byte[] b = baos.toByteArray();
			oos.close();
			oos = null;
			baos.close();
			baos = null;
			if (b != null && b.length > 0) {
				bais = new ByteArrayInputStream(b);
				ois = new ObjectInputStream(bais);
//				T res = (T)ois.readObject();
				T res = (T)ois.readUnshared();
				ois.close();
				ois = null;
				bais.close();
				bais = null;
				return res;
			}
		} catch(Exception e) {
			throw e;
		} finally {
			if(ois != null) {
				try {
					ois.close();
				} catch (Exception e) {
				}
			}
			if(bais != null) {
				try {
					bais.close();
				} catch (Exception e) {
				}
			}
			if(oos != null) {
				try {
					oos.close();
				} catch (Exception e) {
				}
			}
			if(baos != null) {
				try {
					baos.close();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}
}