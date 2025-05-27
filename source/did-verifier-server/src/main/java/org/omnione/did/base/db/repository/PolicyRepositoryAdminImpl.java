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
import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.domain.QPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PolicyRepositoryAdminImpl implements PolicyRepositoryAdmin {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Policy> searchPolicyList(String searchKey, String searchValue, PolicyType policyType, Pageable pageable) {
        QPolicy qPolicy = QPolicy.policy;

        BooleanExpression predicate = buildPredicate(searchKey, searchValue, policyType);

        long total = queryFactory
                .select(qPolicy.count())
                .from(qPolicy)
                .where(predicate)
                .fetchOne();

        List<Policy> results = queryFactory
                .selectFrom(qPolicy)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable, qPolicy))
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    private BooleanExpression buildPredicate(String searchKey, String searchValue, PolicyType policyType) {
        QPolicy qPolicy = QPolicy.policy;
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();

        if (policyType != null) {
            predicate = predicate.and(qPolicy.policyType.eq(policyType));
        }

        if (searchKey != null && searchValue != null && !searchValue.isEmpty()) {
            switch (searchKey) {
                case "policyTtitle":
                    predicate = predicate.and(qPolicy.policyTitle.eq(searchValue));
                    break;
                default:
                    predicate = predicate.and(Expressions.FALSE);
            }
        }

        return predicate;
    }

    private OrderSpecifier<?>[]  getOrderSpecifier(Pageable pageable, QPolicy qPolicy) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (!pageable.getSort().isSorted()) {
            orders.add(new OrderSpecifier<>(Order.ASC, qPolicy.createdAt));
        }

        for (Sort.Order order: pageable.getSort()) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "title":
                    orders.add(new OrderSpecifier<>(direction, qPolicy.policyTitle));
                    break;
                default:
                    orders.add(new OrderSpecifier<>(Order.ASC, qPolicy.createdAt));
                    break;
            }
        }

        return orders.toArray(new OrderSpecifier[0]);
    }
}
