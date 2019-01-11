'use strict'

const Hapi = require('hapi')
const config = require('./config')
const routes = require('./routes')
const plugins = require('./plugins')

// Create a server with a host and port
const server = new Hapi.Server()

server.connection({
  port: config.port
})

// Register plugins
server.register(plugins, (err) => {
  if (err) {
    return console.error(err)
  }
})

// Add the routes
server.route(routes)

// Start the server
module.exports.start = () => {
  server.start(() => {
    server.log(`Server running at: ${server.info.uri}`)
    server.log(`Serving hook at: ${server.info.uri}${config.route}/${config.token}`)
  })
}

// Stop the server
module.exports.stop = () => {
  server.stop(() => {
    console.log('Server stopped')
  })
}
