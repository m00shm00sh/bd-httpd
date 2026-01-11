import { Application, Request, Response } from 'express'

import { config } from './config'
import { generateJwt } from './auth'
import { extractBearer } from './auth'
import { Unauthorized } from './errors'
import type { RefreshResponse } from './models'
import { findUserByRefresh, revokeToken } from './db/refresh-queries' 

export const handleRefresh = async (req: Request, res: Response): Promise<void> => {
	const rTok = extractBearer(req)
	const uid = await findUserByRefresh(rTok)
	if (!uid)
		throw new Unauthorized('bad token')
	const aTok = generateJwt(uid)
	const r: RefreshResponse = { token: aTok }
	res.json(r)
}

export const handleRevoke = async (req: Request, res: Response): Promise<void> => {
	const rTok = extractBearer(req)
	await revokeToken(rTok)
	res.status(204).end()
}

