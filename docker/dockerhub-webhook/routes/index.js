'use strict'

const Joi = require('joi')
const config = require('../config')
const handler = require('../handlers')

module.exports = [
  {
    method: 'GET',
    path: '/',
    handler: (request, reply) => {
      reply(
        {
          message: '(Nothing but) Flowers'
        }
      )
    }
  },
  {
    method: 'POST',
    path: config.route + '/{token}',
    handler: handler,
    config: {
      validate: {
        params: {
          token: Joi.string().min(3).max(300).required()
        }
      }
    }
  }
]
