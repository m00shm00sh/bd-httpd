import { sql } from "drizzle-orm"
import { pgTable, unique, timestamp, uuid, integer, text } from "drizzle-orm/pg-core"

const _timestamp = {
	createdAt: timestamp('created_at').notNull().defaultNow(),
	updatedAt: timestamp('updated_at').notNull().defaultNow(),
}
const _idTimestamp = {
	id: uuid('id').defaultRandom().notNull().primaryKey(),
	..._timestamp,
}

export const users = pgTable("users", {
	..._idTimestamp,
	email: text('email').notNull().unique(),
	pass: text('pass').notNull().default(""),
	isChirpyRed: integer('is_chirpy_red').notNull().default(0),
})

export const chirps = pgTable("chirps", {
	..._idTimestamp,
	body: text('body').notNull(),
	userId: uuid('user_id').references(() => users.id, {onDelete: 'cascade'}),
})

export const refreshes = pgTable("refresh_tokens", {
	token: text('token').notNull().primaryKey(),
	..._timestamp,
	userId: uuid('user_id').notNull().references(() => users.id, {onDelete: 'cascade'}),
	expiresAt: timestamp('expires_at').notNull(),
	revokedAt: timestamp('revoked_at')
})

