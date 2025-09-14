package com.moshy.jchirp.headers;

import java.util.List;
import java.util.Optional;

public class HeaderSupport {
    public static Optional<String> getByType(List<String> authorizations, String type) {
        for (var auth : authorizations) {
            var elems = auth.split(" +", 2);
            if (elems.length == 2 && elems[0].equals(type))
                return Optional.of(elems[1]);
        }
        return Optional.empty();
    }

}

