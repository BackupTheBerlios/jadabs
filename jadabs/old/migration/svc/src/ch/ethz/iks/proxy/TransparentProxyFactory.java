package ch.ethz.iks.proxy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.adapter.DefaultAdapter;
import ch.ethz.iks.evolution.adapter.IAdapter;
import ch.ethz.iks.evolution.adapter.cop.AdapterClassLoader;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResourceFactory;
import ch.ethz.iks.evolution.state.StateOfComponent;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.proxy.cop.ProxyComponentResource;
import ch.ethz.iks.proxy.cop.ProxyComponentResourceFactory;
import ch.ethz.iks.utils.CallByReferenceArgument;
import ch.ethz.iks.utils.Decode;

/**
 * Creates a dynamic proxy by providing a similar API as <code>java.lang.reflect.Proxy</code>.
 * In addition to the API provided by Java, the proxies superclass and name may be specified.
 * Thus, the class the proxy was created for is assignable from the resulting proxy class.
 * This has the advantage that another class instatiating a proxy object does not have to care about typing issues,
 * e.g. a predefined method that takes an argument of the class the proxy was created for
 * may be invoked with the proxy object as well.
 * Say we want to create a proxy for our class <code>VectorOfint</code> named <code>proxy4_VectorOfint</code>.
 * Assume <code>VectorOfint</code> extends <code>java.util.Vector</code>.
 * With proxy <code>proxy</code> created using the java dynamic proxy API, e.g. <code>Proxy proxy = java.lang.reflect.Proxy.newProxyInstance(\/*args*\/);</code>, 
 * you cannot compile any of the following statements:
 * <p>
 * <code>javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(proxyOfVector, 10);</code></br>
 * <br><code>Vector ints = proxy;</code></p>
 * In constrast, if you create a proxy using this factory, e.g. 
 * <p><code>Vector moreInts = TransparentProxyFactory.newProxyInstance(proxyLoader, java.util.Vector.class, invocationHandler, constructorArgs);</code><br>
 * <code>DefaultTableModel model = new DefaultTableModel(moreInts, 10);</code>
 * </p>
 * works fine.
 *
 *  * @see ch.ethz.iks.evolution.jadabs.ExternalReferenceScanner
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */

public class TransparentProxyFactory {

	private final static String handlerClass = IAdapter.class.getName();
	private final static String implementingMethodName = "invoke"; // Name of Method in InvocationHandler
	
	private static final String newLine = " \n ";
	private static final String objectArraySuffix = "_Array";
	public static final String METHOD_CONST_PREFIX = "METHOD_";
	public static final String factoryMethod = "createExternal";
	private static boolean isIfcHidingEnabled = false;
	private static HashMap proxies = new HashMap(); // caches proxy classes 
	private static Logger LOG = Logger.getLogger(TransparentProxyFactory.class);

	


	// without original (hidden object does not yet exist => to be created on registration) 
	public static IProxy newProxyInstance(IClassLoaderWithClassPath loader, Class toHide, Object[] initArgs) throws Exception {

		Class proxyClass = getProxyClass(loader, toHide);
		Class[] paramTypes = Decode.getClasses(initArgs);

		// look for the constructor of the proxy that takes the same parameter types as the hidden object to be created		
		Constructor constructor = Decode.getMatchingConstructor(proxyClass, paramTypes);
		IProxy proxy = (IProxy) constructor.newInstance(initArgs); //registers proxy

		return proxy;
	}

	// incl. original (hidden object exists already, use copyconstructor of proxy)
	public static IProxy newProxyInstance(ClassLoader loader, Object toHide) throws Exception {

		// last arg matches to copy constructor
		return newProxyInstance((IClassLoaderWithClassPath)loader, toHide.getClass(), new Object[] { toHide });
	}

	/**
	 * As the java dynamic proxy, this method just implements the interfaces given.
	 * However, it adds funcionality to use it in ProxyRegistry maps and allows exchanging its hanlder
	 * Abstract methods of superclass is also implemented
	 * 
	 * No ifc belonging to the same component as the Object toHide must be given in the interface list
	 * 
	 * @param loader
	 * @param ifcsToImpl
	 * @param toHide
	 * @return a transparent proxy that implements the ifcsToImpl
	 * @throws Exception
	 */
	public static IProxy newProxyInstance(IClassLoaderWithClassPath loader, Class[] ifcsToImpl, Object toHide) throws Exception {
		// last arg matches to copy constructor
		boolean didHideIfcs = TransparentProxyFactory.isIfcHidingEnabled;
		if (ifcsToImpl != null) {
			setIfcHiding(false);
		}
		Class proxyClass = getProxyClass(loader, ifcsToImpl, toHide.getClass());

		// look for the constructor of the proxy that takes the same parameter types as the hidden object to be created		
		Constructor constructor = Decode.getMatchingConstructor(proxyClass, new Class[] { Object.class });
		LOG.info("implemented ifcs, instantiating proxy with " + constructor);
		IProxy proxy = (IProxy) constructor.newInstance(new Object[] { toHide }); //registers proxy
		setIfcHiding(didHideIfcs);
		return proxy;

	}

	public static Class getProxyClass(IClassLoaderWithClassPath loader, Class toImplementAProxyFor) throws WrappingException {
		return getProxyClass(loader, null, toImplementAProxyFor);
	}

