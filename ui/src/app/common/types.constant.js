/*
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
export default angular.module('tempus.types', [])
    .constant('types',
        {
            serverErrorCode: {
                general: 2,
                authentication: 10,
                jwtTokenExpired: 11,
                permissionDenied: 20,
                invalidArguments: 30,
                badRequestParams: 31,
                itemNotFound: 32
            },
            entryPoints: {
                login: "/api/auth/login",
                tokenRefresh: "/api/auth/token",
                nonTokenBased: "/api/noauth"
            },
            id: {
                nullUid: "13814000-1dd2-11b2-8080-808080808080",
            },
            aggregation: {
                min: {
                    value: "MIN",
                    name: "aggregation.min"
                },
                max: {
                    value: "MAX",
                    name: "aggregation.max"
                },
                avg: {
                    value: "AVG",
                    name: "aggregation.avg"
                },
                sum: {
                    value: "SUM",
                    name: "aggregation.sum"
                },
                count: {
                    value: "COUNT",
                    name: "aggregation.count"
                },
                none: {
                    value: "NONE",
                    name: "aggregation.none"
                }
            },
            alarmFields: {
                createdTime: {
                    keyName: 'createdTime',
                    value: "createdTime",
                    name: "alarm.created-time",
                    time: true
                },
                startTime: {
                    keyName: 'startTime',
                    value: "startTs",
                    name: "alarm.start-time",
                    time: true
                },
                endTime: {
                    keyName: 'endTime',
                    value: "endTs",
                    name: "alarm.end-time",
                    time: true
                },
                ackTime: {
                    keyName: 'ackTime',
                    value: "ackTs",
                    name: "alarm.ack-time",
                    time: true
                },
                clearTime: {
                    keyName: 'clearTime',
                    value: "clearTs",
                    name: "alarm.clear-time",
                    time: true
                },
                originator: {
                    keyName: 'originator',
                    value: "originatorName",
                    name: "alarm.originator"
                },
                originatorType: {
                    keyName: 'originatorType',
                    value: "originator.entityType",
                    name: "alarm.originator-type"
                },
                type: {
                    keyName: 'type',
                    value: "type",
                    name: "alarm.type"
                },
                severity: {
                    keyName: 'severity',
                    value: "severity",
                    name: "alarm.severity"
                },
                status: {
                    keyName: 'status',
                    value: "status",
                    name: "alarm.status"
                }
            },
            alarmStatus: {
                activeUnack: "ACTIVE_UNACK",
                activeAck: "ACTIVE_ACK",
                clearedUnack: "CLEARED_UNACK",
                clearedAck: "CLEARED_ACK"
            },
            alarmSearchStatus: {
                any: "ANY",
                active: "ACTIVE",
                cleared: "CLEARED",
                ack: "ACK",
                unack: "UNACK"
            },
            alarmSeverity: {
                "CRITICAL": {
                    name: "alarm.severity-critical",
                    class: "tb-critical",
                    color: "red"
                },
                "MAJOR": {
                    name: "alarm.severity-major",
                    class: "tb-major",
                    color: "orange"
                },
                "MINOR": {
                    name: "alarm.severity-minor",
                    class: "tb-minor",
                    color: "#ffca3d"
                },
                "WARNING": {
                    name: "alarm.severity-warning",
                    class: "tb-warning",
                    color: "#abab00"
                },
                "INDETERMINATE": {
                    name: "alarm.severity-indeterminate",
                    class: "tb-indeterminate",
                    color: "green"
                }
            },
            auditLogActionType: {
                "ADDED": {
                    name: "audit-log.type-added"
                },
                "DELETED": {
                    name: "audit-log.type-deleted"
                },
                "UPDATED": {
                    name: "audit-log.type-updated"
                },
                "ATTRIBUTES_UPDATED": {
                    name: "audit-log.type-attributes-updated"
                },
                "ATTRIBUTES_DELETED": {
                    name: "audit-log.type-attributes-deleted"
                },
                "RPC_CALL": {
                    name: "audit-log.type-rpc-call"
                },
                "CREDENTIALS_UPDATED": {
                    name: "audit-log.type-credentials-updated"
                },
                "ASSIGNED_TO_CUSTOMER": {
                    name: "audit-log.type-assigned-to-customer"
                },
                "UNASSIGNED_FROM_CUSTOMER": {
                    name: "audit-log.type-unassigned-from-customer"
                },
                "ACTIVATED": {
                    name: "audit-log.type-activated"
                },
                "SUSPENDED": {
                    name: "audit-log.type-suspended"
                },
                "CREDENTIALS_READ": {
                    name: "audit-log.type-credentials-read"
                },
                "ATTRIBUTES_READ": {
                    name: "audit-log.type-attributes-read"
                }
            },
            auditLogActionStatus: {
                "SUCCESS": {
                    value: "SUCCESS",
                    name: "audit-log.status-success"
                },
                "FAILURE": {
                    value: "FAILURE",
                    name: "audit-log.status-failure"
                }
            },
            auditLogMode: {
                tenant: "tenant",
                entity: "entity",
                user: "user",
                customer: "customer"
            },
            aliasFilterType: {
                singleEntity: {
                    value: 'singleEntity',
                    name: 'alias.filter-type-single-entity'
                },
                entityList: {
                    value: 'entityList',
                    name: 'alias.filter-type-entity-list'
                },
                entityName: {
                    value: 'entityName',
                    name: 'alias.filter-type-entity-name'
                },
                stateEntity: {
                    value: 'stateEntity',
                    name: 'alias.filter-type-state-entity'
                },
                assetType: {
                    value: 'assetType',
                    name: 'alias.filter-type-asset-type'
                },
                deviceType: {
                    value: 'deviceType',
                    name: 'alias.filter-type-device-type'
                },
                relationsQuery: {
                    value: 'relationsQuery',
                    name: 'alias.filter-type-relations-query'
                },
                assetSearchQuery: {
                    value: 'assetSearchQuery',
                    name: 'alias.filter-type-asset-search-query'
                },
                deviceSearchQuery: {
                    value: 'deviceSearchQuery',
                    name: 'alias.filter-type-device-search-query'
                }
            },
            position: {
                top: {
                    value: "top",
                    name: "position.top"
                },
                bottom: {
                    value: "bottom",
                    name: "position.bottom"
                },
                left: {
                    value: "left",
                    name: "position.left"
                },
                right: {
                    value: "right",
                    name: "position.right"
                }
            },
            datasourceType: {
                function: "function",
                entity: "entity"
            },
            dataKeyType: {
                timeseries: "timeseries",
                attribute: "attribute",
              //##### ADDING DEPTH DATA KEY TYPE
                depthSeries: "depthSeries",
                function: "function",
                alarm: "alarm"
            },
            componentType: {
                filter: "FILTER",
                processor: "PROCESSOR",
                action: "ACTION",
                plugin: "PLUGIN"
            },
            entityType: {
                device: "DEVICE",
                asset: "ASSET",
                rule: "RULE",
                plugin: "PLUGIN",
                tenant: "TENANT",
                customer: "CUSTOMER",
                user: "USER",
                dashboard: "DASHBOARD",
                alarm: "ALARM",
                computation:"COMPUTATION",
                computationJob:"COMPUTATION_JOB"

            },
            aliasEntityType: {
                current_customer: "CURRENT_CUSTOMER"
            },
            entityTypeTranslations: {
                "DEVICE": {
                    type: 'entity.type-device',
                    typePlural: 'entity.type-devices',
                    list: 'entity.list-of-devices',
                    nameStartsWith: 'entity.device-name-starts-with'
                },
                "ASSET": {
                    type: 'entity.type-asset',
                    typePlural: 'entity.type-assets',
                    list: 'entity.list-of-assets',
                    nameStartsWith: 'entity.asset-name-starts-with'
                },
                "RULE": {
                    type: 'entity.type-rule',
                    typePlural: 'entity.type-rules',
                    list: 'entity.list-of-rules',
                    nameStartsWith: 'entity.rule-name-starts-with'
                },
                "PLUGIN": {
                    type: 'entity.type-plugin',
                    typePlural: 'entity.type-plugins',
                    list: 'entity.list-of-plugins',
                    nameStartsWith: 'entity.plugin-name-starts-with'
                },
                "TENANT": {
                    type: 'entity.type-tenant',
                    typePlural: 'entity.type-tenants',
                    list: 'entity.list-of-tenants',
                    nameStartsWith: 'entity.tenant-name-starts-with'
                },
                "CUSTOMER": {
                    type: 'entity.type-customer',
                    typePlural: 'entity.type-customers',
                    list: 'entity.list-of-customers',
                    nameStartsWith: 'entity.customer-name-starts-with'
                },
                "USER": {
                    type: 'entity.type-user',
                    typePlural: 'entity.type-users',
                    list: 'entity.list-of-users',
                    nameStartsWith: 'entity.user-name-starts-with'
                },
                "DASHBOARD": {
                    type: 'entity.type-dashboard',
                    typePlural: 'entity.type-dashboards',
                    list: 'entity.list-of-dashboards',
                    nameStartsWith: 'entity.dashboard-name-starts-with'
                },
                "ALARM": {
                    type: 'entity.type-alarm',
                    typePlural: 'entity.type-alarms',
                    list: 'entity.list-of-alarms',
                    nameStartsWith: 'entity.alarm-name-starts-with'
                },
                "COMPUTATION": {
                    type: 'entity.type-computation',
                    typePlural: 'entity.type-computations',
                    list: 'entity.list-of-computations',
                    nameStartsWith: 'entity.computation-name-starts-with'
                },
                "COMPUTATION_JOB": {
                    type: 'entity.type-computationJob',
                    typePlural: 'entity.type-computationJobs',
                    list: 'entity.list-of-computationJobs',
                    nameStartsWith: 'entity.computationJob-name-starts-with'
                },

                "CURRENT_CUSTOMER": {
                    type: 'entity.type-current-customer',
                    list: 'entity.type-current-customer'
                }
            },
            entitySearchDirection: {
                from: "FROM",
                to: "TO"
            },
            entityRelationType: {
                contains: "Contains",
                manages: "Manages"
            },
            eventType: {
                error: {
                    value: "ERROR",
                    name: "event.type-error"
                },
                lcEvent: {
                    value: "LC_EVENT",
                    name: "event.type-lc-event"
                },
                stats: {
                    value: "STATS",
                    name: "event.type-stats"
                }
            },
            extensionType: {
                http: "HTTP",
                mqtt: "MQTT",
                opc: "OPC UA",
                wits: "WITS",
                witsml: "WITSML",
                modbus: "MODBUS"
            },
            permissionType: {
                '*':"ALL",
                'ASSIGN': "ASSIGN",
                'CREATE': "CREATE",
                'DELETE': "DELETE",
                'READ': "READ",
                'UPDATE': "UPDATE",
            },
           entityTypes: {
                'ALL':'ALL',
                'ASSET': "ASSET",
                'DEVICE':"DEVICE"
            },
           userTypes: {
                'CUSTOMER_USER':'CUSTOMER_USER',
                'SYSADMIN_USER': "SYSADMIN_USER",
                'TENANT_USER': "TENANT_USER"
            },
            extensionValueType: {
                string: 'value.string',
                long: 'value.long',
                double: 'value.double',
                boolean: 'value.boolean'
            },
            extensionTransformerType: {
                toDouble: 'extension.to-double',
                custom: 'extension.custom'
            },
            mqttConverterTypes: {
                json: 'extension.converter-json',
                custom: 'extension.custom'
            },
            extensionModbusFunctionCodes: {
                1: "Read Coils (1)",
                2: "Read Discrete Inputs (2)",
                3: "Read Multiple Holding Registers (3)",
                4: "Read Input Registers (4)"
            },
            extensionModbusTransports: {
                tcp: "TCP",
                udp: "UDP",
                rtu: "RTU"
            },
            extensionModbusRtuParities: {
                none: "none",
                even: "even",
                odd: "odd"
            },
            extensionModbusRtuEncodings: {
                ascii: "ascii",
                rtu: "rtu"
            },
            mqttCredentialTypes: {
                anonymous:  {
                    value: "anonymous",
                    name: "extension.anonymous"
                },
                basic: {
                    value: "basic",
                    name: "extension.basic"
                },
                pem: {
                    value: "cert.PEM",
                    name: "extension.pem"
                }
            },
            witsmlVersions: {
              v1411: "1.4.1.1",
              v1311: "1.3.1.1"
            },
            witsmlObjectTypes: {
              wellbore: "Wellbore",
                log: "Logs",
                mudLogs: "Mudlogs",
                message: "Messages",
                rig: "Rig",
                trajectory: "Trajectory"
            },
            witsmlWellStatus: {
                active: "Active",
                drilling: "Drilling"
            },
            extensionOpcSecurityTypes: {
                Basic128Rsa15: "Basic128Rsa15",
                Basic256: "Basic256",
                Basic256Sha256: "Basic256Sha256",
                None: "None"
            },
            extensionIdentityType: {
                anonymous: "extension.anonymous",
                username: "extension.username"
            },
            extensionKeystoreType: {
                PKCS12: "PKCS12",
                JKS: "JKS"
            },
            latestTelemetry: {
                value: "LATEST_TELEMETRY",
                name: "attribute.scope-latest-telemetry",
                clientSide: true
            },
            latestDepthSeries: {
                value: "LATEST_DEPTH_SERIES",
                name: "attribute.scope-latest-depth-series",
                clientSide: true
            },
            attributesScope: {
                client: {
                    value: "CLIENT_SCOPE",
                    name: "attribute.scope-client",
                    clientSide: true
                },
                server: {
                    value: "SERVER_SCOPE",
                    name: "attribute.scope-server",
                    clientSide: false
                },
                shared: {
                    value: "SHARED_SCOPE",
                    name: "attribute.scope-shared",
                    clientSide: false
                }
            },
            valueType: {
                string: {
                    value: "string",
                    name: "value.string",
                    icon: "mdi:format-text"
                },
                integer: {
                    value: "integer",
                    name: "value.integer",
                    icon: "mdi:numeric"
                },
                double: {
                    value: "double",
                    name: "value.double",
                    icon: "mdi:numeric"
                },
                boolean: {
                    value: "boolean",
                    name: "value.boolean",
                    icon: "mdi:checkbox-marked-outline"
                }
            },
            widgetType: {
                timeseries: {
                    value: "timeseries",
                    name: "widget.timeseries",
                    template: {
                        bundleAlias: "charts",
                        alias: "basic_timeseries"
                    }
                },
                depthseries: {
                    value: "depthseries",
                    name: "widget.depthseries",
                    template: {
                        bundleAlias: "charts",
                        alias: "basic_depthseries"
                    }
                },
                latest: {
                    value: "latest",
                    name: "widget.latest-values",
                    template: {
                        bundleAlias: "cards",
                        alias: "attributes_card"
                    }
                },
                rpc: {
                    value: "rpc",
                    name: "widget.rpc",
                    template: {
                        bundleAlias: "gpio_widgets",
                        alias: "basic_gpio_control"
                    }
                },
                alarm: {
                    value: "alarm",
                    name: "widget.alarm",
                    template: {
                        bundleAlias: "alarm_widgets",
                        alias: "alarms_table"
                    }
                },
                static: {
                    value: "static",
                    name: "widget.static",
                    template: {
                        bundleAlias: "cards",
                        alias: "html_card"
                    }
                }
            },
            widgetActionSources: {
                headerButton: {
                    name: 'widget-action.header-button',
                    value: 'headerButton',
                    multiple: true
                }
            },
            widgetActionTypes: {
                openDashboardState: {
                    name: 'widget-action.open-dashboard-state',
                    value: 'openDashboardState'
                },
                updateDashboardState: {
                    name: 'widget-action.update-dashboard-state',
                    value: 'updateDashboardState'
                },
                openDashboard: {
                    name: 'widget-action.open-dashboard',
                    value: 'openDashboard'
                },
                custom: {
                    name: 'widget-action.custom',
                    value: 'custom'
                }
            },
            systemBundleAlias: {
                charts: "charts",
                cards: "cards"
            },
            translate: {
                customTranslationsPrefix: "custom."
            },
            computationType: {
                spark: "SPARK",
                kubeless: "KUBELESS",
                lambda: "AWS-LAMBDA"
            },
            runtimeTypes: {
                python27: {
                    name: "Python 2.7",
                    value: "python2.7"
                },
                python34: {
                    name: "Python 3.4",
                    value: "python3.4"
                },
                python36: {
                    name: "Python 3.6",
                    value: "python3.6"
                },
                nodejs6: {
                    name: "Node.js 6",
                    value: "nodejs6"
                },
                nodejs8: {
                    name: "Node.js 8",
                    value: "nodejs8"
                },
                ruby24: {
                    name: "Ruby 2.4",
                    value: "ruby2.4"
                },
                go110: {
                    name: "Go 1.10",
                    value: "go1.10"
                },
                java18: {
                    name: "Java 1.8",
                    value: "java1.8"
                }
            },
            lmbdaRuntimeTypes: {
                java18: {
                    name: "Java 1.8",
                    value: "java8"
                },
                python27: {
                    name: "Python 2.7",
                    value: "python2.7"
                },
                python34: {
                    name: "Python 3.7",
                    value: "python3.7"
                },
                python36: {
                    name: "Python 3.6",
                    value: "python3.6"
                },
                nodejs: {
                    name: "Node.js",
                    value: "nodejs"
                },
                nodejs43: {
                    name: "Node.js 4.3",
                    value: "nodejs4.3"
                },
                nodejs61: {
                    name: "Node.js 6.10",
                    value: "nodejs6.10"
                },
                nodejs81: {
                    name: "Node.js 8.10",
                    value: "nodejs8.10"
                },
                nodejs43edge: {
                    name: "Node.js 4.3-edge",
                    value: "nodejs4.3-edge"
                },
                dotnet21: {
                    name: "Dot Net 2.1",
                    value: "dotnetcore2.1"
                },
                dotnet20: {
                    name: "Dot Net 2.0",
                    value: "dotnetcore2.0"
                },
                dotnet10: {
                    name: "Dot Net 1.0",
                    value: "dotnetcore1.0"
                },
                ruby25: {
                    name: "Ruby 2.5",
                    value: "ruby2.5"
                },
                go110: {
                    name: "Go 1.10",
                    value: "go1.10"
                }
            },
            triggerTypes: {
                Kafka: "KUBELESS-KAFKA"
            },
            metadataSourceType: {
                jdbc: "JDBC"
            },
            metadataSinkType: {
                rest: "REST"
            }
        }
    ).name;
