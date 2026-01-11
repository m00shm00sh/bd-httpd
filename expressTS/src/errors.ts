import { Request, Response, NextFunction, json } from "express"

export class BadRequest extends Error {
	readonly rc: number
	constructor(message: string, rc?: number) {
		super(message)
		if (typeof rc === 'number')
			this.rc = rc!
		else
			this.rc = 400
	}
}

export class Unauthorized extends BadRequest {
	constructor(message: string) {
		super(message, 401)
	}
}

export class Forbidden extends BadRequest {
	constructor(message: string) {
		super(message, 403)
	}
}

export class NotFound extends BadRequest {
	constructor(message: string) {
		super(message, 404)
	}
}

export const handleError = (e: Error, req: Request, res: Response, next: NextFunction) => {
	console.log(`error: ${e.message}`)
	if (e instanceof BadRequest)
		res
		.status(e.rc)
		.json({"error": e.message})
	else
		res
		.status(500)
		.json({"error": "Something went wrong on our end"})
}

