import { and, desc, eq, sql } from 'drizzle-orm'
import { db } from '../queries'
import { users } from './schema' 

import type { HashedPassword } from '../auth'
import type { UserRequestForStorage, UserResponse } from '../models'

export const createUser = async (u: UserRequestForStorage): Promise<UserResponse> => {
	const [row] = await db.insert(users)
		.values({ email: u.email,
			  pass: u.password.value,
			})
		.returning({ id: users.id,
			     createdAt: users.createdAt,
			     updatedAt: users.updatedAt,
			     isChirpyRed: users.isChirpyRed,
			   })
	return { email: u.email,
		 id: row.id,
		 createdAt: row.createdAt,
		 updatedAt: row.updatedAt,
		 isChirpyRed: row.isChirpyRed !== 0,
	}
}
		 
export const findUserByEmail = async (email: string): Promise<UserResponse & {password: HashedPassword} | undefined> => {
	const row_ = await db.select({ id: users.id,
			     	        createdAt: users.createdAt,
			     		updatedAt: users.updatedAt,
			     		isChirpyRed: users.isChirpyRed,
					password: users.pass,
					email: users.email,
			   })
		.from(users)
		.where(eq(users.email, email))
		.limit(1)
	if (row_.length < 1)
		return undefined
	const {password, isChirpyRed, ...r} = row_[0]
	return {...r, isChirpyRed: isChirpyRed !== 0, password: { value: password }}
}

export const updateUser = async (u: UserRequestForStorage, id: string): Promise<void> => {
	await db.update(users)
		.set({	email: u.email,
			pass: u.password.value
		})
		.where(eq(users.id, id))
}

export const upgradeUserToRed = async (id: string): Promise<boolean> => {
	const rows = await db.update(users)
		.set({ isChirpyRed: 1 })
		.where(eq(users.id, id))
		.returning({ red: users.isChirpyRed })
	
	return rows.length == 1
}
		 
export const deleteUsers = async () => {
	await db.delete(users)
}

