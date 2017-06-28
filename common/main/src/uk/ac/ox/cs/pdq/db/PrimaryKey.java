package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;

import uk.ac.ox.cs.pdq.InterningManager;

public class PrimaryKey implements Serializable{
	private static final long serialVersionUID = 5003431166885480820L;
	protected final Attribute[] attributes;
	
	protected PrimaryKey(Attribute[] key) {
		this.attributes = key.clone();
	}
	
	protected Object readResolve() {
		return s_interningManager.intern(this);
	}

	public Attribute[] getAttributes() {
		return this.attributes.clone();
	}
	
	public int getNumberOfAttributes() {
		return this.attributes.length;
	}
	
	protected static final InterningManager<PrimaryKey> s_interningManager = new InterningManager<PrimaryKey>() {
		protected boolean equal(PrimaryKey object1, PrimaryKey object2) {
			if (object1.attributes.length != object2.attributes.length)
				return false;
			for (int index = object1.attributes.length - 1; index >= 0; --index)
				if (!object1.attributes[index].equals(object2.attributes[index]))
					return false;
			return true;
		}

		protected int getHashCode(PrimaryKey object) {
			int hashCode = 0;
			for (int index = object.attributes.length - 1; index >= 0; --index)
				hashCode = hashCode * 7 + object.attributes[index].hashCode();
			return hashCode;
		}
	};

	public static PrimaryKey create(Attribute[] attributes) {
		return s_interningManager.intern(new PrimaryKey(attributes));
	}

}
