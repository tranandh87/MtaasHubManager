package com.mtaas.util;

import java.util.ArrayList;
import java.util.List;

public class CustomStringList extends ArrayList<String> {
	
	public CustomStringList(List<String> arrayList){
		super(arrayList);
	}
	
    @Override
    public boolean contains(Object o) {
        String paramStr = (String)o;
        for (String s : this) {
            if (paramStr.equalsIgnoreCase(s)) 
            	return true;
        }
        return false;
    }
}

