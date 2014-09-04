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

package hu.sztaki.ilab.cumulonimbus.als_comparison.strato;

import java.util.Iterator;
import java.util.Random;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.IterativeDataSet;
import eu.stratosphere.api.java.functions.CoGroupFunction;
import eu.stratosphere.api.java.functions.GroupReduceFunction;
import eu.stratosphere.api.java.functions.JoinFunction;
import eu.stratosphere.api.java.operators.DataSink;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.util.Collector;

public class AlsStrato {

	private static final Random RANDOM = new Random();

	public static void runAls(int noSubTasks, String matrixInput,
			String output, int k, int iteration) throws Exception {

		final ExecutionEnvironment env = ExecutionEnvironment
				.getExecutionEnvironment();

		DataSet<Tuple3<Integer, Integer, Double>> matrixSource = env
				.readCsvFile(matrixInput).fieldDelimiter('|')
				.lineDelimiter("|\n").includeFields(true, true, true)
				.types(Integer.class, Integer.class, Double.class);

		DataSet<Tuple2<Integer, double[]>> q = matrixSource.groupBy(1)
				.reduceGroup(new RandomMatrix(k))
				.name("Create q as a random matrix");

		IterativeDataSet<Tuple2<Integer, double[]>> initialQ = q
				.iterate(iteration);

		DataSet<Tuple3<Integer, Integer, double[]>> multipliedQ = matrixSource
				.join(initialQ).where(1).equalTo(0).with(new MultiplyVector())
				.name("Sends the columns of q with multiple keys");

		DataSet<Tuple2<Integer, double[]>> p = matrixSource
				.coGroup(multipliedQ).where(0).equalTo(0)
				.with(new Iteration(k, 0))
				.name("For fixed q calculates optimal p");

		DataSet<Tuple3<Integer, Integer, double[]>> multipliedP = matrixSource
				.join(p).where(0).equalTo(0).with(new MultiplyVector())
				.name("Sends the rows of p with multiple keys)");

		DataSet<Tuple2<Integer, double[]>> nextQ = matrixSource
				.coGroup(multipliedP).where(1).equalTo(1)
				.with(new Iteration(k, 1))
				.name("For fixed p calculates optimal q");

		q = initialQ.closeWith(nextQ);

		multipliedQ = matrixSource.join(q).where(1).equalTo(0)
				.with(new MultiplyVector())
				.name("Sends the columns of q with multiple keys");

		p = matrixSource.coGroup(multipliedQ).where(0).equalTo(0)
				.with(new Iteration(k, 0))
				.name("For fixed q calculates optimal p");

		ColumnOutputFormatStrato pFormat = new ColumnOutputFormatStrato(output
				+ "/p");
		
		@SuppressWarnings("unused")
		DataSink<Tuple2<Integer, double[]>> pOut = p.output(pFormat);

		ColumnOutputFormatStrato qFormat = new ColumnOutputFormatStrato(output
				+ "/q");
		@SuppressWarnings("unused")
		DataSink<Tuple2<Integer, double[]>> qOut = q.output(qFormat);

		env.setDegreeOfParallelism(noSubTasks);

		env.execute("AlsStrato");
	}

	public static double[] genRndVector(int k) {
		double[] vector_elements = new double[k];

		for (int i = 0; i < k; ++i) {
			vector_elements[i] = 1 + RANDOM.nextDouble() / 2;
		}
		return vector_elements;
	}

	public static final class RandomMatrix
			extends
			GroupReduceFunction<Tuple3<Integer, Integer, Double>, Tuple2<Integer, double[]>> {

		private static final long serialVersionUID = 1L;
		private int k;
		private Tuple2<Integer, double[]> vector = new Tuple2<Integer, double[]>();

		public RandomMatrix(int k) {
			this.k = k;
		}

		@Override
		public void reduce(Iterator<Tuple3<Integer, Integer, Double>> elements,
				Collector<Tuple2<Integer, double[]>> out) throws Exception {
			Tuple3<Integer, Integer, Double> element = elements.next();
			vector.setFields(element.f1, genRndVector(k));
			out.collect(vector);
		}
	}

	public static final class MultiplyVector
			extends
			JoinFunction<Tuple3<Integer, Integer, Double>, Tuple2<Integer, double[]>, Tuple3<Integer, Integer, double[]>> {

		private static final long serialVersionUID = 1L;

		@Override
		public Tuple3<Integer, Integer, double[]> join(
				Tuple3<Integer, Integer, Double> matrixElement,
				Tuple2<Integer, double[]> columnOfQ) {
			return new Tuple3<Integer, Integer, double[]>(matrixElement.f0,
					matrixElement.f1, columnOfQ.f1);
		}

	}

	public static final class Iteration
			extends
			CoGroupFunction<Tuple3<Integer, Integer, Double>, Tuple3<Integer, Integer, double[]>, Tuple2<Integer, double[]>> {

		private static final long serialVersionUID = 1L;
		private int k;
		private int id_;
		private int index;
		private Tuple2<Integer, double[]> result_ = new Tuple2<Integer, double[]>();

		public Iteration(int k, int index) {
			this.k = k;
			this.index = index;
		}

		@Override
		public void coGroup(
				Iterator<Tuple3<Integer, Integer, Double>> matrixElements,
				Iterator<Tuple3<Integer, Integer, double[]>> vector_,
				Collector<Tuple2<Integer, double[]>> out) {

			// TODO: If it is commented then there is a NullPointerException but
			// in Flink it does not occur!
			// Don't do anything if q is empty
			if (!vector_.hasNext()) {
				return;
			}

			id_ = vector_.next().getField(index);

			result_.setFields(id_, genRndVector(k));
			out.collect(result_);
		}
	}

}
