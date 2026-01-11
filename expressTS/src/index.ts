import express from 'express'

import { config } from './config'
import { route } from './routes'

const app = express()

route(app)
console.log(`listening on ${config.port}`)
app.listen(config.port)
