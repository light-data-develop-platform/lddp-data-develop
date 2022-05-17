/*
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

package com.dtstack.taier.develop.utils.develop.sync.format.writer;

import com.dtstack.taier.develop.utils.develop.sync.format.ColumnType;
import com.dtstack.taier.develop.utils.develop.sync.format.TypeFormat;


public class OracleWriterFormat implements TypeFormat {

    @Override
    public String formatToString(String str) {
        return format(str).name();
    }

    private ColumnType format(String str) {
        ColumnType originType = ColumnType.fromString(str);
        switch (originType) {
            case BIT:
            case TINYINT:
            case SMALLINT:
            case INT:
            case MEDIUMINT:
            case INTEGER:
            case YEAR:
            case INT2:
            case INT4:
            case INT8:
            case BIGINT:
            case DOUBLE:
            case DOUBLE_PRECISION:
            case DECIMAL:
            case NUMBER:
                return ColumnType.NUMBER;
            case REAL:
                return ColumnType.REAL;
            case FLOAT:
            case FLOAT2:
            case FLOAT4:
            case FLOAT8:
                return ColumnType.FLOAT;
            case BINARY_DOUBLE:
                return ColumnType.BINARY_DOUBLE;
            case NUMERIC:
                return ColumnType.NUMERIC;
            case VARCHAR:
            case VARCHAR2:
            case CHAR:
            case CHARACTER:
            case NCHAR:
            case TINYTEXT:
            case TEXT:
            case MEDIUMTEXT:
            case LONGTEXT:
            case LONGVARCHAR:
            case LONGNVARCHAR:
            case NVARCHAR:
            case NVARCHAR2:
            case TIME:
            case BOOLEAN:
                return ColumnType.VARCHAR2;
            case STRING:
            case CLOB:
                return ColumnType.CLOB;
            case BINARY:
            case BLOB:
                return ColumnType.BLOB;
            case DATE:
            case SMALLDATETIME:
                return ColumnType.DATE;
            case TIMESTAMP:
            case DATETIME:
                return ColumnType.TIMESTAMP;
            default:
                throw new IllegalArgumentException();
        }
    }

}
