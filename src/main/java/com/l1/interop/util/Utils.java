package com.l1.interop.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class Utils {
	
	public static List<Object[]> readCSVFile(String csvFile) throws Exception {
		
		List<Object []> testCases = new ArrayList<Object[]>();
        String[] data= null;
        String line = null;

        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        while ((line = br.readLine()) != null) {
            // use comma as separator
            data= line.split(",");
            testCases.add(data);
        }
		br.close();
        return testCases;
	}
	
	public static void write(String filename, String data) throws Exception {
		FileUtils.write(new File(filename), data,null,true);
	}

}
