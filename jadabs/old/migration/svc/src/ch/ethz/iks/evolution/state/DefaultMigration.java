package ch.ethz.iks.evolution.state;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.mgr.OnlineUpgradeFailedException;
import ch.ethz.iks.evolution.step.IUpgradeStrategy;
import ch.ethz.iks.proxy.IProxy;


/**
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class DefaultMigration {

	private static  Logger  LOG =  Logger.getLogger(DefaultMigration.class);
	
	public static void shallowCopy(Object oldObj, Object newObj) throws OnlineUpgradeFailedException {
		shallowCopy(oldObj, newObj, null, true, true);
	}
	/**
	 * Implements default state transfer for objects by a shallow copy of their (non final) members
	 *
	 * If the old object's type is not assignable from the new one ( oldObj = newObj leads to a type violation ) , the objects are assumed
	 * to declare the same fields and declared in the same type hierarchy (that is, their superclasses are equal or were loaded from different loaders and also define the same fields) 
	 * The latter case is satisfied in case an object is evolved to its next generation object but the underlying class remain the same.
	 * 
	 * In the case of assignability, that is, newObj's type is the same or a subclass of oldObj's type, just the state of their common superclasses
	 * is copied: All fields declared in oldObj's type and its recursive superclasses.
	 *  
	 * todo: this method is forseen to also handle state transfer between completely unrelated classes (e.g. in case of code evolution)
	 *
	 * @param oldObj
	 * @param newObj
	 * @throws OnlineUpgradeFailedException
	 */
	public static void shallowCopy(
		Object oldObj,
		Object newObj,
		IUpgradeStrategy customizer,
		boolean doCopyPrimitives,
		boolean doSearchCommonSuperclass) throws OnlineUpgradeFailedException {

		Class newClass = newObj.getClass();
		Class oldClass = oldObj.getClass();
		
		if (doSearchCommonSuperclass) {
			if (oldClass.isAssignableFrom(newClass)) {
				// newClass extends oldClass, just copy state of common superclasses
				while (oldClass != newClass) {
					newClass = newClass.getSuperclass();
				}
			}
		}
		
		
		// iterate over all declared members in every superobject (and object itself)
		//synchronized (oldObj) {
			while ( ( newClass != Object.class) && 
			        ( ! newClass.isInterface() ) && 
			        ( oldClass != Object.class) && 
			        ( ! oldClass.isInterface()) 
			      ) {
					String cn = oldClass.getName();
					
					//hack TODO: fix this hack to avoid copying of evolution.jar objects: e.g. set evolution.jar Upgradeable and check copresbycontent == evolutioncop
				if (cn.startsWith("ch.ethz.iks.evolution") || cn.startsWith("ch.ethz.iks.proxy")) {
					if (cn.startsWith("ch.ethz.iks.evolution.test.")) {
						// test cases
					} else {
						//LOG.error("[do NOT copy object belonging to evolution.jar] "+cn); 
						return;
					}
				}
				if (cn.startsWith("ch.ethz.iks.jadabs.")) {
					return;
				} else if (cn.startsWith("java.lang.System")) {
					 return;
				} else if (cn.startsWith("java.lang.Class")) {
					return;
				} else if (cn.startsWith("java.lang.Process")) {
					return;
				} else if (cn.startsWith("java.lang.Runtime")) {
					return;
				} else if (cn.startsWith("java.lang.Security")) {
					return;
				} else if (cn.startsWith("java.lang.ThreadGroup")) {
					return;
				} else if (cn.startsWith("java.lang.ThreadLocal")) {
					return;
				} else if (cn.startsWith("java.lang.InheritableThreadLocal")) {
					return;
				} else if (cn.startsWith("java.lang.Compiler")) {
					return;
				} else if (cn.startsWith("java.lang.Package")) {
					return;
				} else if (cn.startsWith("java.lang.Thread")) {
					return;
				} if (cn.startsWith("sun.")) {
					return;
				} 
				 
				Field[] members = oldClass.getDeclaredFields();
				for (int mIndex = 0; mIndex < members.length; mIndex++) {
					Field newField = null;
					Field oldField = null;
					Object oldValue = null;
					String msg = "";
					
					try {
						oldField = members[mIndex];
						
						
						int mods = oldField.getModifiers();
						if (Modifier.isFinal(mods)) {
							continue;
						} //else if (Modifier.isTransient(mods)) {
							//continue;
						//} 
						// check if field exists in new version, too
						//TODO: exception handling: match field type/name to transfer state even in case of code evolution 
						
						newField = newClass.getDeclaredField(oldField.getName());
						
						Class declaredType = oldField.getType();
						
						
						if ( doCopyPrimitives && declaredType.isPrimitive() ) {
							copyPrimitive( oldObj, oldField, newObj, newField);
							continue;
						} else if (declaredType.isPrimitive()) {
							continue;
						} else if (declaredType.isArray()) {
							if (customizer == null ) continue;
							Class simpleType = declaredType.getComponentType();
							oldField.setAccessible(true);
							oldValue = oldField.get(oldObj);
							if (oldValue instanceof Object[]) {
								Object newValue = null;
								// LOG.info("[ COPY ]   object array "+oldObj.getClass()+" "+oldObj+"as value of field"+oldField.getName());
								for (int k = 0; k < ((Object[])oldValue).length; k++) {
									Object oldArrayElement = ((Object[])oldValue)[k];
									Object newArrayElement = null;
									try {
										newField.setAccessible(true);
										newValue = newField.get(newObj);
										newArrayElement = ((Object[])newValue)[k];
									} catch (Exception e) {
										
										newValue = new Object[ ((Object[])oldValue).length];
									} finally {
										if (newObj ==null) return; 	
										boolean doMigr = customizer.doMigrate(oldField) || doCopyPrimitives;
										Object migratedElement = customizer.transferState(oldArrayElement, newArrayElement, doMigr);
										
										if (newObj!=null) {
																			String migrobj = null;
																			if(migratedElement instanceof IProxy) {
																				migrobj = ((IProxy)migratedElement).dump();
																			} else if(migratedElement !=null) {
																				migrobj = migratedElement.toString();
																			} else {
																				migrobj ="NULL";
																			}
																			if (false) {
																				LOG.info("[ COPY ]   allowed to assign migrated obj "+migrobj+" to "+newObj+"."+newField.getName()+"? "+doMigr);
																			}
									
																		}
																		
										((Object[])newValue)[k] = migratedElement;
										oldField.setAccessible(false);
										newField.setAccessible(false);
									}
								}
							} else if (doCopyPrimitives) {
									copyPrimitive(oldObj, oldField, newObj, newField);
							} else continue;
						}
						
						boolean doMigr =doCopyPrimitives;
						if (customizer!=null) {
							doMigr |=  customizer.doMigrate(oldField);
						}
						oldValue = copyMember(oldObj, oldField, newObj, newField, customizer, doMigr);					
							
					} catch (OnlineUpgradeFailedException f) {
							throw f;
					} catch (Exception e) {
						
							/*msg =
								" migrate "
									+ newClass.getName()
									+ ": "
									+ newField.getType()
									+ " @ "
									+ newField.getType().getClassLoader()
									+ " "
									+ newField.getName()
									+ " := ("
									+ oldField.getType()
									+ " @ "
									+ oldField.getType().getClassLoader()
									+ ") "
									+ oldValue;*/
						 msg = " to migrate "+oldField;
						 LOG.error(" [FAILED]   " + msg + ": caugh exception "+e);
						
					}
				}

				newClass = newClass.getSuperclass();
				oldClass = oldClass.getSuperclass();
				//// LOG.info(" [ SUPER ]   moving up type hierarchy to old superclass " + oldClass);
			}
		//}
	}
	
	private static Object copyMember(Object oldObj, Field oldField, Object newObj, Field newField, IUpgradeStrategy customizer, boolean doAssignMigratedObj) throws IllegalArgumentException, IllegalAccessException {
							oldField.setAccessible(true);
							newField.setAccessible(true);
							// get current values of members of both version objects
							Object oldValue = oldField.get(oldObj);
							Object newValue = newField.get(newObj);
						
							if (customizer != null) {
								
								Object migratedObject = null;						
								//try {
									migratedObject = customizer.transferState(oldValue, newValue, doAssignMigratedObj ); // cannot decide in general how to map state between existing objects
								/*} catch (StackOverflowError e) {
									RuntimeException trace = new RuntimeException("current stack");
									trace.fillInStackTrace();
									LOG.error("OFLO started at ", trace);
									trace.printStackTrace();
									
								}*/
								if (newObj!=null) {
									String migrobj = null;
									if(migratedObject instanceof IProxy) {
										migrobj = ((IProxy)migratedObject).dump();
									} else if(migratedObject !=null) {
										migrobj = migratedObject.toString();
									} else {
										migrobj ="NULL";
									}
									if (false) {
										LOG.info("[ COPY ]   allowed to assign migrated obj "+migrobj+" to "+newObj+"."+newField.getName()+"? "+doAssignMigratedObj);
									}
								}
								if (doAssignMigratedObj && newObj != null) {
									newField.set(newObj, migratedObject); // assign migrated object as new value of new object member
									/*String msg =
																" [ COPY:done ]   "
																	+ newObj
																	+ ": "
																	+ newField.getType()
																	+ " @ "
																	+ newField.getType().getClassLoader()
																	+ " "
																	+ newField.getName()
																	+ " := "
																	+ migratedObject
																	+": ("
																	+ oldField.getType()
																	+ " @ "
																	+ oldField.getType().getClassLoader()
																	+ ") "
																	+ oldValue;
									// LOG.info("[  OK  ]   " + msg);*/
								}
								
						
							}  

							oldField.setAccessible(false);
							newField.setAccessible(false);
							return oldValue;
	}
	
	
	/**
	 * @param oldObj
	 * @param oldField
	 * @param newObj
	 * @param newField
	 */
	private static void copyPrimitive(Object oldObj, Field oldField, Object newObj, Field newField) throws IllegalArgumentException, IllegalAccessException {
	   Class memberType = oldField.getType();
	   oldField.setAccessible(true);
		newField.setAccessible(true);
	   if (memberType.isPrimitive()) {
			
		if (memberType == int.class) {
			int i = oldField.getInt(oldObj);
			newField.setInt(newObj, i);
		} else if (memberType == float.class) {
			float f = oldField.getFloat(oldObj);
			newField.setFloat(newObj, f);
		} else if (memberType == double.class) {
			double d = oldField.getDouble(oldObj);
			newField.setDouble(newObj, d);
		} else if (memberType == char.class) {
			char c = oldField.getChar(oldObj);
			newField.setChar(newObj, c);
		} else if (memberType == boolean.class) {
			boolean z = oldField.getBoolean(oldObj);
			newField.setBoolean(newObj, z);
		} else if (memberType == short.class) {
			short s = oldField.getShort(oldObj);
			newField.setShort(newObj, s);
		} else if (memberType == long.class) {
			long l = oldField.getLong(oldObj);
			newField.setLong(newObj, l);
		}
		
	   } else {
	   
	   memberType = memberType.getComponentType(); 
	   if (memberType == null) {
	   		return;
	   } else if (!memberType.isPrimitive()) {
	   		return;
	   } else if (memberType == int.class) {
			int[] i = (int[])oldField.get(oldObj);
			newField.set(newObj, i);
		} else if (memberType == float.class) {
			float[] f = (float[])oldField.get(oldObj);
			newField.set(newObj, f);
		} else if (memberType == double.class) {
			double[] d =(double[]) oldField.get(oldObj);
			newField.set(newObj, d);
		} else if (memberType == char.class) {
			char[] c = (char[])oldField.get(oldObj);
			newField.set(newObj, c);
		} else if (memberType == boolean.class) {
			boolean[] z = (boolean[])oldField.get(oldObj);
			newField.set(newObj, z);
		} else if (memberType == short.class) {
			short[] s = (short[])oldField.get(oldObj);
			newField.set(newObj, s);
		} else if (memberType == long.class) {
			long[] l = (long[])oldField.get(oldObj);
			newField.set(newObj, l);
	   }
	   
	}
	
	oldField.setAccessible(false);
	newField.setAccessible(false);
	}
}
