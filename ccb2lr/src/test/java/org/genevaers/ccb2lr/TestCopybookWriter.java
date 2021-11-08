package org.genevaers.ccb2lr;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class TestCopybookWriter {

	@Test
	public void testCCB2LRtoYaml() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/simple.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		File out = new File("build/out");
		out.mkdirs();
		ccb2lr.writeYAMLTo("build/out/simplecb.yaml");
		File scbf = new File("build/out/simplecb.yaml");
		assertTrue(scbf.exists());
	}

	@Test
	public void testCCB2LRGroupInGrouptoYaml() throws IOException {
		Copybook2LR ccb2lr = new Copybook2LR();
		Path testPath = Paths.get("src/test/resources/groupInGroup.cpy");
		ccb2lr.processCopybook(testPath);
		ccb2lr.generateData();
		File out = new File("build/out");
		out.mkdirs();
		ccb2lr.writeYAMLTo("build/out/groupInGroup.yaml");
		File scbf = new File("build/out/groupInGroup.yaml");
		assertTrue(scbf.exists());
	}

}
