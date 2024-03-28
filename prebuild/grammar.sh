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

mvn dependency:get -Dartifact=org.genevaers:grammar:1.0.0 -o > /dev/null 
if [ $? != 0 ]; 
then
    echo "Clone the grammar"
    BASEDIR=${PWD}
    echo "Workbench location: ${BASEDIR}"
    cd ..
    if [[ ! -z "$GERS_GRAMMAR" ]]; then
        echo "Cloning from $GERS_GRAMMAR"
        git clone $GERS_GRAMMAR Grammar
    else
        git clone https://github.com/genevaers/Grammar.git Grammar
    fi
    cd ./Grammar
    echo "Grammar location: ${PWD}"
    mvn install
    cd $BASEDIR
else
    echo "Grammar repo in place"
fi
