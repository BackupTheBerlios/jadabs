package ch.ethz.iks.evolution.state;

import java.util.HashMap;

/**
 * NOT YET IN USE - Planned to hold state of a component during an evolution step.
 * It may keep the state while the old version is down and the new one not yet started.
 * 
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class StateOfComponent {
	
	private HashMap members = new HashMap(); //just useful per object
	
	public Object get(String memberIdentifier) {
		return members.get(memberIdentifier);
	}
	
	public byte getByte(String memberIdentifier) {
			Byte valueOfMember = (Byte) members.get(memberIdentifier);
			return valueOfMember.byteValue();
	}
	
	public boolean getBoolean(String memberIdentifier) {
		Boolean valueOfMember = (Boolean) members.get(memberIdentifier);
			return valueOfMember.booleanValue();
	}
	
	public char getCharacter(String memberIdentifier) {
		Character valueOfMember = (Character) members.get(memberIdentifier);
			return valueOfMember.charValue();
	}
	
	public double getDouble(String memberIdentifier) {
			Double valueOfMember = (Double) members.get(memberIdentifier);
			return valueOfMember.doubleValue();
	}
	
	public float getFloat(String memberIdentifier) {
			Float valueOfMember = (Float) members.get(memberIdentifier);
			return valueOfMember.floatValue();
	}
	
	public int getInteger(String memberIdentifier) {
			Integer valueOfMember = (Integer) members.get(memberIdentifier);
			return valueOfMember.intValue();
	}
	
	public long getLong(String memberIdentifier) {
			Long valueOfMember = (Long) members.get(memberIdentifier);
			return valueOfMember.longValue();
	}
	
	public short getShort(String memberIdentifier) {
			Short valueOfMember = (Short) members.get(memberIdentifier);
			return valueOfMember.shortValue();
	}
	
	public Object set(String memberIdentifier, Object reference) {
			return members.put(memberIdentifier, reference);
		}
	
		public byte setByte(String memberIdentifier, byte value) {
				Byte valueOfMember = (Byte) members.put(memberIdentifier, new Byte(value));
				return valueOfMember.byteValue();
		}
	
		public boolean setBoolean(String memberIdentifier, boolean value) {
			Boolean valueOfMember = (Boolean) members.put(memberIdentifier, new Boolean(value));
				return valueOfMember.booleanValue();
		}
	
		public char setCharacter(String memberIdentifier, char value) {
			Character valueOfMember = (Character) members.put(memberIdentifier, new Character(value));
				return valueOfMember.charValue();
		}
	
		public double setDouble(String memberIdentifier, double value) {
				Double valueOfMember = (Double) members.put(memberIdentifier, new Double(value));
				return valueOfMember.doubleValue();
		}
	
		public float setFloat(String memberIdentifier, float value) {
				Float valueOfMember = (Float) members.put(memberIdentifier, new Float(value));
				return valueOfMember.floatValue();
		}
	
		public int setInteger(String memberIdentifier, int value) {
				Integer valueOfMember = (Integer) members.put(memberIdentifier, new Integer(value));
				return valueOfMember.intValue();
		}
	
		public long setLong(String memberIdentifier, long value) {
				Long valueOfMember = (Long) members.put(memberIdentifier, new Long(value));
				return valueOfMember.longValue();
		}
	
		public short setShort(String memberIdentifier, short value) {
				Short valueOfMember = (Short) members.put(memberIdentifier, new Short(value));
				return valueOfMember.shortValue();
		}


}
