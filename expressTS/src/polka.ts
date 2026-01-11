import { Application, Request, Response, NextFunction } from 'express'

import { config } from './config'
import { extractAuth } from './auth'
import { Unauthorized, NotFound } from './errors'
import { PolkaHookSchema } from './models'

import { upgradeUserToRed } from './db/user-queries'

export const requireApikey = (req: Request, res: Response, next: NextFunction): void => {
	const exp = config.polkaKey
	const got = extractAuth(req, 'apikey')
	if (exp !== got)
		throw new Unauthorized('bad apikey')
	next()
}

export const handlePolkaWebhook = async (req: Request, res: Response): Promise<void> => {
	if (typeof req.body === 'undefined')
		throw new Error('missing json middleware')
	const r = PolkaHookSchema.parse(req.body)
	if (r.event !== 'user.upgraded') {
		res.status(204).end()
		return
	}
	const ok = await upgradeUserToRed(r.data.userId)
	if (!ok)
		throw new NotFound('no match for user')
	res.status(204).end()
}

