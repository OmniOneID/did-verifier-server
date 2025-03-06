/*
 * Copyright 2025 OmniOne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omnione.did.base.db.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.constant.ProfileMode;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.base.db.domain.QPayload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PayloadRepositoryAdminImpl implements PayloadRepositoryAdmin {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Payload> searchPayloadList(String searchKey, String searchValue, Pageable pageable) {
        QPayload qPayload = QPayload.payload;
        BooleanExpression predicate = buildPredicate(searchKey, searchValue);

        long total = queryFactory
                .select(qPayload.count())
                .from(qPayload)
                .where(predicate)
                .fetchOne();

        List<Payload> results = queryFactory
                .selectFrom(qPayload)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable, qPayload))
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    public BooleanExpression buildPredicate(String searchKey, String searchValue) {
        QPayload qPayload = QPayload.payload;
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();

        if (searchKey != null && searchValue != null && !searchValue.isEmpty()) {
            switch (searchKey) {
                case "service":
                    predicate = predicate.and(qPayload.service.eq(searchValue));
                    break;
                case "device":
                    predicate = predicate.and(qPayload.device.eq(searchValue));
                    break;
                case "mode":
                    ProfileMode profileMode = ProfileMode.valueOf(searchValue.toUpperCase());
                    predicate = predicate.and(qPayload.mode.eq(profileMode));
                    break;
                default:
                    predicate = predicate.and(Expressions.FALSE);
            }
        }

        return predicate;
    }

    public OrderSpecifier<?>[] getOrderSpecifier(Pageable pageable, QPayload qPayload) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (!pageable.getSort().isSorted()) {
            orders.add(new OrderSpecifier<>(Order.ASC, qPayload.createdAt));
        }

        for (Sort.Order order: pageable.getSort()) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "service":
                    orders.add(new OrderSpecifier<>(direction, qPayload.service));
                    break;
                case "device":
                    orders.add(new OrderSpecifier<>(direction, qPayload.device));
                    break;
                default:
                    orders.add(new OrderSpecifier<>(Order.ASC, qPayload.createdAt));
                    break;
            }
        }
        return orders.toArray(new OrderSpecifier[0]);
    }
}