	/**
	 * main method to create a transparent proxy class that hides shyClass
	 * @param shyClass
	 * @param implementator
	 * @return
	 */
	public static Class getProxyClass(IClassLoaderWithClassPath loader, Class[] ifcsToImpl, Class toImplementAProxyFor)
		throws WrappingException {
		Class shyClass = toImplementAProxyFor; // class to be hidden
		CallByReferenceArgument copContainer = new CallByReferenceArgument();
		
		// first check if supertypes must be wrapped, too
		if (belongToSameCop(shyClass.getSuperclass(), copContainer, shyClass.getName())) {
			getProxyClass(loader, shyClass.getSuperclass());
		}
		// do not hide interfaces
		if (!isIfcHidingEnabled && shyClass.isInterface()) {
			return null;
		}
		// do hide interfaces
		try {
			
			Class[] ifcs = shyClass.getInterfaces();
			for (int i = 0; i < ifcs.length; i++) {
				if (belongToSameCop(ifcs[i], copContainer, shyClass.getName())) {
					getProxyClass(loader, ifcs[i]);
				}
			}

			//LOG.info("getProxyClass: creating a proxy for " + shyClass);
			String nameOfWrapper = Decode.getSimpleName(shyClass.getName()); ///getNamingOfWrapper(shyClass);
			Class transparentProxy = (Class) proxies.get(loader.getClassPath() + shyClass.getName()); // map by type , distinct incoming from outgoing proxies (ifcsToImpl)
			

			if (transparentProxy != null) {
				// proxy class already exists, TODO: test if binary is still there
				LOG.info(loader+" returning cached proxy "+transparentProxy);
				// load class using given ClassLoader
				return loader.loadClass(transparentProxy.getName());
			}

			//	write source to file
			String sourceFile = nameOfWrapper + ".java";

			String pathToSourceFile = createPackages(shyClass);
			File javaFile = new File(pathToSourceFile + sourceFile);
			if (javaFile.exists()) {
				//javaFile.renameTo(new File(javaFile.getPath()+".bak"));
				javaFile.delete();
			}
			FileWriter source = new FileWriter(javaFile);

			appendClassHeader(shyClass, ifcsToImpl, source);
			source.write("{ " + newLine); // start of body of class

			if (!shyClass.isInterface()) {
				appendMembers(shyClass, source, loader);

				Constructor[] publicConstructors = shyClass.getConstructors();

				boolean isCopyConstructorDefined = false;
				for (int cIndex = 0; cIndex < publicConstructors.length; cIndex++) {
					if (publicConstructors[cIndex].getParameterTypes().equals(new Class[] { Object.class })) {
						isCopyConstructorDefined = true;
					}
					appendSignature(publicConstructors[cIndex], source);
					source.write("    { " + newLine);
					appendBody(publicConstructors[cIndex], source, shyClass, loader); // implementation of constructor
					source.write("    } " + newLine); // end of method 

				}

				if (!isCopyConstructorDefined) {
					// add a copy  constructor ProxyClass(Object toHide) to be able to create a proxy given an existing object to be hidden
					source.write(
						newLine
							+ "    public "
							+ Decode.getSimpleName(shyClass.getName())
							+ "(Object toHide) {"
							+ newLine);
					source.write("        super(");
					Class superC = shyClass.getSuperclass();

					if (superC != null && proxies.containsKey(loader.getClassPath() + superC.getName())) {
						source.write(" toHide ); " + newLine);
						// superclass is a proxy, too => has also a copy constructor
					} else {
						//source.write("     System.out.println(\"3 copyconstructor registers proxy for original\");" + newLine);
						// just register once in hierarchy (on topmost superclass that isa proxy => is unique)
						source.write(
							");"
								+ newLine + "        "
								+ Decode.getSimpleName(DefaultAdapter.class.getName())
								+ ".Instance().register( ("
								+ IProxy.class.getName()
								+ ")this, new Object [] {toHide});"
								+ newLine);
					}
					source.write(" }" + newLine);
				}
			}

			Iterator iter = getPublicMethodsOf(shyClass, ifcsToImpl); // public methods of superclass and interfaces

			while (iter.hasNext()) {
				Method publicMethod = (Method) iter.next();
				int mod = publicMethod.getModifiers();

				// implement methods of interface IProxy and ifcsToImpl (if available)
				boolean needsImpl = publicMethod.getDeclaringClass().isAssignableFrom(IProxy.class) && publicMethod.getDeclaringClass() != Object.class; //publicMethod.getDeclaringClass().equals(IProxy.class);
				for (int aIndex = 0; ifcsToImpl != null && aIndex < ifcsToImpl.length; aIndex++) {
					needsImpl |= publicMethod.getDeclaringClass().equals(ifcsToImpl[aIndex]);
				}

				if (!(Modifier.isFinal(mod)) && (!(Modifier.isNative(mod)))) {
					appendSignature(publicMethod, source);
					if (shyClass.isInterface() || (Modifier.isAbstract(mod) && !needsImpl)) {
						source.write(" ; " + newLine);
					} else {
						source.write("    { " + newLine); // implementation of method
						appendBody(publicMethod, source, shyClass, loader);
						source.write("    } " + newLine); // end of method 
					}
				}
			}

			source.write(" }  // end of class ");
			//source.flush(); 
			source.close();
			String packageName = Decode.getPackage(shyClass);
			String className = packageName + "." + nameOfWrapper;
			//LOG.info("Compiling " + className + " to " + pathToSourceFile + sourceFile);

			transparentProxy = doCompile(loader, className, pathToSourceFile + sourceFile, shyClass);

			proxies.put(loader.getClassPath() + shyClass.getName(), transparentProxy); // cache proxy class
			System.gc();
			return transparentProxy;

		} catch (WrappingException w) {
			LOG.error("Caught ", w);
			throw w;
		} catch (Throwable e) {
			LOG.error("getProxyClass: Caught " + e.getClass(), e);

			WrappingException w = new WrappingException(e);
			throw w;
		}
	}

	private static void appendMembers(Class shyClass, FileWriter source, IClassLoaderWithClassPath loader) throws IOException {
		
		// declare adapter members (this.implementator and staticimplementator)
		source.write(newLine + "    private " + handlerClass + " implementator = null;" + newLine);
		source.write(
			"    private static "
				+ handlerClass
				+ " staticImplementator = "
				+ Decode.getSimpleName(DefaultAdapter.class.getName())
				+ ".Instance();"
				+ newLine);

		// declare hashvalue member to implement hash() in proxy class at root of proxy class hierarchy only
		if (shyClass.getSuperclass() == null || proxies.get(loader.getClassPath() + shyClass.getSuperclass().getName()) == null) {
			source.write("    protected int hashValue = System.identityHashCode(this);" + newLine);
		}
		// Logger member used for benchmarking only
		source.write("    private static Logger bmLog = Logger.getLogger(\"ProxyBenchmark\");" + newLine);
	}

	/**
	 * DEPRECATED
	 * 
	 * Writes the java sourcecode of a factory method that invokes the (private) constructor having
	 * the same parameter types and returns this instance (typed as its supertype)
	 * @param constructor
	 * @param source
	 
	private static void appendFactory(Constructor constructor, Class shyClass, FileWriter source) throws IOException {
		source.write(newLine +" public static "+ constructor.getName() +" "+ TransparentProxyFactory.factoryMethod +" ( ");
		String params = formatParameterList(constructor.getParameterTypes(), true, false);
		source.write(params);
		source.write(" ) { "+ newLine);
		source.write("    "+ TransparentProxyFactory.getNamingOfWrapper(shyClass) +" proxy = new "+ TransparentProxyFactory.getNamingOfWrapper(shyClass) +" ( " );
		params = formatParameterList(constructor.getParameterTypes(), false, false);
		source.write(params);
		source.write(" );"+ newLine); 
		source.write("    ch.ethz.iks.evolution.step.DefaultComponentEvolution.register((ch.ethz.iks.evolution.step.IProxy)proxy);" + newLine);
		source.write("    return proxy;"+ newLine); 
		source.write(newLine +" } "+ newLine);
	}*/

