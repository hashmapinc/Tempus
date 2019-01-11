'use strict'

const tap = require('tap')
const runScript = require('../lib/run-script')

tap.test('runScript missing options', (t) => {
  const options = false
  runScript(options)
  .catch((err) => {
    t.equal(err.message, 'Missing required input: options', 'runScript missing options ok')
    t.end()
  })
})

tap.test('runScript missing options.script', (t) => {
  const options = {}
  runScript(options)
  .catch((err) => {
    t.equal(err.message, 'Missing required input: options.script', 'runScript missing options.script ok')
    t.end()
  })
})

tap.test('runScript script do not exist', (t) => {
  const options = {
    script: 'dengalevandrer'
  }
  runScript(options)
  .catch((err) => {
    t.match(err.message, 'does not exist', 'runScript script do not exist ok')
    t.end()
  })
})

tap.test('runScript run fail script', (t) => {
  const options = {
    script: 'fail.sh'
  }
  runScript(options)
  .then((data) => {
    t.equal(data.state, 'error', 'runScript fails ok')
    t.end()
  })
  .catch((err) => {
    throw err
  })
})

tap.test('runScript run through', (t) => {
  const options = {
    script: 'hello.sh'
  }
  runScript(options)
  .then((data) => {
    t.equal(data.description, 'Running dummy script\n\n\n', 'runScript runs ok')
    t.end()
  })
  .catch((err) => {
    throw err
  })
})

tap.test('runScript run through with parameters', (t) => {
  const options = {
    script: 'hello.sh parameter1 parameter2'
  }
  runScript(options)
  .then((data) => {
    t.equal(data.description, 'Running dummy script\nparameter1\nparameter2\n', 'runScript runs ok')
    t.end()
  })
  .catch((err) => {
    throw err
  })
})
