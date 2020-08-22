package ch.jvi.budgetmanager.backend.command.domain.transfer.service

import ch.jvi.budgetmanager.backend.command.api.command.CreationCommand
import ch.jvi.budgetmanager.backend.command.api.command.store.CommandStore
import ch.jvi.budgetmanager.backend.command.api.event.EventBus
import ch.jvi.budgetmanager.backend.command.api.service.EntityService
import ch.jvi.budgetmanager.backend.command.domain.transfer.Transfer
import ch.jvi.budgetmanager.backend.command.domain.transfer.command.TransferCommand
import ch.jvi.budgetmanager.backend.command.domain.transfer.command.TransferCommand.CreateTransferCommand
import ch.jvi.budgetmanager.backend.command.domain.transfer.command.TransferCommand.UpdateTransferCommand
import ch.jvi.budgetmanager.backend.command.domain.transfer.event.TransferEvent.CreateTransferEvent
import ch.jvi.budgetmanager.backend.command.domain.transfer.event.TransferEvent.UpdateTransferEvent
import ch.jvi.budgetmanager.backend.server.repository.TransferCommandRepository
import ch.jvi.budgetmanager.backend.server.repository.TransferCreationCommandRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class TransferService(
    private val commandStore: CommandStore,
    private val eventBus: EventBus,
    private val creationCommandRepository: TransferCreationCommandRepository,
    private val updateCommandRepository: TransferCommandRepository
) :
    EntityService<Transfer> {

    /**
     * @return The transfer with the requested ID
     * @throws IllegalArgumentException if no Entity with the given ID is found.
     */
    override fun find(entityId: String): Transfer {
        val createTransferCommand = commandStore.findCreationCommand(entityId) as CreateTransferCommand
        val transfer = Transfer(createTransferCommand)
        return applyCommands(transfer)
    }

    override fun findAll(): List<Transfer> {
        return commandStore.findCreationCommands(this::isTransferCreationCommand)
            .map { Transfer(it as CreateTransferCommand) }
            .map { applyCommands(it) }
    }

    fun findAllForAccount(accountId: String): List<Transfer> {
        return commandStore.findCreationCommands(this::isTransferCreationCommand)
            .map { Transfer(it as CreateTransferCommand) }
            .map { applyCommands(it) }
            .filter { it.recipientId == accountId || it.senderId == accountId }
    }

    private fun isTransferCreationCommand(command: CreationCommand): Boolean {
        return command is CreateTransferCommand
    }

    private fun applyCommands(transfer: Transfer): Transfer {
        val commands: List<TransferCommand> = commandStore.findTransferCommands(transfer.id)
        transfer.applyAll(commands)
        return transfer
    }

    /**
     * Creates and sends a CreateTransferEvent with the given data.
     */
    fun createTransfer(senderId: String, name: String, recipientId: String, amount: BigDecimal) {
        val creationCommand = CreateTransferCommand(name, recipientId, senderId, amount)
        creationCommandRepository.save(creationCommand)

        val createTransferEvent = CreateTransferEvent(recipientId, name, senderId, amount)
        eventBus.send(createTransferEvent)
    }

    /**
     * Sends an UpdateTransferEvent with the given Data.
     */
    fun updateTransfer(updateTransferEvent: UpdateTransferEvent) {
        val updateTransferCommand = UpdateTransferCommand(
            entityId = updateTransferEvent.transferId,
            recipientId = updateTransferEvent.newRecipientId,
            senderId = updateTransferEvent.newSenderId,
            amount = updateTransferEvent.newAmount,
            name = updateTransferEvent.newName
        )
        updateCommandRepository.save(updateTransferCommand)
        eventBus.send(updateTransferEvent)
    }

}