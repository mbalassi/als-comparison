/***********************************************************************************************************************
 *
 * Copyright (C) 2014 by the Datamining and Search Group of the Hungarian Academy of Sciences (http://dms.sztaki.hu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/

package hu.sztaki.ilab.cumulonimbus.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogCreator {
  private String outputPath;
  private PrintWriter pw;
  private Date startTime;

  private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
  public LogCreator(String filePath) {
    outputPath = filePath;
    startTime = new Date();
  }

  public void writeParameters(String algorithm, long timeTaken, String input, String output, int numTasks, int numIterations, int k, double lambda, String qInput) {
    
    try {
      pw = new PrintWriter(new FileWriter(new File(outputPath),true));
      pw.println("#Parameters of the job:");
  
      pw.println("start time: " + dateFormat.format(this.startTime));
      pw.println("input file: " + input);
      pw.println("q input: " + qInput);
      pw.println("output file: " + output);
      pw.println("numOfTasks: " + numTasks);
      pw.println("k: " + k);
      pw.println("lambda: " + Double.toString(lambda));
      pw.println("iter: " + numIterations);
      pw.println("program: " + algorithm);
      pw.println("Time taken: " + Integer.toString((int) timeTaken));
    
    } catch(IOException io) {
      io.printStackTrace();
    }
  }

  public void close() {
      pw.close();
  }

}
