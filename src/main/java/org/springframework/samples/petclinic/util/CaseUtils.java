package org.springframework.samples.petclinic.util;

public class CaseUtils {
	public static String toCamelCase(String s){
		   String[] parts = s.split("_");
		   String camelCaseString = "";
		   for (String part : parts){
		      camelCaseString = camelCaseString + toProperCase(part);
		   }
		   return camelCaseString;
		}

		static String toProperCase(String s) {
		    return s.substring(0, 1).toUpperCase() +
		               s.substring(1).toLowerCase();
		}

}
