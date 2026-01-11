import { z } from 'zod' 
import type { HashedPassword } from './auth'
import { hashPassword, checkPassword } from './auth'

export const UserRequestSchema = z.object({
	email: z.string().email(),
	password: z.string()
})
export type UserRequest = z.infer<typeof UserRequestSchema>
export type UserRequestForStorage = {
	email: string
	password: HashedPassword
}

export const toStorage = async (e: UserRequest): Promise<UserRequestForStorage> => 
	({
		email: e.email,
		password: await hashPassword(e.password),
	})

export type UserResponse = {
	id: string
	createdAt: Date
	updatedAt: Date
	email: string
	isChirpyRed: boolean
}

export type UserResponseWithToken = UserResponse & {
	token: string
	refreshToken: string
}

export const ChirpRequestSchema = z.object({
	body: z.string(),
})
export type ChirpRequest = z.infer<typeof ChirpRequestSchema>

export type ChirpResponse = {
	id: string
	createdAt: Date
	updatedAt: Date
	body: string
	userId: string
}

export type RefreshResponse = {
	token: string
}

export const PolkaHookSchema = z.object({
	event: z.string(),
	data: z.object({
		userId: z.string().uuid()
	})
})
export type PolkaHook = z.infer<typeof PolkaHookSchema>