	private static String createPackages(Class shyClass) throws WrappingException {
		String packagePrefix = shyClass.getName().replace('.', File.separatorChar);
		int prefix = packagePrefix.indexOf(File.separatorChar);
		String packageName = "";
		String path = "";
		while (prefix != -1) {
			packageName = packagePrefix.substring(0, prefix);
			packagePrefix = packagePrefix.substring(prefix + 1, packagePrefix.length());
			File f = new File(path + packageName);
			if (!f.exists()) {
				boolean created = f.mkdir();
				if (!created) {
					LOG.error("Could not create the folder " + f.getName() + "in the directory " + f.getParent());
					throw new WrappingException(
						"Could not create the folder " + f.getName() + "in the directory " + f.getParent());
				} else
					LOG.info("creating proxy packages: mkdir " + packageName);
			}
			if (!f.canWrite())
				LOG.error("No permission to write to folder " + f.getPath());
			path += packageName + File.separatorChar;
			prefix = packagePrefix.indexOf(File.separatorChar);
		}
		return path;
	}

	private static void appendBody(AccessibleObject operation, FileWriter source, Class shyClass, IClassLoaderWithClassPath loader) throws IOException {
		String name;
		Class[] argTypes;
		Class returnType = Void.TYPE;
		String returnValue = "returnValue";
		Class[] declaredExceptions = null;
		boolean isStatic = false;
		String className;
		boolean needsImpl = false;
		boolean alreadyReturned = false;
		if (operation instanceof Method) {
			Method m = (Method) operation;
			argTypes = m.getParameterTypes();
			name = m.getName();
			returnType = m.getReturnType();
			isStatic = Modifier.isStatic(m.getModifiers());
			className = m.getDeclaringClass().getName();
			declaredExceptions = m.getExceptionTypes();
			needsImpl = m.getDeclaringClass().isAssignableFrom(IProxy.class)  && m.getDeclaringClass() != Object.class; //(m.getDeclaringClass().equals(IProxy.class)); // Methods of IProxy
			String params = formatParameterList(argTypes, false, true); // argument values to pass to handler

			if (needsImpl && !shyClass.isInterface()) { // interface methods, e.g. IProxy.setInvocationHandler()
				implementIProxy(((Method) operation), params, source, shyClass, loader);
				return;
			} else {
				//source.write("System.out.println(\"I am a proxy\"); "+newLine);
				
				//source.write(" java.lang.reflect.Method dispatched = null; " + newLine);
				source.write("    try { " + newLine);
				///source.write("   if (dummy == null) { System.out.println(\" dummy is null!\"); }" + newLine); debugging only
				
				//source.write(" dispatched = "+Decode.getSimpleName(className) +".class.getDeclaredMethod(");

				//source.write("\"" + name + "\", ");
				if (argTypes.length == 0) {
					//source.write("null ); " + newLine);
				} else {
					String conversionCode = new String(); // convert primitive array arguments to Object arrays
					//source.write("new Class [] {"); // name of method 
					String listSeparator = " ";

					for (int pIndex = 0; pIndex < argTypes.length; pIndex++) { // parameter types
						//source.write(listSeparator);
						listSeparator = ", ";
						if (argTypes[pIndex].isPrimitive()) {
							//source.write(argTypes[pIndex].toString() + ".class");
						} else if (argTypes[pIndex].isArray()) {
							Class simple = argTypes[pIndex].getComponentType();
							if (simple.isArray()) {
								LOG.error("Multidimensional arrays are not yet supported");
								throw new RuntimeException("Multidimensional arrays are not yet supported");
							} else if (simple.isPrimitive()) { // primitive array type
								//source.write(simple.toString() + "[].class");
								conversionCode += getCodeFor_toArrayObject(argTypes[pIndex], simple, pIndex);
							} else {
								String objArray = argTypes[pIndex].getName();
								int endIndex = objArray.length();
								if (objArray.endsWith(";"))
									endIndex--;
								objArray = objArray.substring(2, endIndex);
								//source.write(objArray + " [].class ");
							}
						} else {
							//source.write(argTypes[pIndex].getName() + ".class");
						}
					}
					//source.write("} );" + newLine); // pass method name instead of reflection
					///source.write("bmLog.info(\"<td>proxy body: resolved method</td><td>\"+System.currentTimeMillis()+\"</td>\");");
					source.write(conversionCode);
				}
				// declares Object arrays, fills them with the data of the incoming primitive array to be ready to pass the object arrays to the handler
				//	Returntype of the wrapped method                         
				String returnTypeIdentifier = returnType.getName();
				// prepare return value
				if (returnType != Void.TYPE) {
					if (returnType.isArray()) {
						Class simple = returnType.getComponentType();
						returnTypeIdentifier = Decode.formatTypeName(returnType);
						if (simple.isPrimitive()) { // array of prmitive types
							returnTypeIdentifier = simple.toString(); // "int"
							returnTypeIdentifier = Decode.primitiveTypeKeywordToObjectTypeName(returnTypeIdentifier);
							// "Integer"
							returnTypeIdentifier += " [] ";
						}
					} else if (returnType.isPrimitive()) {
						returnTypeIdentifier = returnType.toString(); // "int"
						returnTypeIdentifier = Decode.primitiveTypeKeywordToObjectTypeName(returnTypeIdentifier);
						// "Integer"
					}
					///source.write("bmLog.info(\"<td>proxy body: invoking handler</td><td>\"+System.currentTimeMillis()+\"</td>\");");
					///until handler: source.write("return null; "+ newLine);
					// assign to (prepared) return value variable
					source.write("        " + returnTypeIdentifier + " " + returnValue + " = ");
					if (!returnType.equals(Object.class))
						// cast return value of handler method to declared return type
						source.write(" (" + returnTypeIdentifier + ") ");
				} else {
					alreadyReturned = true; // hide return stmt
				}
			} //needsImpl

			// invoke handler method
			if (isStatic) {
				source.write(" staticImplementator." + implementingMethodName + "( null ");
				// must be null on static calls (NOT dummy)
			} else {
				source.write(" implementator." + implementingMethodName + "( this ");
			}
			// pass id instead of String
			source.write(",  " + getMethodId(m) + ", \"" + m.getDeclaringClass().getName() + "\", ");
			// pass name of method instead of method itself
			if (argTypes.length == 0) {
				source.write(" null ");
			} else {
				source.write(" new Object [] { ");
				source.write(params);
				source.write(" } ");
			}
			source.write(" ); " + newLine);
			///source.write("bmLog.info(\"<td>proxy body: invoked handler</td><td>\"+System.currentTimeMillis()+\"</td>\");");
			// return the (prepared) variable 
			if (returnType.isPrimitive()) {
				// unwrap the primitive type
				String keywordForPrimitiveType = returnType.toString(); // "int"
				returnValue = returnValue + "." + keywordForPrimitiveType + "Value()";
			} else if (returnType.isArray()) {
				String returnTypeName = returnType.getComponentType().getName();
				//int i = returnTypeName.indexOf(' '); // cut off leading "class " or "interface "
				//returnTypeName = returnTypeName.substring(i  != -1 ? i : 0 , returnTypeName.length());

				if (returnType.getComponentType().isPrimitive()) { // array of primitive types
					returnTypeName = returnType.getComponentType().toString();
					String objectType = returnTypeName.substring(0, 1).toUpperCase();
					objectType += returnTypeName.substring(1, returnTypeName.length());
					//	declare primitive array to be returned
					source.write(
						returnTypeName
							+ " [] primitive_"
							+ returnValue
							+ " = new "
							+ returnTypeName
							+ " ["
							+ returnValue
							+ ".length];"
							+ newLine);
					// fill in Objects values returned by handler and convert them to the corresponding primitive values
					source.write(" for (int i = 0; i < " + returnValue + ".length; i++) {" + newLine);
					source.write(
						"  primitive_"
							+ returnValue
							+ " [i] = java.lang.reflect.Array.get"
							+ objectType
							+ " ( "
							+ returnValue
							+ "[i], i);	"
							+ newLine);
					source.write(" } " + newLine); // end of for loop
					///source.write("bmLog.info(\"<td>proxy body: returning</td><td>\"+System.currentTimeMillis()+\"</td>\");");
					source.write("    return primitive_" + returnValue + ";" + newLine);
					alreadyReturned = true;
				}
			}
			if (!alreadyReturned) {
				///source.write("bmLog.info(\"<td>proxy body: returning</td><td>\"+System.currentTimeMillis()+\"</td>\");");
				source.write("    return " + returnValue + ";" + newLine);
			}
			// exception handling: re-throw exception declared to be thrown  by method
			boolean isThrowableDeclaredToBeThrown = false;
			boolean isWrappingExceptionDeclaredToBeThrown = false;
			source.write("    } ");
			if (declaredExceptions != null) {
				for (int k=0; k<declaredExceptions.length; k++) {
					isThrowableDeclaredToBeThrown |= declaredExceptions[k] == Throwable.class;
					isWrappingExceptionDeclaredToBeThrown |= declaredExceptions[k].isAssignableFrom(WrappingException.class);
					source.write("        catch ("+declaredExceptions[k].getName()+" e) { throw e; } " + newLine);
				}
			}
			
			if (!isWrappingExceptionDeclaredToBeThrown) {
				// hide WrappingException
				source.write("     catch ( "+WrappingException.class.getName()+" w) { w.printStackTrace(); " + newLine);
								if (returnType == Void.TYPE){;
								} else if (returnType == Character.TYPE) {
									source.write("    return '0';" + newLine);
								} else if (returnType == Boolean.TYPE) {
									source.write("    return false;" + newLine);
								} else if (returnType.isPrimitive()) {
									source.write("    return 0;" + newLine);
								} else {
									source.write("    return null;" + newLine);
								}
								source.write("       }"+newLine);
			}
			if (!isThrowableDeclaredToBeThrown) {
					source.write("   catch ( "+Throwable.class.getName()+" w) { w.printStackTrace(); " + newLine);	
								if (returnType == Void.TYPE){;
								} else if (returnType == Character.TYPE) {
									source.write("    return '0';" + newLine);
								} else if (returnType == Boolean.TYPE) {
									source.write("    return false;" + newLine);
								} else if (returnType.isPrimitive()) {
									source.write("    return 0;" + newLine);
								} else {
									source.write("    return null;" + newLine);
								}
								source.write("       }"+newLine);
			}
			else { if (returnType == Void.TYPE){;
										} else if (returnType == Character.TYPE) {
											source.write("    return '0';" + newLine);
										} else if (returnType == Boolean.TYPE) {
											source.write("    return false;" + newLine);
										} else if (returnType.isPrimitive()) {
											source.write("    return 0;" + newLine);
										} else {
											source.write("    return null;" + newLine);
										}
			}
								
			
		} else if (operation instanceof Constructor) {
			Constructor c = (Constructor) operation;
			argTypes = c.getParameterTypes();
			// public constructor, pass arguments to super(...)

			///if (argTypes.length > 0) {
				String[] params = createIdentifiers(argTypes, false);
				Class superC = c.getDeclaringClass().getSuperclass();
				String[] superArgs = null;

				// TODO: check if there exists a matching constructor in the superclass
				Constructor superConstr = Decode.getMatchingConstructor(superC, argTypes);

				if (superConstr != null && superConstr.getParameterTypes().length != 0)  {
					///LOG.info("superclass "+superC+" has a matching constructor with params: "+formatParameterList(superConstr.getParameterTypes(), true, false) +" to call with args: "+formatParameterList(argTypes, true,false));
					superArgs = params;
				} // hack: superConstr should also be null in following two cases TODO: figure out why not
				if (superC.equals(Object.class)) {
					LOG.error("HACK: superclass is root");
					superArgs = null;
				} else if (superC.equals(Thread.class)) {
					LOG.error("HACK: superclass is Thread.class");
					superArgs = null;
				}
				// and call super(params[i],...) instead of super() or super(same args as this constructor), see below

				source.write("     super(");
				if (superArgs != null) {
					for (int i = 0; i < superArgs.length; i++) {
						if (i > 0) {
							source.write(", ");
						}
						source.write(superArgs[i]);
					}
				}
				source.write(" );" + newLine);
				// pass args to super, TODO: check if corresponding constructor exists in superclass

			if (superC == null || !proxies.containsKey(loader.getClassPath() + superC.getName())) {
				// prepare args to pass to registration (to create original object)
				source.write("        Object [] initArgs = new Object [] { ");
				for (int i = 0; params != null && i < params.length; i++) {

					if (argTypes[i].isPrimitive()) {
						// wrap primitive types
						String conversionCode = primitiveToObject(params[i], argTypes[i]);

						source.write(conversionCode);
					} else if (argTypes[i].isArray()) {
						Class simpleType = argTypes[i].getComponentType();
						if (simpleType.isPrimitive()) {
							String conversionCode = getCodeFor_toArrayObject(argTypes[i], simpleType, i);
							//hack: todo: adapt method to not print ; at end and variable assignment at beginning
							int startIndex = conversionCode.indexOf('=');
							int endIndex = conversionCode.indexOf(';');
							conversionCode = conversionCode.substring(startIndex + 1, endIndex);
							//LOG.info("proxy constructor primitive Array conversion: "+conversionCode);
							source.write(conversionCode);
							//source.flush();
							//source.close();
							//throw new RuntimeException("primitive Arrays are not yet supported as arguments to constructors: "+argTypes[i]);
						}
					} else {
						source.write(params[i]);
					}
					if (i < params.length - 1) {
						source.write(", ");
					}
				}
				source.write(" };" + newLine);
			///} else {
				///source.write("     super (); " + newLine);
				///source.write("     Object [] initArgs = null;" + newLine);
			///}
			// register myself
			source.write(
				"        "
					+ Decode.getSimpleName(DefaultAdapter.class.getName())
					+ ".Instance().register( ("
					+ IProxy.class.getName()
					+ ")this, initArgs);"
					+ newLine);
			} // topmost type in proxy class hierarchy
		} else
			return; // TODO: encapsulate public fields by adding getter and setter for private ones
	}

