########################################################################################################################
#
# Copyright (C) 2014 by the Datamining and Search Group of the Hungarian Academy of Sciences (http://dms.sztaki.hu)
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
# an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
# specific language governing permissions and limitations under the License.
#
########################################################################################################################

#!/bin/bash -eu
date=$(date +%Y-%m-%d-%H-%M-%S)

#input parameters:
numOfTasks="$2"
input="$3"
k_feature="$4"
numIterations="$5"

#options: AlsStrato, AlsFlink
algo="$1"

#the output folder name is generated
inputFile="$(basename "$input")"
outputFolder="$algo"_w"$numOfTasks"_"$date"
echo "$outputFolder"

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"

flinkHome="$thisDir"/repo/flink-0.6-hadoop2
stratoHome="$thisDir"/repo/stratosphere-bin-0.5
jarPath="$thisDir"/target/als-comparison-0.1-SNAPSHOT-jar-with-dependencies.jar
classPath=hu.sztaki.ilab.cumulonimbus.als_comparison.als_runner.AlsRunnerComparison

pushd "$thisDir"

#start local service
if [ "$algo" == "AlsStrato" ]; then
  "$thisDir"/scripts/start-all-local-strato-0.5.sh
  sleep 2
  "$stratoHome"/bin/stratosphere run --verbose --class "$classPath" "$jarPath" -cp "$algo" "$numOfTasks" "$thisDir"/data/"$inputFile" "$thisDir"/"$outputFolder" "$k_feature" "$numIterations" "$outputFolder"
  "$thisDir"/scripts/stop-all-local-strato-0.5.sh
else 
  "$thisDir"/scripts/start-all-local-flink-0.6-hadoop2.sh
  "$flinkHome"/bin/flink run --verbose --class "$classPath" "$jarPath" "$algo" "$numOfTasks" "$thisDir"/data/"$inputFile" "$thisDir"/"$outputFolder" "$k_feature" "$numIterations" "$outputFolder"
  "$thisDir"/scripts/stop-all-local-flink-0.6-hadoop2.sh
fi

popd
