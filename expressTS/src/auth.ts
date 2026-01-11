import { randomBytes } from 'node:crypto'
import { Request, Response, NextFunction } from 'express'
import argon2 from 'argon2'
import jwt from 'jsonwebtoken'
import type { JwtPayload } from 'jsonwebtoken'
import { config } from './config'
import { Unauthorized } from './errors'

export type HashedPassword = { value: string }

export const hashPassword = async (p: string) => ({value: await argon2.hash(p)})
export const checkPassword = async (p: string, hash: HashedPassword) => argon2.verify(hash.value, p)

type payload = Pick<JwtPayload, 'iss' | 'sub' | 'iat' | 'exp'>

export const generateJwt = (uid: string) => {
	const iat = Math.floor(Date.now() / 1000)
	const payload: payload = {
		iss: config.jwtIssuer,
		iat,
		exp: iat + config.jwtExpireSecs,
		sub: uid,
	}
	const token = jwt.sign(payload, config.jwtSecret, { algorithm: 'HS256' })
	return token
}

const extractSubject = (bearer: string): string => {
	let decoded: payload
	try {
		decoded = jwt.verify(bearer, config.jwtSecret) as JwtPayload
	} catch (e) {
		throw new Unauthorized('bad token')
	}
	if (decoded.iss !== config.jwtIssuer)
		throw new Unauthorized('bad token')
	if (!decoded.sub)
		throw new Unauthorized('bad token')
	return decoded.sub
}

export const extractAuth = (req: Request, scheme: string) => {
	const auth: string[] | undefined = req.headersDistinct.authorization
	if (!auth || auth.length < 1)
		throw new Unauthorized('missing token')
	const toks = auth[0].split(' ', 2)
	if (toks.length != 2 || toks[0].toLowerCase() !== scheme)
		throw new Unauthorized('missing token')
	return toks[1]
}
export const extractBearer = (req: Request) => extractAuth(req, 'bearer')

// the typing for this is shoehorned so put it in its own function
const addUserToRequest = (req: Request, u: string) => {
	(req as unknown as Record<string, string>).user = u
}

export const getUserFromRequest = (req: Request): string => {
	const u = (req as unknown as Record<string, string | undefined>).user
	if (!u)
		throw Error('expected user')
	return u
}

export const requireJwt = (req: Request, res: Response, next: NextFunction): void => {
	const user = extractSubject(extractBearer(req)) // throws
	addUserToRequest(req, user)
	next()
}

export const createRefreshToken = () => randomBytes(32).toString('hex')

