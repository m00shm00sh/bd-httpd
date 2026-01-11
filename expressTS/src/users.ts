import { Application, Request, Response } from 'express'

import { config } from './config'

import { checkPassword, generateJwt, createRefreshToken, getUserFromRequest } from './auth'
import { BadRequest, Unauthorized, Forbidden } from './errors'
import type { UserResponse, UserResponseWithToken } from './models'
import { UserRequestSchema, toStorage } from './models'

import { createUser, findUserByEmail, updateUser, deleteUsers } from './db/user-queries'
import { saveRefreshToken } from './db/refresh-queries' 

export const handleCreateUser = async (req: Request, res: Response): Promise<void> => {
	if (typeof req.body === 'undefined')
		throw new Error('missing json middleware')
	const u = UserRequestSchema.parse(req.body)
	const ur = await toStorage(u)
	const r = await createUser(ur)
	res.status(201).json(r)
}

export const handleLoginUser = async (req: Request, res: Response): Promise<void> => {
	if (typeof req.body === 'undefined')
		throw new Error('missing json middleware')
	const u = UserRequestSchema.parse(req.body)
	const has = await findUserByEmail(u.email)
	if (!has)
		throw new Unauthorized('bad user/pass')
	const {password, ...r} = has
	const ok = await checkPassword(u.password, password)
	if (!ok) {
		throw new Unauthorized('bad user/pass')
	}
	const r$ = r as unknown as UserResponseWithToken
	r$.token = generateJwt(r.id)
	r$.refreshToken = await saveRefreshToken(r.id, createRefreshToken(), config.jwtRefreshExpireSecs)
	res.json(r)
}

export const handleUpdateUser = async (req: Request, res: Response): Promise<void> => {
	if (typeof req.body === 'undefined')
		throw new Error('missing json middleware')
	const id = getUserFromRequest(req)
	const u = UserRequestSchema.parse(req.body)
	const ur = await toStorage(u)
	await updateUser(ur, id)
	res.json(u)
}

export const doDeleteUsers = async (_0: Request, res: Response): Promise<void> => {
	if (!config.isDev)
		throw new Forbidden('!dev')
	await deleteUsers()
}
 
