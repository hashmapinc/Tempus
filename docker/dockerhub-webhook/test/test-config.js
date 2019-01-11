'use strict'

const tap = require('tap')
const config = require('../config')

tap.equal(config.port, 3000, 'Default server port ok')
tap.equal(config.route, '/api', 'Default route ok')
tap.equal(config.token, 'abc123', 'Default token ok')
