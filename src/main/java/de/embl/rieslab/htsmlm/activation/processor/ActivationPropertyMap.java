package de.embl.rieslab.htsmlm.activation.processor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.micromanager.PropertyMap;

public class ActivationPropertyMap implements PropertyMap {
	
	HashMap<String, Object> map;
	
	public ActivationPropertyMap() {
		map = new HashMap<String, Object>();
	}
	
	@Override
	public Builder copyBuilder() {
		return (Builder) new ActivationPropertyMapBuilder();
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public boolean containsKey(String key) {
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> keys) {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Class<?> getValueTypeForKey(String key) {
		return null;
	}

	@Override
	public OpaqueValue getAsOpaqueValue(String key) {
		return null;
	}

	@Override
	public String getValueAsString(String key, String aDefault) {
		return null;
	}

	@Override
	public boolean containsBoolean(String key) {
		return false;
	}

	@Override
	public Boolean getBoolean(String key) {
		return null;
	}

	@Override
	public Boolean getBoolean(String key, Boolean aDefault) {
		return null;
	}

	@Override
	public boolean getBoolean(String key, boolean aDefault) {
		return false;
	}

	@Override
	public boolean containsBooleanList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean[] getBooleanList(String key, boolean... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Boolean> getBooleanList(String key, Iterable<Boolean> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsByte(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte getByte(String key, byte aDefault) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean containsByteList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getByteList(String key, byte... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Byte> getByteList(String key, Iterable<Byte> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsShort(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public short getShort(String key, short aDefault) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean containsShortList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public short[] getShortList(String key, short... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Short> getShortList(String key, Iterable<Short> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsInteger(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getInteger(String key, int aDefault) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean containsIntegerList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int[] getIntegerList(String key, int... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> getIntegerList(String key, Iterable<Integer> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsLong(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Long getLong(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getLong(String key, Long aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLong(String key, long aDefault) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean containsLongList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long[] getLongList(String key, long... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> getLongList(String key, Iterable<Long> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsFloat(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getFloat(String key, float aDefault) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean containsFloatList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float[] getFloatList(String key, float... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Float> getFloatList(String key, Iterable<Float> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsDouble(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Double getDouble(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getDouble(String key, Double aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getDouble(String key, double aDefault) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean containsDoubleList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double[] getDoubleList(String key, double... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Double> getDoubleList(String key, Iterable<Double> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsNumber(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Number getAsNumber(String key, Number aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsNumberList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Number> getAsNumberList(String key, Number... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Number> getAsNumberList(String key, Iterable<Number> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsString(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getString(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getString(String key, String aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsStringList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> getStringList(String key, String... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getStringList(String key, Iterable<String> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsUUID(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public UUID getUUID(String key, UUID aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsUUIDList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<UUID> getUUIDList(String key, UUID... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UUID> getUUIDList(String key, Iterable<UUID> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsColor(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Color getColor(String key, Color aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsColorList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Color> getColorList(String key, Color... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Color> getColorList(String key, Iterable<Color> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsAffineTransform(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AffineTransform getAffineTransform(String key, AffineTransform aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsAffineTransformList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<AffineTransform> getAffineTransformList(String key, AffineTransform... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AffineTransform> getAffineTransformList(String key, Iterable<AffineTransform> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsPropertyMap(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PropertyMap getPropertyMap(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertyMap getPropertyMap(String key, PropertyMap aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsPropertyMapList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<PropertyMap> getPropertyMapList(String key, PropertyMap... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PropertyMap> getPropertyMapList(String key, Iterable<PropertyMap> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsRectangle(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Rectangle getRectangle(String key, Rectangle aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsRectangleList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Rectangle> getRectangleList(String key, Rectangle... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Rectangle> getRectangleList(String key, Iterable<Rectangle> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsDimension(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Dimension getDimension(String key, Dimension aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsDimensionList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Dimension> getDimensionList(String key, Dimension... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Dimension> getDimensionList(String key, Iterable<Dimension> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsPoint(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Point getPoint(String key, Point aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsPointList(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Point> getPointList(String key, Point... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Point> getPointList(String key, Iterable<Point> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Enum<E>> boolean containsStringForEnum(String key, Class<E> enumType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <E extends Enum<E>> E getStringAsEnum(String key, Class<E> enumType, E aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Enum<E>> boolean containsStringListForEnumList(String key, Class<E> enumType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <E extends Enum<E>> List<E> getStringListAsEnumList(String key, Class<E> enumType, E... defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Enum<E>> List<E> getStringListAsEnumList(String key, Class<E> enumType, Iterable<E> defaults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveJSON(File file, boolean overwrite, boolean createBackup) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PropertyMapBuilder copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getStringArray(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getStringArray(String key, String[] aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getInt(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getInt(String key, Integer aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer[] getIntArray(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer[] getIntArray(String key, Integer[] aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long[] getLongArray(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long[] getLongArray(String key, Long[] aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double[] getDoubleArray(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double[] getDoubleArray(String key, Double[] aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean[] getBooleanArray(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean[] getBooleanArray(String key, Boolean[] aDefault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertyMap merge(PropertyMap alt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getPropertyType(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(String path) throws IOException {
		// TODO Auto-generated method stub

	}

}
