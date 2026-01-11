import { Request, Response, NextFunction } from "express"

const logAfter = (_: Request, res: Response, next: NextFunction): void => {	
	res.on("finish", () => {
		const sc: number = res.statusCode
		if (sc >= 200 && sc <= 299)
			return
		const req: Request = res.req
		console.log(`[NON-OK] ${req.method} ${req.path} Status: ${sc}`)
	})	
	next()
}

export { logAfter }
