'use strict'

const tap = require('tap')
const dockerhubCallback = require('../lib/dockerhub-callback')

tap.test('dockerhubCallback missing options', (t) => {
  const options = false
  return dockerhubCallback(options)
  .catch((err) => {
    t.equal(err.message, 'Missing required input: options', 'Missing options ok')
    t.end()
  })
})

tap.test('dockerhubCallback missing options.callbackUrl', (t) => {
  const options = {}
  return dockerhubCallback(options)
  .catch((err) => {
    t.equal(err.message, 'Missing required input: options.callbackUrl', 'Missing options.callbackUrl ok')
    t.end()
  })
})

tap.test('dockerhubCallback invalid URI', (t) => {
  const options = {
    callbackUrl: 'wrongurl'
  }
  return dockerhubCallback(options)
  .catch((err) => {
    t.equal(err.message, `Invalid URI "${options.callbackUrl}"`, 'Wrong URL ok')
    t.end()
  })
})

tap.test('dockerhubCallback callback off', (t) => {
  const options = {
    callbackUrl: 'https://maccyber.io/api/test',
    callbackDisable: true
  }
  return dockerhubCallback(options)
  .then((data) => {
    t.notOk(data.callback, 'dockerhubCallback off ok')
  })
})

tap.test('dockerhubCallback', (t) => {
  const options = {
    callbackUrl: 'https://maccyber.io/api/test'
  }
  return dockerhubCallback(options)
  .then((data) => {
    t.equal(data.callback.text, `Callback sent to ${options.callbackUrl}`, 'dockerhubCallback ok')
    t.equal(data.callback.response.test, 'ok', 'dockerhubCallback response ok')
    t.end()
  })
  .catch((err) => {
    throw err
  })
})