	/**
	 * generates the source code that is needed to convert an instance of the given primitive array type to a corresponding Object array instance.
	 * Both variables must be named according to the <code>createIdentifiers([..,arrayType], true)[seqNrOfParam]</code>
	 * arrayType is placed at the index seqNrOfParam in the array to guarantee identical identifier generation
	 * @param arrayType - the given primitive array type
	 * @param primitiveType - the given primitive type (elements in the array have values of this type
	 * @param seqNrOfParam - the sequence number of the parameter in the method signature (first parameter has seqNr 0)
	 * @return a String containing the source code generated
	 */
	private static String getCodeFor_toArrayObject(Class arrayType, Class primitiveType, int seqNrOfParam) {
		String code = new String();
		if (arrayType.isArray() && primitiveType.isPrimitive()) {
			String objectType = Decode.primitiveTypeKeywordToObjectTypeName(primitiveType.toString());

			Class[] classArray = new Class[seqNrOfParam + 1];
			for (int i = 0; i < seqNrOfParam; i++) {
				classArray[i] = int.class;
			} // fill dummy types
			classArray[seqNrOfParam] = primitiveType;
			String primitiveIdent = createIdentifiers(classArray, false)[seqNrOfParam];
			classArray[seqNrOfParam] = arrayType;
			String objectIdent = createIdentifiers(classArray, true)[seqNrOfParam];
			code =
				objectType
					+ " [] "
					+ objectIdent
					+ " = "
					+ Decode.class.getName()
					+ ".toObjectArray("
					+ primitiveIdent
					+ "); "
					+ newLine;
		}
		return code;
	}

