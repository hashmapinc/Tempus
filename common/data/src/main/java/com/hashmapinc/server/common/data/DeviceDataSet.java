package com.hashmapinc.server.common.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceDataSet {
    private Map<String, Map<String, String>> tableRowsGroupedByKeyColumn;
    private List<String> headerColumns;
    private String keyColumnName;

    public DeviceDataSet(Map<String, Map<String, String>> tableRowsGroupedByKeyColumn, List<String> headerColumns, String keyColumnName) {
        this.tableRowsGroupedByKeyColumn = tableRowsGroupedByKeyColumn;
        this.headerColumns = headerColumns;
        this.keyColumnName = keyColumnName;
    }

    public String[] getHeaderRow() {
        String[] headerArr = new String[headerColumns.size()];
        return headerColumns.toArray(headerArr);
    }

    public List<String[]> getDataRows() {
        List<String[]> dataRows = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> entry : tableRowsGroupedByKeyColumn.entrySet()) {
            String key = entry.getKey();
            Map<String, String> attributesValues = entry.getValue();

            String[] rowToWrite = new String[headerColumns.size()];
            for (int i = 0; i < headerColumns.size(); i++) {
                String headerColumn = headerColumns.get(i);
                if (headerColumn.equals(keyColumnName)) {
                    rowToWrite[i] = key;
                } else {
                    if (attributesValues.keySet().contains(headerColumn)) {
                        rowToWrite[i] = attributesValues.get(headerColumn);
                    } else {
                        rowToWrite[i] = "";
                    }
                }
            }
            dataRows.add(rowToWrite);
        }
        return dataRows;
    }
}
