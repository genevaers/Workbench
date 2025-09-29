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
#build the properties file
#and copy the DB2 jars to the WE lib if needed
GENEVAGUI="plugins/genevagui"
rm $GENEVAGUI/build.properties
rm -rf $GENEVAGUI/lib
echo "mkdir $GENEVAGUI/lib"
mkdir $GENEVAGUI/lib
cat prebuild/build.properties.base > $GENEVAGUI/build.properties
if [[ ! -z "$GERS_JARS" ]]; then
    echo "Including Db2 jars from $GERS_JARS"
    cat prebuild/build.properties.db2 >> $GENEVAGUI/build.properties
    cp $GERS_JARS/db2jcc4.jar $GENEVAGUI/lib/
    cp $GERS_JARS/db2jcc_license_cu.jar $GENEVAGUI/lib/
    cp $GERS_JARS/db2jcc_license_cisuz.jar $GENEVAGUI/lib/
else 
    echo "*******************************"
    echo "Building a Postgres only version"
    echo "*******************************"
fi 
echo "Copy the appchooser"
cp ../Run-Control-Apps/RCApps/target/rcapps-1.1.0_RC20-jar-with-dependencies.jar $GENEVAGUI/lib/
cat prebuild/build.properties.end >> $GENEVAGUI/build.properties
