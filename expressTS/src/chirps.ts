import { Application, Request, Response } from 'express'

import { getUserFromRequest } from './auth'
import { BadRequest, NotFound } from './errors'
import { ChirpRequestSchema } from './models'
import { createChirp, getChirps, getChirpById, deleteChirp } from './db/chirp-queries' 

const profane = new Set<string>([
	'kerfuffle',
	'sharbert',
	'fornax'
])

export const handleCreateChirp = async (req: Request, res: Response): Promise<void> => {
	if (typeof req.body === 'undefined')
		throw new Error('missing json middleware')
	const u = getUserFromRequest(req)
	const params = ChirpRequestSchema.parse(req.body)
	if (params.body.length > 140)
		throw new BadRequest('Chirp is too long. Max length is 140')

	const cleaned = params.body
		.split(' ')
		.map(e => profane.has(e.toLowerCase()) ? '****' : e)
		.join(' ')

	const r = await createChirp(params, u)
	res.status(201).json(r)
}

export const handleGetChirps = async (req: Request, res: Response): Promise<void> => {
	const authorId = req.query.authorId as string|undefined
	const sort = (() => {
		switch (req.query.sort) {
		case 'asc': return 1
		case 'desc': return -1
		default: return 0
		}
	})()
	const r = await getChirps({uid: authorId, sort})
	res.json(r)
}

export const handleGetChirpById = async (req: Request, res: Response): Promise<void> => {
	const chirp = await getChirpById(req.params.id)
	if (!chirp)
		throw new NotFound('no such chirp')
	res.json(chirp)
}

export const handleDeleteChirp = async (req: Request, res: Response): Promise<void> => {
	const u = getUserFromRequest(req)
	const chirp = await getChirpById(req.params.id)
	if (!chirp) {
		res.status(404).end()
		return
	}
	if (chirp.userId !== u) {
		res.status(403).end()
		return
	}
	await deleteChirp(req.params.id)
	res.status(204).end()
}
