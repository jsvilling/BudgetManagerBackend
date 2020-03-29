package ch.jvi.budgetmanager.backend.server.controller

import ch.jvi.budgetmanager.backend.core.service.TransferService
import ch.jvi.budgetmanager.backend.domain.transfer.Transfer
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

/**
 * Rest Controller for all Transfer related operations.
 *
 * @author J. Villing
 */
@RestController
@RequestMapping("/api/transfers")
class TransferController(private val transferService: TransferService) {

    @GetMapping()
    fun get(): List<Transfer> {
        return transferService.findAll()
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String) {
        transferService.find(id)
    }

    @PutMapping("/create")
    fun create(
        @RequestParam senderId: String,
        @RequestParam recipientId: String,
        @RequestParam amount: BigDecimal,
        @RequestParam budgetId: String
    ) {
        transferService.createTransfer(senderId, recipientId, amount, budgetId)
    }

    @PostMapping("/{id}/update")
    fun update(@PathVariable id: String, @RequestParam senderId: String, @RequestParam recipientId: String, @RequestParam amount: BigDecimal) {
        transferService.updateTransfer(id, senderId, recipientId, amount)
    }
}