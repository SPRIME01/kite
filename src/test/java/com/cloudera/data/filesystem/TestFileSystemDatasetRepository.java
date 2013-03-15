/**
 * Copyright 2013 Cloudera Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.data.filesystem;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.cloudera.data.Dataset;
import com.cloudera.data.DatasetDescriptor;
import com.cloudera.data.PartitionStrategy;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class TestFileSystemDatasetRepository {

  private FileSystem fileSystem;
  private Path testDirectory;
  private FileSystemDatasetRepository repo;
  private Schema testSchema;

  @Before
  public void setUp() throws IOException {
    Configuration conf = new Configuration();

    conf.set("fs.default.name", "file:///");

    fileSystem = FileSystem.get(conf);
    testDirectory = new Path(Files.createTempDir().getAbsolutePath());
    repo = new FileSystemDatasetRepository(fileSystem, testDirectory,
        new FileSystemMetadataProvider(fileSystem, testDirectory));

    testSchema = Schema.createRecord("Test", "Test record schema",
        "com.cloudera.data.filesystem", false);
    testSchema.setFields(Lists.newArrayList(new Field("name", Schema
        .create(Type.STRING), null, null)));
  }

  @After
  public void tearDown() throws IOException {
    fileSystem.delete(testDirectory, true);
  }

  @Test
  public void testCreate() throws IOException {
    Dataset dataset = repo.create("test1", new DatasetDescriptor.Builder()
        .schema(testSchema).get());

    Assert.assertEquals("Dataset name is propagated", "test1",
        dataset.getName());
    Assert.assertEquals("Dataset schema is propagated", testSchema,
        dataset.getSchema());
    Assert.assertTrue("Dataset data directory exists",
        fileSystem.exists(new Path(testDirectory, "test1/data")));
    Assert.assertTrue("Dataset metadata file exists",
        fileSystem.exists(new Path(testDirectory, "test1/descriptor.avro")));
  }

  @Test
  public void testCreatePartitioned() throws IOException {
    DatasetDescriptor descriptor = new DatasetDescriptor.Builder()
        .schema(testSchema)
        .partitionStrategy(
            new PartitionStrategy.Builder().hash("name", 3).get()).get();

    Dataset dataset = repo.create("test2", descriptor);

    Assert.assertEquals("Dataset name is propagated", "test2",
        dataset.getName());
    Assert.assertEquals("Dataset schema is propagated", testSchema,
        dataset.getSchema());
    Assert.assertTrue("Dataset data directory exists",
        fileSystem.exists(new Path(testDirectory, "test2/data")));
    Assert.assertTrue("Dataset metadata file exists",
        fileSystem.exists(new Path(testDirectory, "test2/descriptor.avro")));
  }

  @Test
  public void testLoad() throws IOException {
    // Just invoke the creation test so we have a dataset to test with.
    testCreate();

    Dataset dataset = repo.get("test1");

    Assert.assertNotNull("Dataset is loaded and produced", dataset);
    Assert.assertEquals("Dataset name is propagated", "test1",
        dataset.getName());
    Assert.assertEquals("Dataset schema is loaded", testSchema,
        dataset.getSchema());
  }

}