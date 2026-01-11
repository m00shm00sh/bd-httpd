import { and, asc, desc, eq, sql } from 'drizzle-orm'
import { db } from '../queries'
import { chirps } from './schema'

import { ChirpRequest, ChirpResponse } from '../models'

export const createChirp = async (e: ChirpRequest, uid: string): Promise<ChirpResponse> => {
	const [row] = await db.insert(chirps)
		.values({ body: e.body,
			  userId: uid,
			})
		.returning({ id: chirps.id,
			     createdAt: chirps.createdAt,
			     updatedAt: chirps.updatedAt,
			   })
	return { id: row.id,
		 body: e.body,
		 userId: uid,
		 createdAt: row.createdAt,
		 updatedAt: row.updatedAt,
	}
}

export const getChirps = async ({uid, sort}: {uid?: string, sort: number}): Promise<ChirpResponse[]> => {
	let q = db.select({ id: chirps.id,
			      body: chirps.body,
			      userId: chirps.userId,
			      createdAt: chirps.createdAt,
			      updatedAt: chirps.updatedAt,
			    }).from(chirps)
	if (uid !== undefined)
		q = q.where(eq(chirps.userId, uid)) as typeof q
	if (sort !== undefined) {
		if (sort > 0)
			q = q.orderBy(asc(chirps.createdAt)) as typeof q
		else if (sort < 0)
			q = q.orderBy(desc(chirps.createdAt)) as typeof q
	}
	return (await q) as ChirpResponse[]
}
	
export const getChirpById = async (id: string): Promise<ChirpResponse|undefined> => {
	const rows = await db
		.select({ id: chirps.id,
		          body: chirps.body,
		          userId: chirps.userId,
		          createdAt: chirps.createdAt,
		          updatedAt: chirps.updatedAt,
		        })
		.from(chirps)
		.where(eq(chirps.id, id))
	if (rows.length != 1)
		return undefined
	return rows[0] as ChirpResponse
}

export const deleteChirp = async (id: string): Promise<void> => {
	await db.delete(chirps).where(eq(chirps.id, id))
}

