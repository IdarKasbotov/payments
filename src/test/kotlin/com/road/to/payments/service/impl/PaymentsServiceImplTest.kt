package com.road.to.payments.service.impl

import com.road.to.payments.db.entity.BalanceEntity
import com.road.to.payments.db.entity.PaymentEntity
import com.road.to.payments.db.repository.BalancesRepository
import com.road.to.payments.db.repository.PaymentRepository
import com.road.to.payments.mapper.BalanceMapper
import com.road.to.payments.mapper.PaymentMapper
import com.road.to.payments.model.GetPaymentsByTypeAndDateRequest
import com.road.to.payments.model.PaymentDto
import com.road.to.payments.model.PaymentType
import com.road.to.payments.model.UpdatePaymentRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

class PaymentsServiceImplTest {

    private val paymentRepository = mock<PaymentRepository>()
    private val balancesRepository = mock<BalancesRepository>()
    private val paymentMapper = mock<PaymentMapper>()
    private val balanceMapper = mock<BalanceMapper>()

    private val sut = PaymentsServiceImpl(paymentRepository, balancesRepository, paymentMapper, balanceMapper)

    @Test
    fun getPaymentsByTypeAndDate_existingPayments_returnsMappedDtos() {
        val entity = paymentEntity(1L, "120000.00")
        val expected = paymentDto("120000.00")
        whenever(paymentRepository.findPaymentByTypeAndDate(TYPE, DATE)).thenReturn(listOf(entity))
        whenever(paymentMapper.dtoFromEntity(entity)).thenReturn(expected)

        val actual = sut.getPaymentsByTypeAndDate(GetPaymentsByTypeAndDateRequest(TYPE, DATE)).payments

        assertEquals(listOf(expected), actual)
    }

    @ParameterizedTest
    @MethodSource("balanceCases")
    fun getBalanceByTypeAndDate_byRepositoryResult_returnsBalanceOrNull(
        repositoryResult: Optional<BigDecimal>,
        expected: BigDecimal?
    ) {
        whenever(balancesRepository.findBalanceByDateAndType(TYPE, DATE)).thenReturn(repositoryResult)

        val actual = sut.getBalanceByTypeAndDate(TYPE, DATE).balance

        assertEquals(expected, actual)
    }

    @Test
    fun updatePaymentSum_validRequest_delegatesToRepository() {
        val sum = BigDecimal("500.00")

        sut.updatePaymentSum(UpdatePaymentRequest(sum, 7L))

        verify(paymentRepository).updatePaymentSum(7L, sum)
    }

    @Test
    fun savePayment_validDto_mapsAndSaves() {
        val dto = paymentDto("100.00")
        val entity = paymentEntity(null, "100.00")
        whenever(paymentMapper.entityFromDto(dto)).thenReturn(entity)

        sut.savePayment(dto)

        verify(paymentRepository).save(entity)
    }

    @Test
    fun countBalancesByTypeAndDate_existingPayments_clearsThenSavesInOrder() {
        val grouped = paymentEntity(null, "180000.00")
        val balance = balanceEntity("180000.00")
        whenever(paymentRepository.paymentsGroupedByTypeAndDate()).thenReturn(listOf(grouped))
        whenever(balanceMapper.balanceEntityFromPaymentEntity(grouped)).thenReturn(balance)

        sut.countBalancesByTypeAndDate()

        inOrder(paymentRepository, balancesRepository) {
            verify(paymentRepository).paymentsGroupedByTypeAndDate()
            verify(balancesRepository).deleteAll()
            verify(balancesRepository).save(listOf(balance))
        }
    }

    private fun paymentEntity(id: Long?, sum: String): PaymentEntity {
        return PaymentEntity().apply {
            this.id = id
            this.sum = BigDecimal(sum)
            this.type = TYPE
            this.date = DATE
        }
    }

    private fun paymentDto(sum: String): PaymentDto {
        return PaymentDto().apply {
            this.sum = BigDecimal(sum)
            this.type = TYPE
            this.date = DATE
        }
    }

    private fun balanceEntity(balance: String): BalanceEntity {
        return BalanceEntity().apply {
            setType(TYPE)
            this.date = DATE
            this.balance = BigDecimal(balance)
        }
    }

    companion object {
        private val TYPE = PaymentType.SALARY
        private val DATE: LocalDate = LocalDate.of(2025, 10, 1)

        @JvmStatic
        fun balanceCases(): List<Arguments> = listOf(
            Arguments.of(Optional.of(BigDecimal("180000.00")), BigDecimal("180000.00")),
            Arguments.of(Optional.empty<BigDecimal>(), null)
        )
    }
}