	/**
	 * @param method
	 * @return
	 */
	private static void implementIProxy(Method method, String params, FileWriter source, Class shyClass, IClassLoaderWithClassPath loader) throws IOException {
		String stateClass = StateOfComponent.class.getName();
		String stateId = formatParameterList(new Class[] { StateOfComponent.class }, false, true);
		String internalStateAction = "unknownInternalAction";
		String externalStateAction = "unknownExternalAction";

		//		implement IProxy.setInvocationHandler(IProxyInvocationHandler)
		if (method.getName().equals("setInvocationHandler")) {
			String handler = createIdentifiers(new Class[] { IAdapter.class }, false)[0];
			
			if (shyClass.getSuperclass() != null && proxies.containsKey(loader.getClassPath() + shyClass.getSuperclass().getName())) {
				// superclass is also a proxy
				source.write("   super.setInvocationHandler( " + handler + " );" + newLine);
			}	
			source.write("    this.implementator = " + handler + "; " + newLine);
			source.write("   staticImplementator = " + handler + "; " + newLine);
			return;

		} else if (
			method.getDeclaringClass().equals(IProxy.class)
				&& method.getName().equals("same")
				&& method.getParameterTypes().length == 1
				&& method.getParameterTypes()[0].equals(IProxy.class)) {
			
			// implement IProxy.equals(IProxy) without redirecting to handler !!!
			String argName = createIdentifiers(method.getParameterTypes(), false)[0];
			source.write("     return " + argName + " == this;");
			return;
			
		} else if (
			method.getDeclaringClass().equals(IProxy.class)
				&& method.getName().equals("hash")
				&& method.getParameterTypes().length == 0) {
			
			// implement IProxy.hash() without redirecting to handler !!!
			source.write("     return this.hashValue;" + newLine);
			return;
			
		} else if (
			method.getDeclaringClass().equals(IProxy.class)
				&& method.getName().equals("dump")
				&& ( (method.getParameterTypes() == null) || (method.getParameterTypes().length == 0) )
				&& method.getReturnType().equals(String.class)) {
		
			source.write("     return \"p4:\"+this.getClass().getName()+\"@\"+Integer.toHexString(this.hash());" + newLine);
		
		} else if (
				 	method.getName().equals(IProxy.unknownMethod)
					&& (method.getParameterTypes().length > 2)
					&& method.getParameterTypes()[0].equals(Object.class)
					 ) { 
				
				
						// TODO: care about primitives wrapping/unwrapping
						// TODO: care about static methods
						String arguments = formatParameterList(method.getParameterTypes(),false,false);
				source.write("    try {"+ newLine);
				//if (method.getParameterTypes()[1].equals(String.class)) {
				//	source.write("        return implementator."+implementingMethodName+"(callee, "+IProxy.methodNameArg+", args);"+newLine);
				//} else if (method.getParameterTypes()[1].equals(int.class)) {
					source.write("        return implementator."+implementingMethodName+"("+arguments+");"+newLine);
				//}
				source.write("    } catch ("+Throwable.class.getName()+" t) { "+newLine);
				source.write("        if (t instanceof "+WrappingException.class.getName()+") {((Exception)t).printStackTrace(); return null; } " + newLine);
				source.write("        else { throw t; } " + newLine);
				source.write("    } "+newLine);
		} 

	}

	/**
	 * Adds IProxy methods
	 * @param shyClass
	 * @param ifcsToImpl
	 * @return
	 */
	private static Iterator getPublicMethodsOf(Class shyClass, Class[] ifcsToImpl) {
		HashMap map = new HashMap();
		Method m;
		Class arg;

		//		shyClass itself (incl. superclasses) 
		Method[] ownMethods = shyClass.getMethods();
		for (int mIndex = 0; mIndex < ownMethods.length; mIndex++) {
			m = ownMethods[mIndex];

			Class[] params = m.getParameterTypes();
			String argTypeNames = formatParameterList(params, false, false);
			String exceptionsThrown = formatParameterList(m.getExceptionTypes(), false, false);
			// implement just abstract methods of superclasses (if ifcsToImpl != null)
			boolean needsImpl = (ifcsToImpl == null);
			try {
				Method superM = shyClass.getSuperclass().getMethod(m.getName(), params);
				// implement abstract methods: 
				needsImpl = (superM!=null) && Modifier.isAbstract(superM.getModifiers());
				needsImpl |= (superM!=null && superM.getDeclaringClass() == Object.class);
			} catch (Exception e) {
				// no method found in superclass	
			}
			if (needsImpl) { 
				
				map.put(m.getName() + "(" + argTypeNames + ") throws " + exceptionsThrown, m);
			}
		}

		// ifcs to be implemented
		for (int cIndex = 0; ifcsToImpl != null && cIndex < ifcsToImpl.length; cIndex++) {
			Class toImpl = ifcsToImpl[cIndex];

			Method[] publicMethods = toImpl.getMethods();
			// all public methods of the class including inherited ones	

			for (int mIndex = 0; mIndex < publicMethods.length; mIndex++) {
				m = publicMethods[mIndex];
				Class[] params = m.getParameterTypes();
				String argTypeNames = formatParameterList(params, false, false);
				String exceptionsThrown = formatParameterList(m.getExceptionTypes(), false, false);
				map.put(m.getName() + "(" + argTypeNames + ") throws " + exceptionsThrown, m);
			}
		}
		// IProxy interface
		Method[] interfaceMethods = IProxy.class.getMethods();
		// all methods declared in the Evolution Interface
		for (int mIndex = 0; mIndex < interfaceMethods.length; mIndex++) {
			m = interfaceMethods[mIndex];
			Class[] params = m.getParameterTypes();
			String argTypeNames = formatParameterList(params, false, false);
			String exceptionsThrown = formatParameterList(m.getExceptionTypes(), false, false);
			map.put(m.getName() + "(" + argTypeNames + ") throws " + exceptionsThrown, m);
		}

		//	These lists may overlap -> eliminate duplicates (signature)*/
		Iterator iter = map.values().iterator();
		return iter;
	}

