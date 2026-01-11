import { and, eq, gt, isNull, sql } from 'drizzle-orm'
import { db } from '../queries'
import { refreshes } from './schema' 

export const saveRefreshToken = async (id: string, tok: string, expIntervalSecs: number): Promise<string> => {
	const exp = new Date((Math.floor(Date.now() / 1000) + expIntervalSecs) * 1000)
	await db.insert(refreshes)
		.values({ token: tok,
			  userId: id,
			  expiresAt: exp
			})
	return tok
}

export const findUserByRefresh = async (tok: string): Promise<string | undefined> => {
	const now = new Date(Date.now())
	const rows = await db.select({ id: refreshes.userId })
		.from(refreshes)
		.where(and(eq(refreshes.token, tok),
			isNull(refreshes.revokedAt),
			gt(refreshes.expiresAt, now)
		))
	if (rows.length > 1)
		throw Error('multiple matches for refresh')
	return rows.length == 1 ? rows[0].id : undefined
}

export const revokeToken = async (tok: string): Promise<void> => {
	const now = new Date(Date.now())
	await db.update(refreshes)
		.set({ revokedAt: now })
		.where(eq(refreshes.token, tok))
}

