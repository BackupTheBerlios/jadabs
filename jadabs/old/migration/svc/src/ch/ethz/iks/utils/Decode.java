package ch.ethz.iks.utils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.proxy.WrappingException;

/**
 * Utility methods (mostly for reflection and typing)
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class Decode {
	
	private static Logger LOG = Logger.getLogger(Decode.class);
	
	/**
	 * Converts the java encoding character for primitive types (as defined by the Java Language Specification) to its corresponding java keyword of the primitive type in a java source file, 
	 * e.g. <code>decodePrimitiveType("Z")</code> results in <code>"boolean"</code> 
	 * @param encoding - the java encoding character for primitive types
	 * @return the java keyword of the primitive type as a String
	 */
	public static String decodePrimitiveType(char encoding) {
		switch(encoding) {
			case 'Z':	return Boolean.TYPE.toString();
			case 'B':	return Byte.TYPE.toString();
			case 'C':	return Character.TYPE.toString();
			case 'D':	return Double.TYPE.toString();
			case 'F':	return Float.TYPE.toString();
			case 'I':	return Integer.TYPE.toString();
			case 'J':	return Long.TYPE.toString();
			case 'S':	return Short.TYPE.toString();
			default:	throw new IllegalArgumentException("Invalid argument value (char encoding): Expected one of \"Z\", \"B\", \"C\", \"D\", \"F\", \"I\", \"J\", \"S\" but found "+encoding);
		}
	}
	
	/**
	 * Print preview of the given type as it occurs in a java source, e.g.
	 * For object type, this corresponds to the fully qualified class name.
	 * For primitive types, to its keyword, and for array types, to its component type
	 * appended by array designators (\"[]\") according to the dimesnsion of the array
	 * <code>
	 * String.class -> "java.lang.String"
	 * [LVector -> "java.util.Vector []"
	 * int -> "int"
	 * [[Z -> Byte [] []
	 * </code>
	 * @param type - the Class to for that the name should be printed
	 * @return a String that represents the given type in a java source file
	 * 
	 */
	public static String formatTypeName(Class type) {
					String typeName = type.getName(); // TODO cache resolved types
					String arrayDesignator = "";
					boolean isArrayType = false;
					while ( typeName.startsWith("[") ) {
						// handle Array types e.g. [[[LObject or [Z
						arrayDesignator += " []";
						typeName = typeName.substring(1); // cut off head (== "[")
						isArrayType = true;
					}
					if (isArrayType) {
						if (typeName.length() == 1) {
							typeName = Decode.decodePrimitiveType(typeName.charAt(0)); // Array of primitive types
						} else {
							typeName = typeName.substring(1); // Array of Objects: cut off leading "L"
						}
					} else typeName = type.getName();
					typeName = typeName.replace(';',' ');
					return typeName + arrayDesignator;
				}

	/** 
	 * Converts to given java keyword for a primitive type (e.g. "int") to its corresponding Object class name ("Integer")
	 * Throws an IllegalArgumentException if the given keyword is unknown.
	 * @param returnTypeIdentifier - the java keyword designating a primitive type in a java source file
	 * @throws IllegalArgumentException if the given <code>keywordOfPrimitiveType</code> is unknown, not equals any of "byte", "boolean", "char", "double", "float", "int", "long", short", "void"
	 * @return the identifier of the class that wraps the primitive type with the given <code>keywordOfPrimitiveType</code> as object 
	 */
	public static String primitiveTypeKeywordToObjectTypeName(String keywordOfPrimitiveType) {
		if (keywordOfPrimitiveType.equals("byte")) {
			return "Byte";
		} else if (keywordOfPrimitiveType.equals("boolean")) {
			return "Boolean";
		} else if (keywordOfPrimitiveType.equals("char")) {
			return "Character";
		} else if (keywordOfPrimitiveType.equals("double")) {
			return "Double";
		} else if (keywordOfPrimitiveType.equals("float")) {
			return "Float";
		} else if (keywordOfPrimitiveType.equals("int")) {
			return "Integer";
		} else if (keywordOfPrimitiveType.equals("long")) {
			return "Long";
		} else if (keywordOfPrimitiveType.equals("short")) {	
			return "Short";
		} else if (keywordOfPrimitiveType.equals("void")) {
			return "Void";
		} else {
			throw new IllegalArgumentException("Invalid argument value (String keywordOfPrimitiveType): Expected one of \"byte\", \"boolean\", \"char\", \"double\", \"float\", \"int\", \"short\", \"long\" but found "+keywordOfPrimitiveType);
		}
	}
	
		/**
	 * Utility to get the runtime types of multiple Objects.
	 * Returns them in the same order as given. In other words, the following conditin holds for int i, 0 <= i < args.length 
	 * <code>args[i].getClass() == Decode.getClasses(args)[i]</code>
	 * 
	 * @param args - an array containing objects for that the type should be determined 
	 * @return an array of classes containg the objects runtime types
	 */
	public static Class[] getClasses(Object[] args) {
		if (args == null)
			return null;
		Class[] params = new Class[args.length];
		for (int pIndex = 0; pIndex < args.length; pIndex++) {
			if (args[pIndex] == null) {
				params[pIndex] = Object.class;
				LOG.error("getClasses of arg ["+pIndex+"] = \'null\', assigning Object.class as its type");
				continue;
			} 
			Class c = args[pIndex].getClass();
			
			if (c.isArray()) {
				throw new RuntimeException("Array types are not yet implemented");
			}
			else if (c.equals(Boolean.class)) {
				params[pIndex] = Boolean.TYPE;
			}
			else if (c.equals(Byte.class)) {
				params[pIndex] = Byte.TYPE;
			}
			else if (c.equals(Character.class)) {
				params[pIndex] = Character.TYPE;
			}
			else if (c.equals(Integer.class)) {
				params[pIndex] = Integer.TYPE;
			}
			else if (c.equals(Long.class)) {
				params[pIndex] = Long.TYPE;
			}
			else if (c.equals(Float.class)) {
				params[pIndex] = Float.TYPE;
			}
			else if (c.equals(Double.class)) {
				params[pIndex] = Double.TYPE;
			}
			else if (c.equals(Short.class)) {
				params[pIndex] = Short.TYPE;
			} else {
				params[pIndex] = c;
			}
		}
		return params;
	}
	
	
	/**
	 * The utility methods toObjectArray() and toPrimitiveArray() are used to automatically wrap and unwrap
	 * primitive arrays to object arrays and vive versa.
	 * 
	 * @param primitiveArray - an array of int
	 * @return an array of Integers for that 
	 * <code>primitiveArray[i] == toObjectArray(primitiveArray)[i].intValue()</code>
	 * holds for all int [] primitiveArray and int i, 0 <= i < primitiveArray.length
	 */
	public static Integer[] toObjectArray(int [] primitiveArray) {
		Integer [] objectArray = new Integer[primitiveArray.length];
		for (int i = 0; i < primitiveArray.length; i++) {
			objectArray[i] = new Integer(primitiveArray[i]);
		}
		return objectArray;
	}

	public static Boolean[] toObjectArray(boolean [] primitiveArray) {
		Boolean [] objectArray = new Boolean[primitiveArray.length];
			for (int i = 0; i < primitiveArray.length; i++) {
				objectArray[i] = new Boolean(primitiveArray[i]);
			}
			return objectArray;
	}
	
	public static Character[] toObjectArray(char [] primitiveArray) {
		Character [] objectArray = new Character[primitiveArray.length];
			for (int i = 0; i < primitiveArray.length; i++) {
				objectArray[i] = new Character(primitiveArray[i]);
			}
			return objectArray;
		}
		
	public static Byte[] toObjectArray(byte [] primitiveArray) {
		Byte [] objectArray = new Byte[primitiveArray.length];
			for (int i = 0; i < primitiveArray.length; i++) {
				objectArray[i] = new Byte(primitiveArray[i]);
			}
			return objectArray;
		}
		
	public static Long[] toObjectArray(long [] primitiveArray) {
		Long [] objectArray = new Long[primitiveArray.length];
			for (int i = 0; i < primitiveArray.length; i++) {
				objectArray[i] = new Long(primitiveArray[i]);
			}
			return objectArray;
		}
		
	public static Short[] toObjectArray(short [] primitiveArray) {
		Short [] objectArray = new Short[primitiveArray.length];
			for (int i = 0; i < primitiveArray.length; i++) {
				objectArray[i] = new Short(primitiveArray[i]);
			}
			return objectArray;
		}
		
	public static Double[] toObjectArray(double [] primitiveArray) {
		Double [] objectArray = new Double[primitiveArray.length];
			for (int i = 0; i < primitiveArray.length; i++) {
				objectArray[i] = new Double(primitiveArray[i]);
			}
			return objectArray;
		}
		
	public static Float[] toObjectArray(float [] primitiveArray) {
		Float [] objectArray = new Float[primitiveArray.length];
				for (int i = 0; i < primitiveArray.length; i++) {
					objectArray[i] = new Float(primitiveArray[i]);
				}
				return objectArray;
			}
		
		
		
	/**
	 * @param objectArray - an array of Integer
	 * @return an array of int for that 
	 * <code>objectArray[i].intValue() == toPrimitiveArray(objectArray)[i]</code>
	 * holds for all Integer [] objectArray and int i, 0 <= i < objectArray.length
	 */	
	public static int[] toPrimitiveArray(Integer [] objectArray) {
		int [] primitiveArray = new int[objectArray.length];
		for (int i = 0; i < primitiveArray.length; i++) {
			primitiveArray[i] = objectArray[i].intValue();
		}
		return primitiveArray;
	}

	public static boolean[] toPrimitiveArray(Boolean [] objectArray) {
		boolean [] primitiveArray = new boolean[objectArray.length];
			for (int i = 0; i < primitiveArray.length; i++) {
				primitiveArray[i] = objectArray[i].booleanValue();
			}
			return primitiveArray;
	}
	
	public static char[] toPrimitiveArray(Character [] objectArray) {
		char [] primitiveArray = new char[objectArray.length];
			for (int i = 0; i < primitiveArray.length; i++) {
				primitiveArray[i] = objectArray[i].charValue();
			}
			return primitiveArray;
		}
		
	public static byte[] toPrimitiveArray(Byte [] objectArray) {
		byte [] primitiveArray = new byte[objectArray.length];
			for (int i = 0; i < primitiveArray.length; i++) {
				primitiveArray[i] = objectArray[i].byteValue();
			}
			return primitiveArray;
		}
		
	public static long[] toPrimitiveArray(Long [] objectArray) {
		long [] primitiveArray = new long[objectArray.length];
			for (int i = 0; i < primitiveArray.length; i++) {
				primitiveArray[i] = objectArray[i].longValue();
			}
			return primitiveArray;
		}
		
	public static short[] toPrimitiveArray(Short [] objectArray) {
		short [] primitiveArray = new short[objectArray.length];
			for (int i = 0; i < primitiveArray.length; i++) {
				primitiveArray[i] = objectArray[i].shortValue(); 
			}
			return primitiveArray;
		}
		
	public static double[] toPrimitiveArray(Double [] objectArray) {
		double [] primitiveArray = new double[objectArray.length];
			for (int i = 0; i < primitiveArray.length; i++) {
				primitiveArray[i] = objectArray[i].doubleValue();
			}
			return primitiveArray;
		}
		
	public static float[] toPrimitiveArray(Float [] objectArray) {
		float [] primitiveArray = new float[objectArray.length];
				for (int i = 0; i < primitiveArray.length; i++) {
					primitiveArray[i] = objectArray[i].floatValue();
				}
				return primitiveArray;
	}



	/**
	 * Utility method to access the package profix of a fully qualified classname., e.g.
	 * <code>getPackage(Vector.class.getName())</code> results in <code>"java.util"</code>
	 * @param fullyQualifiedClassName the fully qualified name of a class
	 * @return the name of the package the class with the given name is defined in
	 */
	public static String getPackage(String fullyQualifiedClassName) {
		int packageEnd = fullyQualifiedClassName.lastIndexOf('.');
		if (packageEnd > 1) return fullyQualifiedClassName.substring(0,packageEnd);
		else return fullyQualifiedClassName;
	}
	
	/**
	 * Utility method to access the package profix of class., e.g.
	 * <code>getPackage(Vector.class)</code> results in <code>"java.util"</code>
	 * @param c - a class for that the package should be returned
	 * @return the name of a package the class with the given name is defined in
	 */
	public static String getPackage(Class c) {
		try {
			return c.getPackage().getName();
		} catch (NullPointerException e) {
			return getPackage(c.getName());
		}
	}
	
	
	/**
	 * @param fullyQualifiedClassName - the fully qualified name of a java class
	 * @return a String defingin the abstract path to a file having the same name as the given <code>fullyQualifiedClassName</code>.
	 * E.g. in UNIX, <code>getAsPath(Vector.class.getName())</code> results in <code>"java/util/Vector"</code>
	 */
	public static String getAsPath(String fullyQualifiedClassName) {
		return getPackage(fullyQualifiedClassName).replace('.',File.separatorChar)
		     + File.separator
		     + getSimpleName(fullyQualifiedClassName);
	}
	
	/**
	 * @param fullyQualifiedClassName - the fully qualified name of a java class
	 * @return the classes simple name, e.g. <code>getSimpleName(Vector.class.getName())</code> results in <code>"Vector"</code>
	 */
	public static String getSimpleName(String fullyQualifiedClassName) {
		int packageEnd = fullyQualifiedClassName.lastIndexOf('.');
		if (packageEnd > 1) return fullyQualifiedClassName.substring(packageEnd+1, fullyQualifiedClassName.length());
		else return fullyQualifiedClassName;
	}

	/**
	 * Retreives a (public) constructor matching the given argument types that is declared inside the class declaringType
	 * <li>the constructor must be overridden by the class <code>declaringType</code> in order to match
	 * <li>the constructor must take values of the types given by <code>argTypes</code>
	 * @param argTypes - a list of types of the parameters taken by the constructor we are looking for 
	 * @param declaringType - the type implementing the constructor we are looking for 
	 * @throws a WrappingException if no such Constructor is available or accessible
	 * @return a public constructor of the given class matching the given argument types 
	 */
	public static Constructor getMatchingConstructor(Class declaringType, Class[] argTypes) throws WrappingException {
		Class wrapper = declaringType;
		Constructor wrapperConstructor = null;
		// debug code start
		String params = "";
		String args = "";
		for (int i = 0; i < argTypes.length; i++) {
			args += argTypes[i].toString();
		}
		// debug code end
		try {
			// default constructor: argTypes == null
			wrapperConstructor = wrapper.getConstructor(argTypes);
			if (wrapperConstructor != null) {
				/*LOG.info("reflection found constructor "+wrapperConstructor.toString()
				         +"\n to match args "+args
				);*/
				return wrapperConstructor;
			}
		} // catch exceptions and give it another try
		catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		
			// signature has to match static types, not only dynamic ones
			Constructor[] wrapperConstructors = wrapper.getConstructors();
			for (int wIndex = 0; wIndex < wrapperConstructors.length; wIndex++) {
				Constructor constructor = wrapperConstructors[wIndex];
				Class[] paramTypes = constructor.getParameterTypes();
				int pIndex = -2;
				for (pIndex = 0; pIndex < paramTypes.length; pIndex++) {
					Class formalParam = paramTypes[pIndex];
					params += formalParam.toString(); 
					if (!(formalParam.isAssignableFrom(argTypes[pIndex]))) {
						//LOG.info("Constructor "+declaringType+"(): formal param "+formalParam+" @ "+formalParam.getClassLoader() +" is not assignable from arg "+argTypes[pIndex]+" @ "+argTypes[pIndex].getClassLoader());
						break;
					}
				}
				if (pIndex == argTypes.length) {
					//constructor signature of wrapper matches 
					wrapperConstructor = constructor;
					/*LOG.info("Decode found matching constructor "+constructor.toString()
					         +"\n with parameters "+params
					         +"\n to match args "+args
					);*/
					break;
				}
			}
		
		return wrapperConstructor; // may be null
	}
	
	/**
	 * see getMatchingConstructor(): This method cares about methods instead of constructors
	 * arguments may be not only of the formal parameter type, but any subclass of it.
	 * <!-- Returns the method that matches closest to the given argument types, meaning 
	 * the one with the most specific signature. --> Returns the first (public) method that arguments are assignable from the formal parameters
	 * and that is defined in the given class or its superclasses
	 * @param declaringType
	 * @param argTypes
	 * @return
	 * @throws WrappingException
	 */
	public static Method getMatchingMethod(String name, Class declaringType, Object[] runtimeArgs) throws WrappingException {
			Class c = declaringType;
			Method method = null;
			Class [] rtParams = Decode.getClasses(runtimeArgs);
			try {
				method = c.getMethod(name, rtParams);
			} // catch exceptions and give it another try
			catch (SecurityException e) {
			} catch (NoSuchMethodException e) {
			}
			if (method == null) {
				// signature has to match static types, not only dynamic ones
				// arguments are subtypes of formal parameters
				Method [] methods = c.getMethods();
				int paramCount = rtParams.length;
				for (int wIndex = 0; wIndex < methods.length; wIndex++) {
					Method m = methods[wIndex];
					if (!name.equals(m.getName())) {
						continue;
					}
					Class[] signature = m.getParameterTypes();
					if (signature == null) {
						continue;
					}
					else if (paramCount != signature.length) {
						continue; // method has wrong numbers of parameters
					}
					int pIndex = -1;
					for (pIndex = 0; pIndex < signature.length; pIndex++) {
						Class sig = signature[pIndex];
						if (!(sig.isAssignableFrom(rtParams[pIndex]))) {
							LOG.error(" incompatible arg "+rtParams[pIndex]+" for param "+sig+" in method "+declaringType+"."+name);
							break; // incompatible parameter
						}
					}
					if (pIndex == signature.length) {
						// signature matches all given runtime types
						method = m;
						break;
					}
				}
			}
			return method;
		}
		
		
		/**
		 * Given a class c, returns all the interfaces implemented by c.
		 * In contrast to <code>Class.getInterfaces()</code>, this method
		 * includes interfaces that are implemented by classes c extends.
		 * 
		 * Example:
		 * <code>
		 * Class c extends b implements i1, i2 { }<br>
		 * Class b implements i0 { }<br>
		 * 
		 * Class[] ifcsOfC = c.getInterfaces();
		 * Class[] ifcsOfCandB = Decode.getInterfaces(c);
		 * </code>
		 * While ifcsOfB contains just i0, ifcsOfCandB includes i1 and i2 as well 
		 * 
		 * @param c
		 * @return
		 */
		public static Class[] getInterfaces(Class c) {
			Vector implByAll = new Vector();
			
			while(c != Object.class) {
				Class[] implByC = c.getInterfaces();
				for (int i = 0; i < implByC.length; i++) {
					implByAll.add(implByC[i]);
				}
				c = c.getSuperclass();
			}
			Class[] ifcs = new Class[implByAll.size()];
			implByAll.toArray(ifcs);
			return ifcs;
		}


}
