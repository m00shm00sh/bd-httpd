import { Application, Request, Response, json } from 'express'
import * as express from 'express'
import { config } from './config'
import { handleError } from './errors'
import { incHitcount, getHitcount, resetHitcount } from './hitcount'
import { requireJwt } from './auth' 
import { logAfter } from './log'
import { handleCreateChirp, handleGetChirps, handleGetChirpById, handleDeleteChirp } from './chirps'
import { handleCreateUser, handleLoginUser, handleUpdateUser, doDeleteUsers } from './users'
import { handleRefresh, handleRevoke } from './refreshes'
import { requireApikey, handlePolkaWebhook } from './polka'

export const route = (app: Application) => {
	app.get('/api/healthz', (req: Request, res: Response): void => {
		res.set({'Content-type': 'text/plain; charset=utf-8'})
		res.send('OK')
	})

	app.get('/admin/metrics', (req: Request, res: Response): void => {
		res.set({'Content-type': 'text/html; charset=utf-8'})
		res.send(`
<html>
  <body>
    <h1>Welcome, Chirpy Admin</h1>
    <p>Chirpy has been visited ${getHitcount()} times!</p>
  </body>
</html>
		`)
	})
	app.post('/admin/reset', async (req: Request, res: Response): Promise<void> => { 
		await doDeleteUsers(req, res)
		resetHitcount()
		res.end()
	})
	app.use('/app', incHitcount, express.static('../static'))
	app.use(logAfter)
	app.use(json())
	app.post('/api/chirps', requireJwt, handleCreateChirp)
	app.get('/api/chirps', handleGetChirps)
	app.get('/api/chirps/:id', handleGetChirpById)
	app.delete('/api/chirps/:id', requireJwt, handleDeleteChirp)
	app.post('/api/users', handleCreateUser)
	app.post('/api/login', handleLoginUser)
	app.post('/api/refresh', handleRefresh)
	app.post('/api/revoke', handleRevoke)
	app.put('/api/users', requireJwt, handleUpdateUser)
	app.post('/api/polka/webhooks', requireApikey, handlePolkaWebhook)
	app.use(handleError)
}