	/** Method or Constructor
	 * @param source
	 */
	private static void appendSignature(AccessibleObject operation, FileWriter source) throws IOException {
		String formattedModifiers = " ";
		String returnTypeIdentifier = " ";
		String identifier = " ";
		Class[] parameterList;
		Class[] thrownExceptions;
		if (operation instanceof Method) {
			Method m = (Method) operation;
			int mod = m.getModifiers();
			if (Modifier.isAbstract(mod)
				&& m.getDeclaringClass().isInterface()) { // implement all abstract methods (IProxy interface)
				mod %= Modifier.ABSTRACT;
			}

			formattedModifiers = Modifier.toString(mod);
			// return type of method to be wrapped
			Class returnType = m.getReturnType();
			boolean wasArray = false;
			if (returnType.isArray()) {
				wasArray = true;
				returnType = returnType.getComponentType();
				if (returnType.isArray()) {
					LOG.error("Multidimensional arrays are not yet supported");
					throw new RuntimeException("Multidimensional arrays are not yet supported");
				}
			}
			returnTypeIdentifier = returnType.getName();
			if (returnType.isPrimitive()) {
				returnTypeIdentifier = returnType.toString();
			}
			if (wasArray)
				returnTypeIdentifier += " [] ";
			identifier = m.getName();
			parameterList = m.getParameterTypes();
			thrownExceptions = m.getExceptionTypes();

		} else if (operation instanceof Constructor) {
			Constructor c = (Constructor) operation;
			int mod = c.getModifiers();
			formattedModifiers = Modifier.toString(mod);
			identifier = Decode.getSimpleName(c.getDeclaringClass().getName());
			///getNamingOfWrapper(c.getDeclaringClass());
			parameterList = c.getParameterTypes();
			if (parameterList.equals(new Class[] { Object.class })) {
				// copy constructor given original => proxy
				// make public
				if (Modifier.isPrivate(mod)) {
					mod -= Modifier.PRIVATE;
					mod += Modifier.PUBLIC;
				} else if (Modifier.isProtected(mod)) {
					mod -= Modifier.PROTECTED;
					mod += Modifier.PUBLIC;
				} else if (!Modifier.isPublic(mod)) {
					mod += Modifier.PUBLIC;
				} // DECLARED (default)
			}
			thrownExceptions = c.getExceptionTypes();
		} else
			return; // neither constructor nor method TODO: hide public fields
		String formattedParameters = formatParameterList(parameterList, true, false);
		String formattedExceptions = " ";
		for (int eIndex = 0; eIndex < thrownExceptions.length; eIndex++) {
			if (eIndex > 0) {
				formattedExceptions += ", ";
			} else {
				formattedExceptions += " throws ";
			}
			formattedExceptions += thrownExceptions[eIndex].getName();
		}
		//source.write(newLine + "public static final int "+ METHOD_CONST_PREFIX + identifier.toUpperCase() + " = "+identifier.hashCode()+"; " + newLine);
		source.write(newLine + "    " + formattedModifiers + " ");
		source.write(returnTypeIdentifier + " ");
		source.write(identifier + " ");
		source.write(" ( ");
		source.write(formattedParameters);
		source.write(" ) ");
		source.write(formattedExceptions + newLine);
	}

	private static String formatParameterList(
		Class[] parameterTypes,
		boolean includeTypes,
		boolean doWrapPrimitiveTypes) {
		String[] classes = formatClassNames(parameterTypes);
		String[] idents = createIdentifiers(parameterTypes, doWrapPrimitiveTypes);
		String paramList = " ";
		String listSeparator = " ";
		for (int nIndex = 0; nIndex < classes.length; nIndex++) {
			paramList += listSeparator;
			listSeparator = " , ";
			if (includeTypes) {
				paramList += classes[nIndex] + " ";
			}
			paramList += idents[nIndex];
		}
		return paramList;
	}

	/**
	 * @param parameterList
	 * @return an array containing the formatted names of the classes in the input array
	 */
	private static String[] formatClassNames(Class[] parameterList) {
		String[] typeNames = new String[parameterList.length];
		for (int pIndex = 0; pIndex < parameterList.length; pIndex++) {
			Class param = parameterList[pIndex];
			typeNames[pIndex] = param.getName(); // e.g. "[I", "[[LString", "java.util.HashMap"
			if (param.isArray()) {
				Class simple = param.getComponentType(); // Assumes simple.isArray() == false
				if (simple.isArray()) {
					LOG.error("Multidimensional arrays are not yet supported");
					throw new RuntimeException("Multidimensional arrays are not yet supported");
					// TODO support multidimensional array types, e.g. [[Z (== byte [][]), [[[LString (== String[][][])
				}
				if (simple.isPrimitive()) { // array of primitive type [$encodedPrimitive
					typeNames[pIndex] = simple.toString() + " []";
				} else { // Object array: [L$typename
					String name = simple.getName();
					int endIndex = name.length();
					if (name.charAt(name.length() - 1) == ';')
						endIndex--; // cut off trailing ";"
					name.substring(2, endIndex); // cut off leading "[L"
					typeNames[pIndex] = name + " []"; // append array designator "[]"
				}
			} else if (param.isPrimitive()) {
				typeNames[pIndex] = param.toString(); // e.g. "int"
			}
		}
		return typeNames;
	}

	private static String[] createIdentifiers(Class[] classes, boolean doWrapPrimitiveTypes) {
		String[] identifiers = new String[classes.length];
		String[] classNames = formatClassNames(classes);
		for (int pIndex = 0; pIndex < classes.length; pIndex++) {
			identifiers[pIndex] = classNames[pIndex].replace('.', '_');
			int firstSpace = identifiers[pIndex].indexOf(' ');
			if (firstSpace != -1)
				identifiers[pIndex] = identifiers[pIndex].substring(0, firstSpace); // cut off trailing " []"
			identifiers[pIndex] = "a" + pIndex + "_" + identifiers[pIndex];
			if (doWrapPrimitiveTypes)
				identifiers[pIndex] = primitiveToObject(identifiers[pIndex], classes[pIndex]);
		}
		return identifiers;
	}

