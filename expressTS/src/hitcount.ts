import { Request, Response, NextFunction } from "express"

let hitcount: number = 0

const incHitcount = (_unused0: Request, _unused1: Response, next: NextFunction): void => {
	++hitcount
	next()
}

const getHitcount = (): number => hitcount

const resetHitcount = (): void => {
	hitcount = 0
}
	
export { incHitcount, getHitcount, resetHitcount }
