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
thisDir=$(dirname "$0")
thisDir=$(readlink -f "$thisDir")
repo=$(readlink -f "$thisDir"/../repo)

java_path=$(dirname $(readlink -f $(which java)))
export JAVA_HOME=$(readlink -f "$java_path"/..)
cp "$repo"/stratosphere-bin-0.5/conf/stratosphere-conf_orig.yaml "$repo"/stratosphere-bin-0.5/conf/stratosphere-conf.yaml 
"$repo"/stratosphere-bin-0.5/bin/start-local.sh "$@"
"$repo"/stratosphere-bin-0.5/bin/start-webclient.sh "$@"

exit

