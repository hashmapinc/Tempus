'use strict'

const good = require('good')

const goodOptions = {
  ops: {
    interval: 900000
  },
  reporters: {
    console: [
      {
        module: 'good-squeeze',
        name: 'Squeeze',
        args: [{ log: '*', ops: '*', error: '*', request: '*', response: '*' }]
      },
      {
        module: 'good-console'
      },
      'stdout'
    ]
  }
}

module.exports = [
  {
    register: good,
    options: goodOptions
  }
]
