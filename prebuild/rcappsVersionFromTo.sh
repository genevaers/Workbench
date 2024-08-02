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
echo updating RCApps version from $1 to $2
sed -i -e "s/$1/$2/" configBuild.sh
sed -i -e "s/rcapps-$1/rcapps-$2/" build.properties.base
sed -i -e "s/rcapps-$1/rcapps-$2/" ../plugins/genevagui/META-INF/MANIFEST.MF