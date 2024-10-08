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
# Run Build scripts

echo "Configure Build"
git status|head -1
./prebuild/configBuild.sh
if [ -d "products/com.ibm.safr.we.product/target" ]; then
    mvn clean
fi
echo "Tycho Build starting..."
mvn install
echo "Post Build Script"
# The postbuild pom.xml contains the command to execute the postbild.sh script