	private static String primitiveToObject(String primitiveIdentifier, Class primitiveType) {
		if (primitiveType.isPrimitive()) {
			String objectType = Decode.primitiveTypeKeywordToObjectTypeName(primitiveType.toString());

			if (objectType != null) {
				return " new " + objectType + " ( " + primitiveIdentifier + " ) ";
			} else {
				LOG.error(" cannot wrap because is not a primitive type: " + primitiveType);
				return primitiveIdentifier;
			}
		} else if (primitiveType.isArray() && primitiveType.getComponentType().isPrimitive()) { // primitive array
			return primitiveIdentifier + objectArraySuffix;
		} else
			return primitiveIdentifier;
	}

	/**
	 * @param shyClass
	 * @param fw
	 * @throws IOException 
	 */
	private static void appendClassHeader(Class shyClass, Class[] ifcsToImpl, FileWriter source) throws IOException {

		// package ...
		String packageOfWrapper = Decode.getPackage(shyClass);
		source.write(" package " + packageOfWrapper + ";" + newLine + newLine);
		Class superClass = shyClass.getSuperclass();

		// import ...
		if (superClass != null)
			source.write(" import " + superClass.getName() + ";" + newLine);
		source.write(" import " + IProxy.class.getName() + ";" + newLine); // any proxy implements this Interface
		source.write(" import " + WrappingException.class.getName() + ";" + newLine);
		source.write(" import " + DefaultAdapter.class.getName() + ";" + newLine);
		source.write(" import " + Logger.class.getName() + ";" + newLine);

		// use fully qualified names everywhere -> no additional imports except for superclass and implemented interfaces
		Class[] interfaces;
		if (ifcsToImpl == null) {
			interfaces = shyClass.getInterfaces();
		} else {
			interfaces = ifcsToImpl;
		}
		for (int iIndex = 0; iIndex < interfaces.length; iIndex++) {
			source.write(" import " + interfaces[iIndex].getName() + ";" + newLine);
		}

		// ... class/interface ... 
		String name = shyClass.getName();
		int mod = shyClass.getModifiers();
		if (shyClass.isInterface()) {
			mod %= Modifier.ABSTRACT;

		}
		String modifiers = Modifier.toString(mod);
		String classOrIfc = shyClass.isInterface() ? " " : " class ";
		source.write(newLine + modifiers + classOrIfc + Decode.getSimpleName(shyClass.getName()));

		// extends ... implements ... , ... 
		if (superClass != null) {
			source.write(" extends " + Decode.getSimpleName(superClass.getName()));
		}
		if (!shyClass.isInterface()) {
			source.write(" implements " + Decode.getSimpleName(IProxy.class.getName()));
		} else if (interfaces != null && interfaces.length > 0) {
			source.write(" extends ");
		}
		for (int iIndex = 0; iIndex < interfaces.length; iIndex++) {
			if (!shyClass.isInterface() || iIndex > 0) {
				source.write(" , ");
			}
			source.write(Decode.getSimpleName(interfaces[iIndex].getName()));
		}

	}

	/**
	 * @param cl - The ClassLoader of the proxy
	 * @param className - The fully qualified name of the proxy class
	 * @param sourceFile - the path to file containing the java source of the proxy class
	 * @return - the proxy Class
	 * @throws WrappingException
	 * @throws ClassNotFoundException
	 */
	private static Class doCompile(IClassLoaderWithClassPath cl, String className, String sourceFile, Class shyClass)
		throws WrappingException, ClassNotFoundException {
		String dest = null;
		String oldBase = null;

		if (cl instanceof AdapterClassLoader) { // hack
			oldBase = ((AdapterClassLoader) cl).getClassPath();
			if (oldBase.indexOf('.') > 0) {
				String newBase = oldBase.substring(oldBase.indexOf('_'), oldBase.lastIndexOf('.'));
				newBase = "bin" + File.separator + "adapters" + File.separator + "adapter2" + newBase;
				LOG.info("custom loader set path = " + newBase + ", was " + oldBase);
				((AdapterClassLoader) cl).setClassPath(newBase);
			}
		}

		if (cl instanceof IClassLoaderWithClassPath) {
			dest = ((IClassLoaderWithClassPath) cl).getClassPath();
		} else {
			WrappingException w = new WrappingException();
			LOG.error(cl + " : Need a ILoaderBase to compile the transparent proxy: " + className, w);
			throw w;
		}

		// prepare arguments to be passed to compiler
		String[] compileArgs = new String[5];

		// classpath argument value 
		compileArgs[0] = "-classpath";
		String pathToCops = new String();

		// include adapter cop in classpath (in case of unstable proxies loaded by an AdapterClassLoader)
		if (cl instanceof AdapterClassLoader) { // hack 
			pathToCops += ((AdapterClassLoader) cl).getClassPath() + File.pathSeparator;
		}
		// include any (TODO dependency jars of the component of the hidden class ONLY) in the classpath
		Enumeration cops = ComponentRepository.Instance().getComponentResources().elements();
		while (cops.hasMoreElements()) {
			ComponentResource cop = (ComponentResource) cops.nextElement();
			if (cop instanceof ProxyComponentResource) {
				// list proxy cops first
				pathToCops += cop.getExtResLocation() + cop.getCodeBase() + File.pathSeparator;
			}
		}
		cops = ComponentRepository.Instance().getComponentResources().elements();
		while (cops.hasMoreElements()) {
			ComponentResource cop = (ComponentResource) cops.nextElement();
			if (!(cop instanceof ProxyComponentResource)) {
				// list hidden cops last
				pathToCops += cop.getExtResLocation() + cop.getCodeBase() + File.pathSeparator;
			}
		}
		//	include any files in libs/  TODO: include system path instead of hardcoded "libs" folder
		File proxies = new File(ProxyComponentResourceFactory.getProxyFolder());
		File libs = new File(proxies.getParentFile().getParentFile(), "libs");
		String[] libraries = libs.list();
		for (int i = 0; libs != null && i < libraries.length; i++) {
			pathToCops += "libs" + File.separator + libraries[i] + File.pathSeparator;
		}
		pathToCops += "bin/lib/jadabs.jar"; // bootstrap jar
		compileArgs[1] = pathToCops;

		// destination path -d
		compileArgs[2] = "-d";
		compileArgs[3] = dest;
		compileArgs[4] = sourceFile;
		//	TODO be careful, this API is platform specific: J2SE version 1.5 will provide a standardized API to the developer tools
		// java 1.3.x
		com.sun.tools.javac.Main compiler = new com.sun.tools.javac.Main();
		int compileReturnCode = compiler.compile(compileArgs);
		// java 1.4.x
		//int compileReturnCode = com.sun.tools.javac.Main.compile( compileArgs );
		if (compileReturnCode == 0) {

			LOG.info(
				className
					+ " compiled successfully from source "
					+ sourceFile
					+ " to "
					+ compileArgs[3]
					+ ". Trying to load it from "
					+ ((IClassLoaderWithClassPath) cl).getClassPath());
			//File source = new File(sourceFile);
			//source.delete(); // uncomment this (and previous) line to delete sourcefile after compilation
			
			Class proxy = null;
			try {
				proxy = cl.loadClass(className);
			} catch (LinkageError e) {
				LOG.error(
				"[FAILED] to link class "
					+className
					+ ". The compiler arguments were: "
					+ compileArgs[0]
					+ " "
					+ compileArgs[1]
					+ " "
					+ compileArgs[2]
					+ " "
					+ compileArgs[3]
					+ " "
					+ compileArgs[4]);
					throw e;
			}
			LOG.info("Loaded successfully " + proxy.getName() + " from path " + compileArgs[3]);

			if (oldBase != null) { // hack
				 ((AdapterClassLoader) cl).setClassPath(oldBase);
			}
			return proxy;

		} else {
			LOG.error(
				"No compiler was found OR javac complained about bad source of file "
					+ sourceFile
					+ ". The compiler arguments were: "
					+ compileArgs[0]
					+ " "
					+ compileArgs[1]
					+ " "
					+ compileArgs[2]
					+ " "
					+ compileArgs[3]
					+ " "
					+ compileArgs[4]
			);
			throw new WrappingException("javac complained about bad source of proxy in File " + sourceFile);
		}

	}

