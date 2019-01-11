'use strict'

const tap = require('tap')
const Hapi = require('hapi')
const config = require('../config')
const routes = require('../routes')

const server = new Hapi.Server()
server.connection()
server.route(routes)

tap.test('GET /', (t) => {
  server.inject('/', (res) => {
    t.equal(res.statusCode, 200, 'Status code ok')
    t.equal(res.result.message, '(Nothing but) Flowers', 'Message ok')
    t.end()
  })
})

tap.test('Not found', (t) => {
  const route = `/wrongroute`
  server.inject(route, (res) => {
    t.equal(res.statusCode, 404, '404 returned OK')
    t.end()
  })
})

tap.test('Invalid token', (t) => {
  const options = {
    method: 'POST',
    url: `${config.route}/wrongtoken`,
    payload: {}
  }
  server.inject(options, (res) => {
    t.equal(res.statusCode, 400, 'Status code ok')
    t.equal(res.result.message, 'Invalid token', 'Error message ok')
    t.end()
  })
})

tap.test('Missing payload', (t) => {
  const options = {
    method: 'POST',
    url: `${config.route}/${config.token}`
  }
  server.inject(options, (res) => {
    t.equal(res.statusCode, 400, 'Status code ok')
    t.equal(res.result.message, 'Missing payload', 'Error message ok')
    t.end()
  })
})

tap.test('Missing payload.repository', (t) => {
  const options = {
    method: 'POST',
    url: `${config.route}/${config.token}`,
    payload: {}
  }
  server.inject(options, (res) => {
    t.equal(res.statusCode, 400, 'Status code ok')
    t.equal(res.result.message, 'Missing payload.repository', 'Error message ok')
    t.end()
  })
})

tap.test('Missing payload.repository.repo_name', (t) => {
  const options = {
    method: 'POST',
    url: `${config.route}/${config.token}`,
    payload: {
      repository: {}
    }
  }
  server.inject(options, (res) => {
    t.equal(res.statusCode, 400, 'Status code ok')
    t.equal(res.result.message, 'Missing payload.repository.repo_name', 'Error message ok')
    t.end()
  })
})

tap.test('does not exist in scripts/index.js', (t) => {
  const file = require('./data/dockerhub.json')
  file.repository.repo_name = 'wrong'
  const options = {
    method: 'POST',
    url: `${config.route}/${config.token}`,
    payload: file
  }
  server.inject(options, (res) => {
    t.equal(res.statusCode, 400, 'Status code ok')
    t.equal(res.result.message, `${file.repository.repo_name} does not exist in scripts/index.js`, 'Error message ok')
    t.end()
  })
})

tap.test('Valid dockerhub JSON', (t) => {
  const file = require('./data/dockerhub.json')
  file.repository.repo_name = 'maccyber/testhook'
  const options = {
    method: 'POST',
    url: `${config.route}/${config.token}`,
    payload: file
  }
  server.inject(options, (res) => {
    t.equal(res.statusCode, 204, 'Valid dockerhub JSON ok')
    t.end()
  })
})
