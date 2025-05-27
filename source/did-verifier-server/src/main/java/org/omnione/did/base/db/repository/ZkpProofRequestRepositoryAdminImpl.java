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
import org.omnione.did.base.db.constant.PolicyType;
import org.omnione.did.base.db.domain.QZkpProofRequest;
import org.omnione.did.base.db.domain.ZkpProofRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ZkpProofRequestRepositoryAdminImpl implements ZkpProofRequestRepositoryAdmin {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ZkpProofRequest> searchZkpProofRequestList(String searchKey, String searchValue, Pageable pageable) {
        QZkpProofRequest zkpProofRequest = QZkpProofRequest.zkpProofRequest;
        BooleanExpression predicate = buildPredicate(searchKey, searchValue);

        long total = Optional.ofNullable(queryFactory
                        .select(zkpProofRequest.count())
                        .from(zkpProofRequest)
                        .where(predicate)
                        .fetchOne())
                .orElse(0L);

        List<ZkpProofRequest> results = queryFactory
                .selectFrom(zkpProofRequest)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable, zkpProofRequest))
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    public BooleanExpression buildPredicate(String searchKey, String searchValue) {
        QZkpProofRequest zkpProofRequest = QZkpProofRequest.zkpProofRequest;
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();

        if (searchKey != null && searchValue != null && !searchValue.isEmpty()) {
            switch (searchKey) {
                case "name":
                    predicate = predicate.and(zkpProofRequest.name.eq(searchValue));
                    break;
                case "version":
                    predicate = predicate.and(zkpProofRequest.version.eq(searchValue));
                    break;
                default:
                    predicate = predicate.and(Expressions.FALSE);
            }
        }

        return predicate;
    }

    public OrderSpecifier<?>[] getOrderSpecifier(Pageable pageable, QZkpProofRequest zkpProofRequest) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (!pageable.getSort().isSorted()) {
            orders.add(new OrderSpecifier<>(Order.ASC, zkpProofRequest.createdAt));
        }

        for (Sort.Order order: pageable.getSort()) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "name":
                    orders.add(new OrderSpecifier<>(direction, zkpProofRequest.name));
                    break;
                default:
                    orders.add(new OrderSpecifier<>(Order.ASC, zkpProofRequest.createdAt));
                    break;
            }
        }
        return orders.toArray(new OrderSpecifier[0]);
    }
}
