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

package hu.sztaki.ilab.cumulonimbus.als_comparison.flink;

import java.util.Iterator;
import java.util.Random;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.IterativeDataSet;
import org.apache.flink.api.java.functions.RichCoGroupFunction;
import org.apache.flink.api.java.functions.RichGroupReduceFunction;
import org.apache.flink.api.java.functions.RichJoinFunction;
import org.apache.flink.api.java.operators.DataSink;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;

public class AlsFlink {

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

		ColumnOutputFormatFlink pFormat = new ColumnOutputFormatFlink(output
				+ "/p");

		// TODO: when commented works for sampledb2b too
		@SuppressWarnings("unused")
		DataSink<Tuple2<Integer, double[]>> pOut = p.output(pFormat);

		ColumnOutputFormatFlink qFormat = new ColumnOutputFormatFlink(output
				+ "/q");
		@SuppressWarnings("unused")
		DataSink<Tuple2<Integer, double[]>> qOut = q.output(qFormat);

		env.setDegreeOfParallelism(noSubTasks);

		env.execute("AlsFlink");
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
			RichGroupReduceFunction<Tuple3<Integer, Integer, Double>, Tuple2<Integer, double[]>> {

		private static final long serialVersionUID = 1L;
		private int k;
		private Tuple2<Integer, double[]> vector = new Tuple2<Integer, double[]>();

		public RandomMatrix(int k) {
			this.k = k;
		}

		@Override
		public void reduce(Iterable<Tuple3<Integer, Integer, Double>> elements,
				Collector<Tuple2<Integer, double[]>> out) throws Exception {
			Tuple3<Integer, Integer, Double> element = elements.iterator()
					.next();
			vector.setFields(element.f1, AlsFlink.genRndVector(k));
			out.collect(vector);
		}
	}

	public static final class MultiplyVector
			extends
			RichJoinFunction<Tuple3<Integer, Integer, Double>, Tuple2<Integer, double[]>, Tuple3<Integer, Integer, double[]>> {

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
			RichCoGroupFunction<Tuple3<Integer, Integer, Double>, Tuple3<Integer, Integer, double[]>, Tuple2<Integer, double[]>> {

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
				Iterable<Tuple3<Integer, Integer, Double>> matrixElements_,
				Iterable<Tuple3<Integer, Integer, double[]>> vector_,
				Collector<Tuple2<Integer, double[]>> out) {

			Iterator<Tuple3<Integer, Integer, double[]>> vector = vector_
					.iterator();

			// Don't do anything if q is empty
			if (!vector.hasNext()) {
				return;
			}

			id_ = vector.next().getField(index);

			result_.setFields(id_, AlsFlink.genRndVector(k));
			out.collect(result_);
		}

	}
}