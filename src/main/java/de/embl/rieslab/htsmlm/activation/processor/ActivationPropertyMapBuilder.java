package de.embl.rieslab.htsmlm.activation.processor;

import org.micromanager.PropertyMap;
import org.micromanager.PropertyMap.PropertyMapBuilder;

public class ActivationPropertyMapBuilder implements PropertyMapBuilder {

	@Override
	public PropertyMap build() {
		return new ActivationPropertyMap();
	}

	@Override
	public PropertyMapBuilder putString(String key, String value) {
		return this;
	}

	@Override
	public PropertyMapBuilder putStringArray(String key, String[] values) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public PropertyMapBuilder putInt(String key, Integer value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public PropertyMapBuilder putIntArray(String key, Integer[] values) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public PropertyMapBuilder putLong(String key, Long value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public PropertyMapBuilder putLongArray(String key, Long[] values) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public PropertyMapBuilder putDouble(String key, Double value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public PropertyMapBuilder putDoubleArray(String key, Double[] values) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public PropertyMapBuilder putBoolean(String key, Boolean value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public PropertyMapBuilder putBooleanArray(String key, Boolean[] values) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public PropertyMapBuilder putPropertyMap(String key, PropertyMap values) {
		// TODO Auto-generated method stub
		return this;
	}

}
