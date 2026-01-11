import { env, loadEnvFile } from 'node:process'

loadEnvFile()

type Config = {
	dbUrl: string
	isDev: boolean
	port: number
	jwtSecret: string
	jwtIssuer: string
	jwtExpireSecs: number
	jwtRefreshExpireSecs: number
	polkaKey: string
}

const envOrThrow = (k: string): string => {
	const v = env[k]
	if (!v)
		throw new Error(`missing envar ${k}`)
	return v
}

export const config = {
	dbUrl: envOrThrow('DB_URL'),
	isDev: env['PLATFORM'] === 'dev',
	port: Number.parseInt(env['PORT'] ?? '8080'),
	jwtSecret: envOrThrow('JWT_SECRET'),
	jwtIssuer: envOrThrow('JWT_ISSUER'),
	jwtExpireSecs: Number.parseInt(env['JWT_EXPIRE_SECS'] ?? '3600'),
	jwtRefreshExpireSecs: Number.parseInt(env['JWT_REFRESH_EXPIRE_SECS'] ?? '5184000'),
	polkaKey: envOrThrow('POLKA_KEY'),
}

