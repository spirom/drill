/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.jdbc;

import net.hydromatic.avatica.ColumnMetaData;

import org.apache.drill.common.expression.SchemaPath;
import org.apache.drill.exec.record.BatchSchema;
import org.apache.drill.exec.record.MaterializedField;
import org.apache.drill.jdbc.impl.DrillColumnMetaDataList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Iterator;

import static org.apache.drill.common.types.TypeProtos.MajorType;
import static org.apache.drill.common.types.TypeProtos.MinorType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DrillColumnMetaDataListTest {

  private DrillColumnMetaDataList emptyList;

  private DrillColumnMetaDataList oneElementList;

  private DrillColumnMetaDataList twoElementList;

  private ColumnMetaData exampleIntColumn = new ColumnMetaData(
    0, false, false, false, false, 0, true, 10, "intLabel", "intColName", "schemaName",
    0, 1, ",myTable", "myCategory", new ColumnMetaData.ScalarType( 1, "myIntType", ColumnMetaData.Rep.INTEGER ),
    true, false, false, Integer.class.getName() );

  private ColumnMetaData exampleStringColumn = new ColumnMetaData(
    0, false, false, false, false, 0, true, 10, "stringLabel", "stringColName", "schemaName",
    0, 1, ",myTable", "myCategory", new ColumnMetaData.ScalarType( 1, "myStringType", ColumnMetaData.Rep.STRING ),
    false, true, true, String.class.getName() );

  @Before
  public void setUp() throws Exception {
    emptyList = new DrillColumnMetaDataList();

    // Create mock columns
    final MaterializedField exampleIntField = mock(MaterializedField.class);
    MajorType exampleIntType = MajorType.newBuilder().setMinorType(MinorType.INT).build();
    when(exampleIntField.getAsSchemaPath()).thenReturn(SchemaPath.getSimplePath("/path/to/testInt"));
    when(exampleIntField.getType()).thenReturn(exampleIntType);

    final MaterializedField exampleStringField = mock(MaterializedField.class);
    MajorType exampleStringType = MajorType.newBuilder().setMinorType(MinorType.VARCHAR).build();
    when(exampleStringField.getAsSchemaPath()).thenReturn(SchemaPath.getSimplePath("/path/to/testString"));
    when(exampleStringField.getType()).thenReturn(exampleStringType);

    oneElementList = new DrillColumnMetaDataList();
    BatchSchema oneElementSchema = mock(BatchSchema.class);
    when(oneElementSchema.getFieldCount()).thenReturn(1);
    doAnswer(
        new Answer<MaterializedField>() {

          @Override
          public MaterializedField answer(InvocationOnMock invocationOnMock) throws Throwable {
            Integer index = (Integer) invocationOnMock.getArguments()[0];
            if (index == 0) {
              return exampleIntField;
            }
            return null;
          }
        }
    ).when(oneElementSchema).getColumn(Mockito.anyInt());
    oneElementList.updateColumnMetaData("testCatalog", "testSchema", "testTable", oneElementSchema);

    twoElementList = new DrillColumnMetaDataList();
    BatchSchema twoElementSchema = mock(BatchSchema.class);
    when(twoElementSchema.getFieldCount()).thenReturn(2);
    doAnswer(
      new Answer<MaterializedField>() {

        @Override
        public MaterializedField answer(InvocationOnMock invocationOnMock) throws Throwable {
          Integer index = (Integer) invocationOnMock.getArguments()[0];
          if (index == 0) {
            return exampleIntField;
          } else if (index == 1) {
            return exampleStringField;
          }
          return null;
        }
      }
    ).when(twoElementSchema).getColumn(Mockito.anyInt());
    twoElementList.updateColumnMetaData("testCatalog", "testSchema", "testTable", twoElementSchema);
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testSize() throws Exception {
    assertEquals("Default constructor should give empty list", 0, emptyList.size());
    assertEquals(1, oneElementList.size());
    assertEquals(2, twoElementList.size());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetFromEmptyList() throws Exception {
    emptyList.get(0);
  }

  @Test
  public void testGetFromNonEmptyList() throws Exception {
    assertEquals(oneElementList.get(0).columnName, "/path/to/testInt");
    assertEquals(twoElementList.get(0).columnName, "/path/to/testInt");
    assertEquals(twoElementList.get(1).columnName, "/path/to/testString");
  }

  @Test
  public void testUpdateColumnMetaData() throws Exception {

  }

  @Test
  public void testIsEmpty() throws Exception {
    assertTrue("Default constructor should give empty list", emptyList.isEmpty());
    assertFalse("One-element List should not be empty", oneElementList.isEmpty());
    assertFalse("Two-element List should not be empty", twoElementList.isEmpty());
  }

  @Test
  public void testContains() throws Exception {

    assertFalse(emptyList.contains(exampleIntColumn));
    assertFalse(emptyList.contains(exampleStringColumn));

    assertTrue(oneElementList.contains(oneElementList.get(0)));
    assertFalse(oneElementList.contains(exampleStringColumn));

    assertTrue(twoElementList.contains(twoElementList.get(0)));
    assertTrue(twoElementList.contains(twoElementList.get(1)));
    assertFalse(twoElementList.contains(exampleStringColumn));
  }

  @Test
  public void testIterator() throws Exception {
    assertFalse(emptyList.iterator().hasNext());

    Iterator<ColumnMetaData> iterator1 = oneElementList.iterator();
    assertNotNull(iterator1);
    assertTrue(iterator1.hasNext());
    assertEquals(iterator1.next(), oneElementList.get(0));
    assertFalse(iterator1.hasNext());

    Iterator<ColumnMetaData> iterator2 = twoElementList.iterator();
    assertNotNull(iterator2);
    assertTrue(iterator2.hasNext());
    assertEquals(iterator2.next(), twoElementList.get(0));
    assertTrue(iterator2.hasNext());
    assertEquals(iterator2.next(), twoElementList.get(1));
    assertFalse(iterator2.hasNext());
  }

  @Test
  public void testToArray() throws Exception {
    assertEquals(0, emptyList.toArray().length);
    assertEquals(1, oneElementList.toArray().length);
    assertEquals(2, twoElementList.toArray().length);
  }

  @Test
  public void testToArrayWithParam() throws Exception {
    ColumnMetaData[] colArray0 = emptyList.toArray(new ColumnMetaData[] {});
    assertEquals(0, colArray0.length);
    ColumnMetaData[] colArray1 = oneElementList.toArray(new ColumnMetaData[] {});
    assertEquals(1, colArray1.length);
    ColumnMetaData[] colArray2 = twoElementList.toArray(new ColumnMetaData[] {});
    assertEquals(2, colArray2.length);
  }

  @Test
  public void testIndexOf() throws Exception {
    assertEquals(-1, emptyList.indexOf(exampleIntColumn));
    assertEquals(-1, oneElementList.indexOf(exampleIntColumn));
    assertEquals(-1, twoElementList.indexOf(exampleIntColumn));

    assertEquals(0, oneElementList.indexOf(oneElementList.get(0)));
    assertEquals(0, twoElementList.indexOf(twoElementList.get(0)));

    assertEquals(1, twoElementList.indexOf(twoElementList.get(1)));
  }

  @Test
  public void testLastIndexOf() throws Exception {
    assertEquals(-1, emptyList.lastIndexOf(exampleIntColumn));
    assertEquals(-1, oneElementList.lastIndexOf(exampleIntColumn));
    assertEquals(-1, twoElementList.lastIndexOf(exampleIntColumn));

    assertEquals(0, oneElementList.lastIndexOf(oneElementList.get(0)));
    assertEquals(0, twoElementList.lastIndexOf(twoElementList.get(0)));

    assertEquals(1, twoElementList.lastIndexOf(twoElementList.get(1)));
  }
}
