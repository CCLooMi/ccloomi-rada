package com.ccloomi.rada.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**© 2015-2019 Chenxj Copyright
 * 类    名：MethodUtil
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2019年1月27日-上午11:45:01
 */
public class MethodUtil {
	private static final Set<Integer>objMethodSet;
	static {
		objMethodSet=new HashSet<>();
		Method[]ms=Object.class.getMethods();
		for(Method m:ms) {
			objMethodSet.add(genericMethodLongName(m).hashCode());
		}
		objMethodSet.add("java.lang.Object.clone()".hashCode());
		objMethodSet.add("java.lang.Object.finalize()".hashCode());
	}
	public static String genericMethodLongName(Method m) {
		StringBuilder sb=new StringBuilder();
		Type[]fs=m.getDeclaringClass().getInterfaces();
		if(fs.length>0) {
			for(int i=0;i<fs.length;i++) {
				try {
					Method mm=((Class<?>)fs[i]).getMethod(m.getName(), m.getParameterTypes());
					if(mm!=null) {
						m=mm;
						break;
					}
				}catch (Exception e) {}
			}
		}
		sb.append(m.getDeclaringClass().getName());
		sb.append('.').append(m.getName());
		if(m.getParameterCount()>0) {
			sb.append('(');
			Class<?>[]ptypes=m.getParameterTypes();
			for(Class<?>pt:ptypes) {
				sb.append(pt.getName()).append(',');
			}
			sb.setCharAt(sb.length()-1, ')');
		}else {
			sb.append("()");
		}
		return sb.toString();
	}
	public static boolean isObjMethod(String name) {
		return objMethodSet.contains(name.hashCode());
	}
	public static boolean isObjMethod(Method m) {
		return objMethodSet.contains(genericMethodLongName(m).hashCode());
	}
}
