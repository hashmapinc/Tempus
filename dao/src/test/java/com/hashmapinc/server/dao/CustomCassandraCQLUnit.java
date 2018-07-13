/**
 * Copyright © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.server.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.hashmapinc.server.dao.model.ModelConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.cassandraunit.BaseCassandraUnit;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.hashmapinc.server.dao.model.ModelConstants.tempus_KEYSPACE;

public class CustomCassandraCQLUnit extends BaseCassandraUnit {
    private List<CQLDataSet> dataSets;
    private List<NamedDataset> upgrades;

    public Session session;
    public Cluster cluster;

    public CustomCassandraCQLUnit(List<CQLDataSet> dataSets, List<NamedDataset> upgrades) {
        this.dataSets = dataSets;
        this.upgrades = upgrades;
    }

    public CustomCassandraCQLUnit(List<CQLDataSet> dataSets, List<NamedDataset> upgrades, int readTimeoutMillis) {
        this(dataSets, upgrades);
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public CustomCassandraCQLUnit(List<CQLDataSet> dataSets, List<NamedDataset> upgrades, String configurationFileName) {
        this(dataSets, upgrades);
        this.configurationFileName = configurationFileName;
    }

    public CustomCassandraCQLUnit(List<CQLDataSet> dataSets, List<NamedDataset> upgrades, String configurationFileName, int readTimeoutMillis) {
        this(dataSets, upgrades);
        this.configurationFileName = configurationFileName;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public CustomCassandraCQLUnit(List<CQLDataSet> dataSets, List<NamedDataset> upgrades, String configurationFileName, long startUpTimeoutMillis) {
        super(startUpTimeoutMillis);
        this.upgrades = upgrades;
        this.dataSets = dataSets;
        this.configurationFileName = configurationFileName;
    }

    public CustomCassandraCQLUnit(List<CQLDataSet> dataSets, List<NamedDataset> upgrades, String configurationFileName, long startUpTimeoutMillis, int readTimeoutMillis) {
        super(startUpTimeoutMillis);
        this.upgrades = upgrades;
        this.dataSets = dataSets;
        this.configurationFileName = configurationFileName;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    @Override
    protected void load() {
        String hostIp = EmbeddedCassandraServerHelper.getHost();
        int port = EmbeddedCassandraServerHelper.getNativeTransportPort();
        cluster = new Cluster.Builder().addContactPoints(hostIp).withPort(port).withSocketOptions(getSocketOptions())
                .build();
        session = cluster.connect();
        CQLDataLoader dataLoader = new CQLDataLoader(session);

        dataSets.forEach(dataLoader::load);
        ResultSet resultSet = session.execute("select "+ ModelConstants.INSTALLED_SCRIPTS_COLUMN+" from " +tempus_KEYSPACE +"." + ModelConstants.INSTALLED_SCHEMA_VERSIONS+";");
        Iterator<Row> rowIterator = resultSet.iterator();

        List<String> executedUpgrades = new ArrayList<>();

        while(rowIterator.hasNext()) {
            Row row = rowIterator.next();
            executedUpgrades.add(row.getString(ModelConstants.INSTALLED_SCRIPTS_COLUMN));
        }
        upgrades.stream().filter(n -> !executedUpgrades.contains(n.getName()))
                .forEach(c -> {
                    dataLoader.load(c.getDataSet());
                    session.execute("insert into " + tempus_KEYSPACE + "." + ModelConstants.INSTALLED_SCHEMA_VERSIONS + "(" + ModelConstants.INSTALLED_SCRIPTS_COLUMN + ")" + " values('" + c.getName() + "'" + ")");
                });
        session = dataLoader.getSession();
    }

    @Override
    protected void after() {
        super.after();
        try (Cluster c = cluster; Session s = session) {
            session = null;
            cluster = null;
        }
    }

    // Getters for those who do not like to directly access fields

    public Session getSession() {
        return session;
    }

    public Cluster getCluster() {
        return cluster;
    }

    @Data
    @AllArgsConstructor
    public static class NamedDataset{
        private final String name;
        private final CQLDataSet dataSet;
    }
}