	/**
	 * @deprecated use getMethodHash instead
	 * Must NOT include declaring class in id 
	 * because a method in the proxy class and the original class must
	 * lead to the same id, but must include parameter types.
	 * 
	 * @param m
	 * @return
	 */
	public static int getMethodId(Method m) {
		String paramTypes = new String();
		String params[] = createIdentifiers(m.getParameterTypes(), false);
		for (int i = 0; i < params.length; i++) {
			paramTypes += " " + params[i];
		}
		return (m.getName() + paramTypes).hashCode();
	}

	public static int getMethodHash(Class declaringClass, String methodName, Class[] parameterTypes) {
		Method m = null;
		try {
			m = declaringClass.getMethod(methodName, parameterTypes);
			return getMethodId(m);
		} catch (SecurityException e) {
			LOG.error("getMethodHash: method is not accessible: " + methodName, e);
			throw new WrappingException(e);
		} catch (NoSuchMethodException e) {
			LOG.error("getMethodHash: method does not exist: " + methodName, e);
			throw new WrappingException(e);
		}
	}
	
	
	public static boolean belongToSameCop(Object o1, Object o2) {
		CallByReferenceArgument copOfo1 = new CallByReferenceArgument();
		boolean isEqual = belongToSameCop(o1.getClass(), copOfo1, o2.getClass().getName());
		//LOG.info(o1 + " belongs to same cop "+copOfo1.modifyableArgument+" as " + o2 + " ??? " + isEqual);
		return isEqual;
	}	
	

	private static boolean belongToSameCop(Class c1, CallByReferenceArgument copOfc1, String c2Name) {
		if (c1 == null) {
			return false;
		}
		copOfc1.modifyableArgument = UpgradeableComponentResourceFactory.getComponentResourceByContent(c1.getName());
		if (copOfc1 == null) {
			return false;
		}
		IComponentResource copOfc2 = UpgradeableComponentResourceFactory.getComponentResourceByContent(c2Name);
		boolean isEqual = (copOfc1.modifyableArgument ==copOfc2);
		return isEqual;
	}
	

	public static boolean isExternal(Object externalObj, String hiddenClassName) {
		if (externalObj == null) {
			return false;
		}
		
		CallByReferenceArgument copContainer = new CallByReferenceArgument();
		if (belongToSameCop(externalObj.getClass(), copContainer, hiddenClassName)) {
			return false; 
		}
			// cop of externalObj may be null (runtime class) => is NOT external
		IComponentResource copOfexternalObj = (IComponentResource) copContainer.modifyableArgument;
		boolean isExternal = (null != copOfexternalObj);
				///!= UpgradeableComponentResourceFactory.getComponentResourceByContent(externalObj.getClass().getName()));
		LOG.info(externalObj + " is external to " + hiddenClassName + " ??? " + isExternal);
		return isExternal;
	}

	/**
	 * Checks if originalObj needs a proxy that hides it from external references.
	 * This is the case if the originalObj is defined inside the same component as the 
	 * callee, meaning both the runtime type of the originalObj and the calls belong to the same component 
	 * (the callee is hidden too).
	 * 
	 * Assumes that a proxy is needed just in case the class belongs to the same component as the class defining the method
	 * 
	 * @param originalReturnedObject
	 * @return true if the originalReturnedObject needs to be hidden behind a proxy
	*/
	public static boolean needsProxy(Object originalObj, String hiddenClassName) {

		if (originalObj == null) {
			return false; // void methods
		}
		Class returningTypeOfMethod = originalObj.getClass();
		CallByReferenceArgument copContainer = new CallByReferenceArgument();
		boolean isTrue = belongToSameCop(returningTypeOfMethod, copContainer, hiddenClassName);
		IComponentResource copOforiginalObj = (IComponentResource) copContainer.modifyableArgument;
		if (isTrue) {
			///IComponentResource res1 =
			///	UpgradeableComponentResourceFactory.getComponentResourceByContent(returningTypeOfMethod.getName());
			isTrue = (copOforiginalObj != null)
				 && (copOforiginalObj instanceof UpgradeableComponentResource)
					&& ((UpgradeableComponentResource) copOforiginalObj).isRuntimeEvolutionSupported(returningTypeOfMethod);
		}
		//LOG.info(copOforiginalObj+" instanceof UpgradeableComponentResource ??? "+(copOforiginalObj instanceof UpgradeableComponentResource));
		//LOG.info(copOforiginalObj+" isRuntimeEvolutionSupported ??? "+((UpgradeableComponentResource) copOforiginalObj).isRuntimeEvolutionSupported(returningTypeOfMethod));
		//LOG.info(originalObj+" needsProxy returning from "+hiddenClassName+" ??? "+isTrue);
		return isTrue;

	}

	/**
	 * @param createProxiesForInterfaces
	 */
	public static void setIfcHiding(boolean doCreateProxiesForInterfaces) {
		TransparentProxyFactory.isIfcHidingEnabled = doCreateProxiesForInterfaces;
		LOG.info("INTERFACE HIDING = " + doCreateProxiesForInterfaces);
	}

	/**
	 * clear proxy classes already generated
	 * Necessary because they are version independently cached. Resetting this cache avoids that
	 * client objects stored in a evolving object gets hidden by an old version unstable proxies
	 */
	public static void clearCache() {
		proxies.clear();
	}

}
