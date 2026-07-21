package com.road.to.payments.controller

import com.road.to.payments.model.GetBalanceByTypeAndDateResponse
import com.road.to.payments.model.GetPaymentsByTypeAndDateResponse
import com.road.to.payments.model.PaymentDto
import com.road.to.payments.model.PaymentType
import com.road.to.payments.service.PaymentsService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDate

@WebMvcTest(PaymentsController::class)
class PaymentsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var paymentsService: PaymentsService

    @Test
    fun getPaymentsByTypeAndDate_validRequest_returns200WithPayments() {
        whenever(paymentsService.getPaymentsByTypeAndDate(any()))
            .thenReturn(GetPaymentsByTypeAndDateResponse(listOf(paymentDto())))

        mockMvc.perform(
            post("/payments/getPaymentsByTypeAndDate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"type":"SALARY","date":"2025-10-01"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.payments[0].type").value("SALARY"))
            .andExpect(jsonPath("$.payments[0].sum").value(120000.00))
    }

    @Test
    fun getBalanceByTypeAndDate_validRequest_returns200WithBalance() {
        whenever(paymentsService.getBalanceByTypeAndDate(TYPE, DATE))
            .thenReturn(GetBalanceByTypeAndDateResponse(BigDecimal("180000.00")))

        mockMvc.perform(
            get("/payments/getBalanceByTypeAndDate")
                .param("type", "SALARY")
                .param("date", "2025-10-01")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(180000.00))
    }

    @Test
    fun updatePaymentSum_validRequest_returns200() {
        mockMvc.perform(
            post("/payments/updatePaymentSum")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"sum":500.00,"id":7}""")
        )
            .andExpect(status().isOk)

        verify(paymentsService).updatePaymentSum(any())
    }

    @Test
    fun updatePaymentSum_negativeSum_returns400AndSkipsService() {
        mockMvc.perform(
            post("/payments/updatePaymentSum")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"sum":-1,"id":7}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.sum").exists())

        verify(paymentsService, never()).updatePaymentSum(any())
    }

    private fun paymentDto(): PaymentDto {
        return PaymentDto().apply {
            sum = BigDecimal("120000.00")
            type = TYPE
            date = DATE
        }
    }

    companion object {
        private val TYPE = PaymentType.SALARY
        private val DATE: LocalDate = LocalDate.of(2025, 10, 1)
    }
}