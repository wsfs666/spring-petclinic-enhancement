package org.springframework.samples.petclinic.appointment;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdempotencyReceiptRepository extends JpaRepository<IdempotencyReceipt, Integer> {

	Optional<IdempotencyReceipt> findByPrincipalIdAndOperationAndIdempotencyKey(Integer principalId, String operation,
			UUID idempotencyKey);

	@Modifying // 修改数据库的操作 not just select
	@Query(value = """
			INSERT INTO idempotency_receipts
			    (principal_id, operation, idempotency_key, request_hash)
			VALUES
			    (:principalId, :operation, :idempotencyKey, :requestHash)
			ON CONFLICT ON CONSTRAINT uq_key_comb
			DO NOTHING
			""", nativeQuery = true) // 指定PostgreSQL SQL语法，不是JPQL

	int claimKey(@Param("principalId") Integer principalId, @Param("operation") String operation,
			@Param("idempotencyKey") UUID idempotencyKey, @Param("requestHash") byte[] requestHash);

	long countByPrincipalIdAndOperationAndIdempotencyKey(Integer principalId, String operation, UUID idempotencyKey);

}
