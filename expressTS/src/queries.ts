import postgres from 'postgres'
import { drizzle } from 'drizzle-orm/postgres-js'

import * as schema from './db/schema'

import { config } from './config'

const conn = postgres(config.dbUrl)
export const db = drizzle(conn, { schema })

