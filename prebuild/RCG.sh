#!/usr/bin/env bash
# Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# Get the Grammar repo if it is not there

main() {
    mvn dependency:get -Dartifact=org.genevaers:compilers:1.0.2 > /dev/null 2>&1
    if [ $? != 0 ]; 
    then cloneRepo 
    fi
    mvn dependency:get -Dartifact=org.genevaers:genevaio:1.0.2 > /dev/null 2>&1
    if [ $? != 0 ]; 
    then cloneRepo 
    fi
    mvn dependency:get -Dartifact=org.genevaers:utilities:1.0.2 > /dev/null 2>&1
    if [ $? != 0 ]; 
    then cloneRepo 
    fi
    mvn dependency:get -Dartifact=org.genevaers:runcontrolgenerator:1.0.2 > /dev/null 2>&1
    if [ $? != 0 ]; 
    then cloneRepo 
    fi
    mvn dependency:get -Dartifact=org.genevaers:runcontrolanalyser:1.0.2 > /dev/null 2>&1
    if [ $? != 0 ]; 
    then cloneRepo 
    fi
    # else condition
    echo "Run Control Apps repo in place"
}

cloneRepo() {
    echo "Clone the Run Control Apps"
    BASEDIR=${PWD}
    echo "Workbench location: ${BASEDIR}"
    cd ..
    if [[ ! -z "$GERS_RCG" ]]; then
        echo "Cloning from $GERS_RCG"
        git clone $GERS_RCG Run-Control-Apps
    else
        https://github.com/genevaers/Run-Control-Apps.git Run-Control-Apps
    fi
    cd ./Run-Control-Apps
    echo "Run-Control-Apps location: ${PWD}"
    # TODO remove following line
    git checkout error-management
    mvn install -DskipTests
    cd $BASEDIR
    echo "Cloning complete"
    exit 0
}

main "$@"
