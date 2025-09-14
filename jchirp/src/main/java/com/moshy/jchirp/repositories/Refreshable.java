package com.moshy.jchirp.repositories;

/** A [Refreshable] permits committing a save to facilitate refresh for querying DB-side values.
 * Users shall still call [JpaRepository.saveAndFlush].
 * */
interface Refreshable<T> {
    void refresh(T t);
}
