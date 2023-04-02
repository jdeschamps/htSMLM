package de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;

public abstract class PropertyFilter {
	
	private PropertyFilter additionalFilter_;
	
	public PropertyFilter(){
	}
	
	public PropertyFilter(PropertyFilter additionalFilter){
		additionalFilter_ = additionalFilter;
	}

	public HashMap<String, UIProperty> filterProperties(HashMap<String, UIProperty> properties){
		HashMap<String, UIProperty> filteredProperties = new HashMap<String, UIProperty>();
		
		Iterator<String> it;
		if(additionalFilter_ != null){
			it = additionalFilter_.filterProperties(properties).keySet().iterator();
		} else {
			it = properties.keySet().iterator();
		}
		
		String s;
		while(it.hasNext()){
			s = it.next();
			if(!filterOut(properties.get(s))){
				filteredProperties.put(s, properties.get(s));
			}
		}
		
		return filteredProperties;
	}
	

	public String[] filterStringProperties(HashMap<String, UIProperty> properties){
		HashMap<String, UIProperty> filteredProperties = new HashMap<String, UIProperty>();
		
		Iterator<String> it;
		if(additionalFilter_ != null){
			it = additionalFilter_.filterProperties(properties).keySet().iterator();
		} else {
			it = properties.keySet().iterator();
		}
		
		String s;
		while(it.hasNext()){
			s = it.next();
			if(!filterOut(properties.get(s))){
				filteredProperties.put(s, properties.get(s));
			}
		}
		
		String[] stringProp = filteredProperties.keySet().toArray(new String[0]);
		Arrays.sort(stringProp);
		
		return stringProp;
	}
	
	public HashMap<String, UIProperty> filteredProperties(HashMap<String, UIProperty> properties){
		HashMap<String, UIProperty> filteredProperties = new HashMap<String, UIProperty>();
		
		Iterator<String> it;
		if(additionalFilter_ != null){
			it = additionalFilter_.filterProperties(properties).keySet().iterator();
		} else {
			it = properties.keySet().iterator();
		}
		
		String s;
		while(it.hasNext()){
			s = it.next();
			if(filterOut(properties.get(s))){
				filteredProperties.put(s, properties.get(s));
			}
		}
		
		return filteredProperties;
	}

	/**
	 * Decides if the UIProperty should be filtered out (returns True) or kept (returns False). 
	 * 
	 * @param property Property to be filtered out or kept
	 * @return False if the property is to be kept, True if it should be excluded
	 */
	protected abstract boolean filterOut(UIProperty property);
}
