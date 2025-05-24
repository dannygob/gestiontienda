package com.gestiontienda.android.di

import android.content.Context
import com.gestiontienda.android.data.scanner.BarcodeScannerService
import com.gestiontienda.android.data.scanner.BarcodeScannerServiceImpl
import com.gestiontienda.android.data.printer.PrinterService
import com.gestiontienda.android.data.printer.PrinterServiceImpl
import com.gestiontienda.android.data.payment.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HardwareModule {

    @Provides
    @Singleton
    fun provideBarcodeScannerService(
        @ApplicationContext context: Context,
    ): BarcodeScannerService {
        return BarcodeScannerServiceImpl(context)
    }

    @Provides
    @Singleton
    fun providePrinterService(
        @ApplicationContext context: Context,
    ): PrinterService {
        return PrinterServiceImpl(context)
    }

    @Provides
    @Singleton
    fun providePaymentService(
        cardPaymentProvider: CardPaymentProvider,
        cashPaymentHandler: CashPaymentHandler,
        transferPaymentHandler: TransferPaymentHandler,
    ): PaymentService {
        return PaymentServiceImpl(
            cardPaymentProvider,
            cashPaymentHandler,
            transferPaymentHandler
        )
    }

    @Provides
    @Singleton
    fun provideCardPaymentProvider(): CardPaymentProvider {
        // TODO: Implement real card payment provider
        return object : CardPaymentProvider {
            override suspend fun processCardPayment(payment: PaymentRequest.Card): PaymentResult {
                return PaymentResult.Success("test-transaction")
            }

            override suspend fun refundCardPayment(transactionId: String): PaymentResult {
                return PaymentResult.Success(transactionId)
            }

            override fun getTerminalStatus(): TerminalStatus {
                return TerminalStatus.READY
            }
        }
    }

    @Provides
    @Singleton
    fun provideCashPaymentHandler(): CashPaymentHandler {
        return object : CashPaymentHandler {
            override suspend fun processCashPayment(payment: PaymentRequest.Cash): PaymentResult {
                val change = calculateChange(payment.amount, payment.tenderedAmount)
                return PaymentResult.Success(
                    transactionId = "cash-${System.currentTimeMillis()}",
                    amount = payment.amount,
                    change = change
                )
            }

            override fun calculateChange(total: Double, tendered: Double): Double {
                return tendered - total
            }

            override fun validateCashAmount(amount: Double): Boolean {
                return amount > 0
            }
        }
    }

    @Provides
    @Singleton
    fun provideTransferPaymentHandler(): TransferPaymentHandler {
        return object : TransferPaymentHandler {
            override suspend fun processTransferPayment(payment: PaymentRequest.Transfer): PaymentResult {
                return PaymentResult.Success("transfer-${payment.reference}")
            }

            override suspend fun verifyTransfer(reference: String): Boolean {
                return true
            }

            override fun generateReference(): String {
                return "TR${System.currentTimeMillis()}"
            }
        }
    }
} 