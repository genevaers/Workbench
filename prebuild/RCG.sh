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
# Get the Run Control Apps repo if it is not there

main() {
    echo "Clone the Run Control Apps"
    BASEDIR=${PWD}
    echo "Workbench location: ${BASEDIR}"
    cd ..
    if [[ ! -z "$GERS_RCG" ]]; then
        echo "Cloning from bash variable $GERS_RCG"
        clone $GERS_RCG
    else
        echo "Using standard GitHub repo"
        clone "https://github.com/genevaers/Run-Control-Apps.git"
    fi
    cd ./Run-Control-Apps
    echo "Run-Control-Apps location: ${PWD}"
    # TODO remove following line
    git checkout error-management
    git pull
    mvn install -DskipTests
    cd $BASEDIR
}

clone() {
    if [ ! -d Grammar ] 
    then
        git clone $1 Run-Control-Apps
    fi
}



main "$@"