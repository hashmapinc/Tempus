'use strict'

const envs = process.env

module.exports = {
  port: envs.SERVER_PORT || 3000,
  route: envs.API_ROUTE || '/api',
  token: envs.TOKEN || 'abc123',
  callbackDisable: envs.CALLBACK_DISABLE || true // set to true if you want to disable callback to dockerhub
}
