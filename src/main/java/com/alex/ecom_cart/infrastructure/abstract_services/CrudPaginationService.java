package com.alex.ecom_cart.infrastructure.abstract_services;

import org.springframework.data.domain.Page;

public interface CrudPaginationService<RQ, RS, ID> {
    Page<RS> readAll(String field, Boolean desc, Integer page);

    RS create(RQ request);

    RS findById(ID id);

    RS update(RQ request, ID id);

    void delete(ID id);

    String FIELD_BY_SORT = "username";

}
