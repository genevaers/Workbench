package com.ibm.safr.we.model;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.ibm.safr.we.model.associations.TestSAFRAssociationFactory;
import com.ibm.safr.we.model.logic.TestLogicTextParser;
import com.ibm.safr.we.model.logic.TestLogicTextParserBracket;
import com.ibm.safr.we.model.logic.TestLogicTextParserFind;
import com.ibm.safr.we.model.utilities.AllMigrationTests;
import com.ibm.safr.we.model.utilities.TestBatchActivateLookupPaths;
import com.ibm.safr.we.model.utilities.TestBatchActivateViews;
import com.ibm.safr.we.model.utilities.TestBatchComponent;
import com.ibm.safr.we.model.utilities.TestDependencyChecker;
import com.ibm.safr.we.model.utilities.TestExportUtility;
import com.ibm.safr.we.model.utilities.TestFindReplaceComponent;
import com.ibm.safr.we.model.utilities.TestFindReplaceText;
import com.ibm.safr.we.model.utilities.importer.AllImportTests;
import com.ibm.safr.we.model.view.AllViewTests;
import com.ibm.safr.we.model.view.TestHeaderFooterItem;

@Suite.SuiteClasses({
    TestSAFRAssociationFactory.class,
    TestLogicTextParser.class,TestLogicTextParserBracket.class,TestLogicTextParserFind.class,
    TestBatchActivateLookupPaths.class,
    TestBatchComponent.class, TestCode.class, TestCodeSet.class, TestControlRecord.class,
    TestEnvironment.class, TestExportUtility.class, TestFindReplaceText.class, 
    TestGroup.class, TestHeaderFooterItem.class, TestLogicalFile.class, TestLogicalRecord.class,
    TestLookupPath.class, TestLookupPathSourceField.class, TestLookupPathStep.class, TestPhysicalFile.class,
    TestSAFRApplication.class, TestSAFRFactory.class, TestSAFRValidator.class, TestUser.class,
    TestUserExitRoutine.class, TestUserSession.class, TestViewFolder.class, 
    TestDependencyChecker.class,TestBatchActivateViews.class,
    TestFindReplaceComponent.class, 
    AllViewTests.class, AllImportTests.class, AllMigrationTests.class
})

@RunWith(Suite.class)

public class AllModelTests {

}
