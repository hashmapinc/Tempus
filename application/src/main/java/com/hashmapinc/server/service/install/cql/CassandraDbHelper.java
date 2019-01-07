/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hashmapinc.server.service.install.cql;

import com.datastax.driver.core.*;
import com.hashmapinc.server.common.msg.exception.TempusRuntimeException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static com.hashmapinc.server.service.install.DatabaseHelper.CSV_DUMP_FORMAT;

public class CassandraDbHelper {

    private CassandraDbHelper() {
    }

    public static Path dumpCfIfExists(KeyspaceMetadata ks, Session session, String cfName,
                                      String[] columns, String[] defaultValues, String dumpPrefix) throws IOException {
        return dumpCfIfExists(ks, session, cfName, columns, defaultValues, dumpPrefix, false);
    }

    public static Path dumpCfIfExists(KeyspaceMetadata ks, Session session, String cfName,
                                      String[] columns, String[] defaultValues, String dumpPrefix, boolean printHeader) throws IOException {
        if (ks.getTable(cfName) != null) {
            Path dumpFile = Files.createTempFile(dumpPrefix, null);
            Files.deleteIfExists(dumpFile);
            CSVFormat csvFormat = CSV_DUMP_FORMAT;
            if (printHeader) {
                csvFormat = csvFormat.withHeader(columns);
            }
            try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(dumpFile), csvFormat)) {
                Statement stmt = new SimpleStatement("SELECT * FROM " + cfName);
                stmt.setFetchSize(1000);
                ResultSet rs = session.execute(stmt);
                Iterator<Row> iter = rs.iterator();
                while (iter.hasNext()) {
                    Row row = iter.next();
                    if (row != null) {
                        dumpRow(row, columns, defaultValues, csvPrinter);
                    }
                }
            }
            return dumpFile;
        } else {
            return null;
        }
    }

    public static void appendToEndOfLine(Path targetDumpFile, String toAppend) throws IOException {
        Path tmp = Files.createTempFile(null, null);
        try (CSVParser csvParser = new CSVParser(Files.newBufferedReader(targetDumpFile), CSV_DUMP_FORMAT)) {
            try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(tmp), CSV_DUMP_FORMAT)) {
                csvParser.forEach(record -> {
                    List<String> newRecord = new ArrayList<>();
                    record.forEach(newRecord::add);
                    newRecord.add(toAppend);
                    try {
                        csvPrinter.printRecord(newRecord);
                    } catch (IOException e) {
                        throw new TempusRuntimeException("Error appending to EOL", e);
                    }
                });
            }
        }
        Files.move(tmp, targetDumpFile, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void loadCf(KeyspaceMetadata ks, Session session, String cfName, String[] columns, Path sourceFile) throws IOException {
        loadCf(ks, session, cfName, columns, sourceFile, false);
    }

    public static void loadCf(KeyspaceMetadata ks, Session session, String cfName, String[] columns, Path sourceFile, boolean parseHeader) throws IOException {
        TableMetadata tableMetadata = ks.getTable(cfName);
        PreparedStatement prepared = session.prepare(createInsertStatement(cfName, columns));
        CSVFormat csvFormat = CSV_DUMP_FORMAT;
        if (parseHeader) {
            csvFormat = csvFormat.withFirstRecordAsHeader();
        } else {
            csvFormat = CSV_DUMP_FORMAT.withHeader(columns);
        }
        try (CSVParser csvParser = new CSVParser(Files.newBufferedReader(sourceFile), csvFormat)) {
            csvParser.forEach(record -> {
                BoundStatement boundStatement = prepared.bind();
                for (String column : columns) {
                    setColumnValue(tableMetadata, column, record, boundStatement);
                }
                session.execute(boundStatement);
            });
        }
    }


    private static void dumpRow(Row row, String[] columns, String[] defaultValues, CSVPrinter csvPrinter) throws IOException {
        List<String> record = new ArrayList<>();
        for (int i=0;i<columns.length;i++) {
            String column = columns[i];
            String defaultValue;
            if (defaultValues != null && i < defaultValues.length) {
                defaultValue = defaultValues[i];
            } else {
                defaultValue = "";
            }
            record.add(getColumnValue(column, defaultValue, row));
        }
        csvPrinter.printRecord(record);
    }

    private static String getColumnValue(String column, String defaultValue, Row row) {
        int index = row.getColumnDefinitions().getIndexOf(column);
        if (index > -1) {
            String str;
            DataType type = row.getColumnDefinitions().getType(index);
            try {
                if (row.isNull(index)) {
                    return null;
                } else if (type == DataType.cdouble()) {
                    str = Double.toString(row.getDouble(index));
                } else if (type == DataType.cint()) {
                    str = Integer.toString(row.getInt(index));
                } else if (type == DataType.uuid()) {
                    str = row.getUUID(index).toString();
                } else if (type == DataType.timeuuid()) {
                    str = row.getUUID(index).toString();
                } else if (type == DataType.cfloat()) {
                    str = Float.toString(row.getFloat(index));
                } else if (type == DataType.timestamp()) {
                    str = ""+row.getTimestamp(index).getTime();
                } else {
                    str = row.getString(index);
                }
            } catch (Exception e) {
                str = "";
            }
            return str;
        } else {
            return defaultValue;
        }
    }

    private static String createInsertStatement(String cfName, String[] columns) {
        StringBuilder insertStatementBuilder = new StringBuilder();
        insertStatementBuilder.append("INSERT INTO ").append(cfName).append(" (");
        for (String column : columns) {
            insertStatementBuilder.append(column).append(",");
        }
        insertStatementBuilder.deleteCharAt(insertStatementBuilder.length() - 1);
        insertStatementBuilder.append(") VALUES (");
        Arrays.stream(columns).forEach(column -> insertStatementBuilder.append("?").append(","));
        insertStatementBuilder.deleteCharAt(insertStatementBuilder.length() - 1);
        insertStatementBuilder.append(")");
        return insertStatementBuilder.toString();
    }

    private static void setColumnValue(TableMetadata tableMetadata, String column,
                                       CSVRecord record, BoundStatement boundStatement) {
        String value = record.get(column);
        DataType type = tableMetadata.getColumn(column).getType();
        if (value == null) {
            boundStatement.setToNull(column);
        } else if (type == DataType.cdouble()) {
            boundStatement.setDouble(column, Double.valueOf(value));
        } else if (type == DataType.cint()) {
            boundStatement.setInt(column, Integer.valueOf(value));
        } else if (type == DataType.uuid()) {
            boundStatement.setUUID(column, UUID.fromString(value));
        } else if (type == DataType.timeuuid()) {
            boundStatement.setUUID(column, UUID.fromString(value));
        } else if (type == DataType.cfloat()) {
            boundStatement.setFloat(column, Float.valueOf(value));
        } else if (type == DataType.timestamp()) {
            boundStatement.setTimestamp(column, new Date(Long.valueOf(value)));
        } else {
            boundStatement.setString(column, value);
        }
    }

}
