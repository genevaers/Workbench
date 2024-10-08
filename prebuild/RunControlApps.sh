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
    if [[ ! -z "$GERS_RUNCONTROL" ]]; then
        echo "Cloning from bash variable $GERS_RUNCONTROL"
        clone $GERS_RUNCONTROL
    else
        echo "Using standard GitHub repo"
        clone "https://github.com/genevaers/Run-Control-Apps.git"
    fi
    cd ./Run-Control-Apps
    echo "Run-Control-Apps location: ${PWD}"
    git status|head -1
    if [[ ! -z "$GERS_JARS" ]]; then
        echo "************************************************************"
        echo "Building Run-Control-Apps including Db2 jars from $GERS_JARS"
        echo "************************************************************"
        mvn install -Pdb2
    else 
        echo "****************************************************"
        echo "Building Run-Control-Apps with Postgres only version"
        echo "****************************************************"
        mvn install 
    fi
    cd $BASEDIR
}

clone() {
    if [ ! -d Run-Control-Apps ] 
    then
        git clone $1 Run-Control-Apps
    fi
}



main "$@"