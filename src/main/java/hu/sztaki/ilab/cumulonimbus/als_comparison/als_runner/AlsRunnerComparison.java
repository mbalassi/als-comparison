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


package hu.sztaki.ilab.cumulonimbus.als_comparison.als_runner;

import hu.sztaki.ilab.cumulonimbus.als_comparison.flink.AlsFlink;
import hu.sztaki.ilab.cumulonimbus.als_comparison.strato.AlsStrato;
import hu.sztaki.ilab.cumulonimbus.util.LogCreator;

public class AlsRunnerComparison {

	public static void main(String args[]) throws Exception {
		if (args.length < 6) {
			System.out
					.println("Parameters: [WhichProgram] [noSubStasks] [matrix] [output] [rank] [numberOfIterations] [LogPath:OPTIONAL]");
		} else {

			boolean enableLogging = false;
			LogCreator logger = null;
			String logPath = "";
			boolean legalParameters = true;
			long startTime = 0L;
			long endTime = 0L;
			long totalTime = 0L;

			// parse parameters:
			String whichProgram = args[0];
			int numTasks = Integer.parseInt(args[1]);
			String matrixSource = args[2];
			String outputPath = args[3];
			int k = Integer.parseInt(args[4]);
			int numIterations = Integer.parseInt(args[5]);

			if (args.length > 6) {
				enableLogging = true;
				logPath = args[6];
			}

			// start logging
			if (enableLogging) {
				logger = new LogCreator(logPath + "/log");
				startTime = System.currentTimeMillis();
			}

			// run program
			switch (whichProgram) {
			case "AlsStrato":
				AlsStrato.runAls(numTasks, matrixSource, outputPath, k,
						numIterations);
				break;

			case "AlsFlink":
				AlsFlink.runAls(numTasks, matrixSource, outputPath, k,
						numIterations);
				break;

			default:
				System.out.println("This program does not exist!");
				printOptions();
				legalParameters = false;
			}

			// finish logging
			if (enableLogging) {
				endTime = System.currentTimeMillis();
				totalTime = endTime - startTime;
				if (legalParameters) {
					logger.writeParameters(whichProgram, totalTime,
							matrixSource, outputPath, numTasks, numIterations,
							k, new Double(0.0), "-");
				}
				logger.close();
			}

		}
	}

	public static void printOptions() {
		System.out.println("Available programs:");
		System.out.println("AlsStrato");
		System.out.println("AlsFlink");
	}

}
