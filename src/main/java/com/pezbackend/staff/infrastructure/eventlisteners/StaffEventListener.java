package com.pezbackend.staff.infrastructure.eventlisteners;

import com.pezbackend.cashregister.domain.model.events.CashRegisterMatched;
import com.pezbackend.cashregister.domain.model.events.CashRegisterMismatched;
import com.pezbackend.cashregister.domain.model.events.ForcedCloseByCutoff;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener que intercepta la finalización de turnos/cierres de caja
 * y delega el marcaje de asistencias sin salida al StaffAttendanceSweeper.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StaffEventListener {

    private final StaffAttendanceSweeper sweeper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCashRegisterMatched(CashRegisterMatched event) {
        log.info("StaffEventListener: Capturado CashRegisterMatched para caja {}", event.cashRegisterId());
        sweeper.processUnresolvedCheckins(event.restaurantId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCashRegisterMismatched(CashRegisterMismatched event) {
        log.info("StaffEventListener: Capturado CashRegisterMismatched para caja {}", event.cashRegisterId());
        sweeper.processUnresolvedCheckins(event.restaurantId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onForcedCloseByCutoff(ForcedCloseByCutoff event) {
        log.info("StaffEventListener: Capturado ForcedCloseByCutoff para caja {}", event.cashRegisterId());
        sweeper.processUnresolvedCheckins(event.restaurantId());
    }
}
