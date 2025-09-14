package com.moshy.jchirp.repositories;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class RefreshableImpl<T> implements Refreshable<T> {
    private final EntityManager em;
    @Override
    public void refresh(T t) {
        em.persist(t);
        em.refresh(t);
    }
}
