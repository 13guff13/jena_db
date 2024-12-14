/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.core.assembler;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphZero;

/**
 * An assembler that creates datasets that do nothing, either a sink or an always empty one.
 *
 * @see DatasetGraphZero
 */

public class DatasetZeroAssembler extends NamedDatasetAssembler {

    public static Resource getType() { return DatasetAssemblerVocab.tDatasetZero; }

    public DatasetZeroAssembler() {}

    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {
        DatasetGraph dsg = DatasetGraphZero.create();
        AssemblerUtils.mergeContext(root, dsg.getContext());
        return dsg;
    }
}
