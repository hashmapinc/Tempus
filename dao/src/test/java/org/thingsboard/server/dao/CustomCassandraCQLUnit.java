/**
 * Copyright © 2016-2018 The Thingsboard Authors
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
package org.thingsboard.server.dao;

import org.cassandraunit.BaseCassandraUnit;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomCassandraCQLUnit extends BaseCassandraUnit {
    private List<CQLDataSet> dataSets;
    private String upgradePath = null;

    public Session session;
    public Cluster cluster;

    public CustomCassandraCQLUnit(List<CQLDataSet> dataSets) {
        this.dataSets = dataSets;
    }

    public CustomCassandraCQLUnit(List<CQLDataSet> dataSets, int readTimeoutMillis) {
        this.dataSets = dataSets;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public CustomCassandraCQLUnit(List<CQLDataSet> dataSets, String configurationFileName) {
        this(dataSets);
        this.configurationFileName = configurationFileName;
    }

    public CustomCassandraCQLUnit(List<CQLDataSet> dataSets, String configurationFileName, int readTimeoutMillis) {
        this(dataSets);
        this.configurationFileName = configurationFileName;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public CustomCassandraCQLUnit(List<CQLDataSet> dataSets, String configurationFileName, long startUpTimeoutMillis, String upgradePath) {
        super(startUpTimeoutMillis);
        this.dataSets = dataSets;
        this.configurationFileName = configurationFileName;
        this.upgradePath = upgradePath;
    }

    public CustomCassandraCQLUnit(List<CQLDataSet> dataSets, String configurationFileName, long startUpTimeoutMillis, int readTimeoutMillis) {
        super(startUpTimeoutMillis);
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
        List<CQLDataSet> totalSet = new ArrayList<>();
        totalSet.addAll(dataSets);
        if (this.upgradePath != null){
            List<CQLDataSet> upgradeSet = getDataSetLists();
            totalSet.addAll(upgradeSet);
        }
        CQLDataLoader dataLoader = new CQLDataLoader(session);
        totalSet.forEach(dataLoader::load);
        session = dataLoader.getSession();
    }

    private List<CQLDataSet> getDataSetLists(){
        List<CQLDataSet> dataSets = new ArrayList<>();
        dataSets.add(new ClassPathCQLDataSet("cassandra/schema.cql", false, false));
        dataSets.add(new ClassPathCQLDataSet("cassandra/system-data.cql", false, false));
        String upgradePath = this.upgradePath;
        URL url = this.getClass().getClassLoader().getResource(upgradePath);
        String path = url.getPath();
        Path upgradeScriptsDirectory = Paths.get(path);
        List<Integer> sortedScriptsIndexes = null;
        try {
            sortedScriptsIndexes = Files.list(upgradeScriptsDirectory).map(a -> stripExtensionFromName(a.getFileName().toString())).sorted().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Integer i: sortedScriptsIndexes) {
            String scriptFileName = upgradePath + i.toString()+".cql";
            dataSets.add(new ClassPathCQLDataSet(scriptFileName, false, false));
        }
        return dataSets;
    }

    private static Integer stripExtensionFromName(String fileName) {
        return Integer.parseInt(fileName.substring(0, fileName.indexOf(".cql")));
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
}
