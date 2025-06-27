package org.omnione.did.base.db.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import org.omnione.did.base.db.constant.TransactionStatus;
import org.omnione.did.base.db.constant.TransactionType;
import org.omnione.did.base.db.domain.QTransaction;
import org.omnione.did.base.db.domain.QVpSubmit;
import org.omnione.did.base.db.domain.VpSubmit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class VpSubmitRepositoryAdminImpl implements VpSubmitRepositoryAdmin {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<VpSubmit> searchVpSubmitList(String searchKey, String searchValue, Pageable pageable) {
        QVpSubmit qVpSubmit = QVpSubmit.vpSubmit;
        QTransaction qTransaction = QTransaction.transaction;

        // Base predicate: only VP_SUBMIT type transactions
        BooleanExpression predicate = qTransaction.type.eq(org.omnione.did.base.db.constant.TransactionType.VP_SUBMIT);

        // Add additional filters based on search parameters
        BooleanExpression searchPredicate = buildPredicate(searchKey, searchValue, qTransaction);
        if (searchPredicate != null) {
            predicate = predicate.and(searchPredicate);
        }

        // Get total count - count transactions, not vp_submits
        long total = queryFactory
                .select(qTransaction.count())
                .from(qTransaction)
                .where(predicate)
                .fetchOne();

        // Get results with LEFT JOIN - Transaction as main entity
        List<Tuple> results = queryFactory
                .select(qTransaction, qVpSubmit)
                .from(qTransaction)
                .leftJoin(qVpSubmit).on(qTransaction.id.eq(qVpSubmit.transactionId))
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable, qVpSubmit, qTransaction))
                .fetch();

        // Convert Tuple results to VpSubmit entities
        List<VpSubmit> vpSubmits = results.stream()
                .map(tuple -> {
                    org.omnione.did.base.db.domain.Transaction transaction = tuple.get(qTransaction);
                    VpSubmit vpSubmit = tuple.get(qVpSubmit);
                    
                    if (vpSubmit == null) {
                        // Create a placeholder VpSubmit for transactions without actual VP submission
                        vpSubmit = VpSubmit.builder()
                                .id(transaction.getId()) // Use transaction ID as placeholder
                                .transactionId(transaction.getId())
                                .vp(null)
                                .holderDid(null)
                                .build();
                    }
                    return vpSubmit;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(vpSubmits, pageable, total);
    }

    public BooleanExpression buildPredicate(String searchKey, String searchValue, QTransaction qTransaction) {
        if (searchKey == null || searchValue == null || searchValue.isEmpty()) {
            return null;
        }

        switch (searchKey) {
            case "transaction":
                return qTransaction.id.eq(Long.valueOf(searchValue));
            case "status":
                // Filter by transaction status from the Transaction entity
                return qTransaction.status.eq(TransactionStatus.valueOf(searchValue));
            default:
                return null;
        }
    }

    public OrderSpecifier<?>[] getOrderSpecifier(Pageable pageable, QVpSubmit qVpSubmit, QTransaction qTransaction) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (!pageable.getSort().isSorted()) {
            // Default sort by transaction created date descending
            orders.add(new OrderSpecifier<>(Order.DESC, qTransaction.createdAt));
        } else {
            // Process the sort specifications from the pageable
            for (Sort.Order order : pageable.getSort()) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                switch (order.getProperty()) {
                    case "createdAt":
                        // Use transaction created date since that's always available
                        orders.add(new OrderSpecifier<>(direction, qTransaction.createdAt));
                        break;
                    case "transactionStatus":
                        orders.add(new OrderSpecifier<>(direction, qTransaction.status));
                        break;
                    default:
                        // Default to sort by transaction created date
                        orders.add(new OrderSpecifier<>(direction, qTransaction.createdAt));
                        break;
                }
            }
        }

        // If no sort orders were added, add a default one
        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier<>(Order.DESC, qTransaction.createdAt));
        }

        return orders.toArray(new OrderSpecifier[0]);
    }
}